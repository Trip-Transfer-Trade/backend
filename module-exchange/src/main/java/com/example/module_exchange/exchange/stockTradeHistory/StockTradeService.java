package com.example.module_exchange.exchange.stockTradeHistory;

import com.example.module_exchange.clients.TripClient;
import com.example.module_exchange.exchange.exchangeCurrency.ExchangeCurrency;
import com.example.module_exchange.exchange.exchangeCurrency.ExchangeCurrencyRepository;
import com.example.module_exchange.exchange.transactionHistory.TransactionHistory;
import com.example.module_exchange.exchange.transactionHistory.TransactionHistoryRepository;
import com.example.module_exchange.exchange.transactionHistory.TransactionType;
import com.example.module_trip.tripGoal.TripGoalResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StockTradeService {
    private final TripClient tripClient;
    private final StockTradeHistoryRepository stockTradeHistoryRepository;
    private final ExchangeCurrencyRepository exchangeCurrencyRepository;
    private final TransactionHistoryRepository transactionHistoryRepository;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, String> redisTemplate;

    public StockTradeService(TripClient tripClient, StockTradeHistoryRepository stockTradeHistoryRepository, ExchangeCurrencyRepository exchangeCurrencyRepository, TransactionHistoryRepository transactionHistoryRepository, RestTemplate restTemplate, ObjectMapper objectMapper, RedisTemplate<String, String> redisTemplate) {
        this.tripClient = tripClient;
        this.stockTradeHistoryRepository = stockTradeHistoryRepository;
        this.exchangeCurrencyRepository = exchangeCurrencyRepository;
        this.transactionHistoryRepository = transactionHistoryRepository;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
    }

    @Transactional
    public void orderStockBuy(StockTradeDTO stockTradeDTO) {
        Integer accountId = getAccountIdFromTripId(stockTradeDTO.getTripId());
        BigDecimal amount = stockTradeDTO.getAmount();
        TradeType tradeType = TradeType.BUY;

        ExchangeCurrency exchangeCurrency = getExchangeCurrencyFromAccountId(accountId, stockTradeDTO.getCurrencyCode());

        isQuantityZero(stockTradeDTO);
        validateSufficientBalance(exchangeCurrency, stockTradeDTO);

        StockTradeHistory stockTradeHistory = stockTradeDTO.toStockTradeHistory(exchangeCurrency, tradeType);
        stockTradeHistoryRepository.save(stockTradeHistory);

        TransactionHistory transactionHistory = stockTradeDTO.toTransactionHistory(exchangeCurrency, TransactionType.WITHDRAWAL, amount);
        transactionHistoryRepository.save(transactionHistory);

        // redis 에 매수 매도 내역 저장
        saveOrderToRedis(stockTradeDTO, true);

        exchangeCurrency.changeAmount(amount.negate());
    }

    @Transactional
    public void orderStockSell(StockTradeDTO stockTradeDTO) {
        Integer accountId = getAccountIdFromTripId(stockTradeDTO.getTripId());
        BigDecimal amount = stockTradeDTO.getAmount();
        TradeType tradeType = TradeType.SELL;

        ExchangeCurrency exchangeCurrency = getExchangeCurrencyFromAccountId(accountId, stockTradeDTO.getCurrencyCode());

        isQuantityZero(stockTradeDTO);
        validateSufficientQuantity(exchangeCurrency, stockTradeDTO);

        StockTradeHistory stockTradeHistory = stockTradeDTO.toStockTradeHistory(exchangeCurrency, tradeType);
        stockTradeHistoryRepository.save(stockTradeHistory);

        TransactionHistory transactionHistory = stockTradeDTO.toTransactionHistory(exchangeCurrency, TransactionType.DEPOSIT, amount);
        transactionHistoryRepository.save(transactionHistory);

        saveOrderToRedis(stockTradeDTO, false);

        exchangeCurrency.changeAmount(amount);
    }

    private Integer getAccountIdFromTripId(int tripId) {
        TripGoalResponseDTO tripGoalResponseDTO = tripClient.getTripGoal(tripId);
        return tripGoalResponseDTO.getAccountId();
    }

    private ExchangeCurrency getExchangeCurrencyFromAccountId(Integer accountId, String currencyCode){
        return exchangeCurrencyRepository
                .findByAccountIdAndCurrencyCode(accountId, currencyCode)
                .orElseGet(() -> {
                    ExchangeCurrency newCurrency = ExchangeCurrency.builder()
                            .accountId(accountId)
                            .currencyCode(currencyCode)
                            .amount(BigDecimal.ZERO)
                            .build();
                    return exchangeCurrencyRepository.save(newCurrency);
                });
    }

    private void validateSufficientBalance(ExchangeCurrency currency, StockTradeDTO stockTradeDTO) {
        BigDecimal totalPurchaseAmount = stockTradeDTO.getAmount();
        if(totalPurchaseAmount.compareTo(currency.getAmount()) > 0){
            throw new RuntimeException("매수 실패: 보유한 예수금이 주문 금액보다 작습니다.");
        }
    }

    private void validateSufficientQuantity(ExchangeCurrency currency, StockTradeDTO stockTradeDTO) {
        BigDecimal totalBuyQuantity = stockTradeHistoryRepository.findTotalBuyQuantityByStockCode(stockTradeDTO.getStockCode());
        BigDecimal totalSellQuantity = stockTradeHistoryRepository.findTotalSellQuantityByStockCode(stockTradeDTO.getStockCode());
        totalSellQuantity = (totalSellQuantity != null) ? totalSellQuantity : BigDecimal.ZERO;

        BigDecimal ownedQuantity = totalBuyQuantity.subtract(totalSellQuantity);

        BigDecimal orderSellQuantity = BigDecimal.valueOf(stockTradeDTO.getQuantity());

        if(orderSellQuantity.compareTo(ownedQuantity) > 0){
            throw new RuntimeException("매도 실패: 보유한 수량이 주문 수량보다 작습니다.");
        }
    }

    private void isQuantityZero(StockTradeDTO stockTradeDTO) {
        if(stockTradeDTO.getQuantity() == 0){
            throw new RuntimeException("주문 실패: 수량은 0보다 커야 합니다.");
        }
    }

    private void saveOrderToRedis(StockTradeDTO stockTradeDTO, boolean isBuy) {
        String cacheKey = "trip:" + stockTradeDTO.getTripId() + ":stock:" + stockTradeDTO.getStockCode();

        String luaScript = isBuy ? """
                local key = "trip:" .. ARGV[3] .. ":stock:" .. KEYS[1]
                local total_cost = redis.call('HINCRBYFLOAT', key, 'total_cost', ARGV[1] * ARGV[2])
                local total_quantity = redis.call('HINCRBY', key, 'total_quantity', ARGV[2])
                
                if total_quantity > 0 then
                    local avg_price = total_cost / total_quantity
                    redis.call('HSET', key, 'average_price', avg_price)
                end
                
                return redis.call('HGETALL', key)
                
                """: """
                local key = "trip:" .. ARGV[3] .. ":stock:" .. KEYS[1]
                
                local total_quantity = tonumber(redis.call('HGET', key, 'total_quantity') or '0')
                local avg_price = tonumber(redis.call('HGET', key, 'average_price') or '0')
                local total_cost = tonumber(redis.call('HGET', key, 'total_cost') or '0')
                
                if total_quantity < tonumber(ARGV[2]) then
                    return "ERROR: 매도 수량이 보유 수량보다 많습니다."
                end
                
                local new_total_cost = 0
                local new_total_quantity = total_quantity - tonumber(ARGV[2])
                
                if avg_price <= tonumber(ARGV[1]) then
                     new_total_cost = total_cost - (avg_price * tonumber(ARGV[2]))
                else
                     new_total_cost = total_cost - (tonumber(ARGV[1]) * tonumber(ARGV[2]))
                end
                
                redis.call('HSET', key, 'total_quantity', new_total_quantity)
                redis.call('HSET', key, 'total_cost', new_total_cost)
                
                if new_total_quantity > 0 then
                    local new_avg_price = new_total_cost / new_total_quantity
                    redis.call('HSET', key, 'average_price', new_avg_price)
                else
                    redis.call('HSET', key, 'average_price', 0)
                end
                
                return redis.call('HGETALL', key)
                """;

        RedisScript<List> script = new DefaultRedisScript<>(luaScript, List.class);
        List<Object> result = redisTemplate.execute(script,
                Collections.singletonList(stockTradeDTO.getStockCode()),   // KEYS[1]
                String.valueOf(stockTradeDTO.getPricePerUnit()),                 // ARGV[1]
                String.valueOf(stockTradeDTO.getQuantity()),               // ARGV[2]
                String.valueOf(stockTradeDTO.getTripId())                  // ARGV[3]
        );

        Map<Object, Object> redisData = convertListToMap(result);
        System.out.println("Updated Redis Data: " + redisData);
    }

    private Map<Object, Object> convertListToMap(List<Object> list) {
        Map<Object, Object> map = new HashMap<>();
        if (list != null && list.size() % 2 == 0) {
            for (int i = 0; i < list.size(); i += 2) {
                map.put(list.get(i), list.get(i + 1));
            }
        }
        return map;
    }
}
