package com.example.module_exchange.exchange.stockTradeHistory;

import com.example.module_exchange.clients.TripClient;
import com.example.module_exchange.exchange.exchangeCurrency.ExchangeCurrency;
import com.example.module_exchange.exchange.exchangeCurrency.ExchangeCurrencyRepository;
import com.example.module_exchange.exchange.transactionHistory.TransactionHistory;
import com.example.module_exchange.exchange.transactionHistory.TransactionHistoryRepository;
import com.example.module_exchange.exchange.transactionHistory.TransactionType;
import com.example.module_trip.tripGoal.TripGoal;
import com.example.module_trip.tripGoal.TripGoalRepository;
import com.example.module_trip.tripGoal.TripGoalResponseDTO;
import com.example.module_trip.tripGoal.TripGoalUpdateDTO;
import com.example.module_utility.response.Response;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class StockTradeService {
    private static final Logger logger = LoggerFactory.getLogger(StockTradeService.class);

    private final TripClient tripClient;
    private final StockTradeHistoryRepository stockTradeHistoryRepository;
    private final ExchangeCurrencyRepository exchangeCurrencyRepository;
    private final TransactionHistoryRepository transactionHistoryRepository;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${APP_URL}")
    private String apiUrl;

    @Value("${APP_KEY}")
    private String appKey;

    @Value("${APP_SECRET}")
    private String appSecret;

    @Value("${ACCESS_TOKEN}")
    private String accessToken;

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

    public StockHoldingsDTO getStockInfoFromRedis(int tripId) {
        String pattern = "trip:" + tripId + ":stock:*";

        Set<String> keys = redisTemplate.keys(pattern);

        List<StockDetailDTO> stockDetails = new ArrayList<>();

        for(String key : keys){
            Map<Object, Object> stockData = redisTemplate.opsForHash().entries(key);
            try {
                String stockCode = key.substring(key.lastIndexOf(":") + 1);
                String quantity = stockData.get("total_quantity").toString();
                String avgPrice = stockData.get("average_price").toString();

                String stockName = getStockName(stockCode);
                String currencyPrice = getStockPrice(stockCode);

                StockDetailDTO stockDetailDTO = StockDetailDTO.builder()
                        .stockCode(stockCode)
                        .quantity(quantity)
                        .avgPrice(avgPrice)
                        .stockName(stockName)
                        .currencyPrice(currencyPrice)
                        .build();
                stockDetails.add(stockDetailDTO);
            } catch (Exception e) {
                System.err.println("Error processing stock data for key: " + key);
                e.printStackTrace();
            }
        }
        return new StockHoldingsDTO(stockDetails);
    }

    private Integer getAccountIdFromTripId(int tripId) {
        ResponseEntity<Response<TripGoalResponseDTO>> responseEntity = tripClient.getTripGoal(tripId);
        TripGoalResponseDTO tripGoalResponseDTO = responseEntity.getBody().getData();
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
        System.out.println("🔍 주문 금액: " + totalPurchaseAmount);
        System.out.println("💰 현재 보유 예수금: " + currency.getAmount());
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
                String.valueOf(stockTradeDTO.getPricePerUnit()),           // ARGV[1]
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

    private String getStockName(String stockCode) {
        String cacheKey = "stockName:" + stockCode;
        ValueOperations<String, String> ops = redisTemplate.opsForValue();

        String value = ops.get(cacheKey);
        if(value != null){
            try{
                return value;
            } catch (Exception e){
                logger.error("Redis 캐시 변환 오류");
            }
        }

        // 한투 API 호출
        String url = apiUrl + "/uapi/domestic-stock/v1/quotations/search-info";
        String queryParam = "?PDNO=" + stockCode + "&PRDT_TYPE_CD=300";

        HttpHeaders headers = new HttpHeaders();
        headers.set("authorization", "Bearer " + accessToken);
        headers.set("content-type", "application/json");
        headers.set("appkey", appKey);
        headers.set("appsecret", appSecret);
        headers.set("tr_id", "CTPF1604R");

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url + queryParam, HttpMethod.GET, entity, String.class);

        logger.info("한투 API 응답: {}", response.getBody());

        try{
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            String stockName = rootNode.path("output").path("prdt_abrv_name").asText();
            ops.set(cacheKey, stockName, 1000, TimeUnit.SECONDS);
            return stockName;
        }catch (Exception e){
            logger.error("API 호출 실패", e);
            return null;
        }
    }

    private String getStockPrice(String stockCode) {
        String cacheKey = "stockPrice:" + stockCode;
        ValueOperations<String, String> ops = redisTemplate.opsForValue();

        String value = ops.get(cacheKey);
        if(value != null){
            try{
                return value;
            } catch (Exception e){
                logger.error("Redis 캐시 변환 오류");
            }
        }

        // 한투 API 호출
        String url = apiUrl + "/uapi/domestic-stock/v1/quotations/inquire-price";
        String queryParam = "?fid_cond_mrkt_div_code=J" + "&fid_input_iscd=" + stockCode;

        HttpHeaders headers = new HttpHeaders();
        headers.set("authorization", "Bearer " + accessToken);
        headers.set("content-type", "application/json");
        headers.set("appkey", appKey);
        headers.set("appsecret", appSecret);
        headers.set("tr_id", "FHKST01010100");

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url + queryParam, HttpMethod.GET, entity, String.class);

        logger.info("한투 API 응답: {}", response.getBody());

        try{
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            String currencyPrice = rootNode.path("output").path("stck_prpr").asText();
            ops.set(cacheKey, currencyPrice, 1000, TimeUnit.SECONDS);
            return currencyPrice;
        }catch (Exception e){
            logger.error("API 호출 실패", e);
            return null;
        }

    }

    // 매도 발생 시 실현 손익 계산
    private void realisedCalc(int tripId){


    }

    // 전날 realisedProfit 가져오기
    @Transactional
    public void getAllUserRealisedProfit(){
        ResponseEntity<Response<List<TripGoalResponseDTO>>> responseEntity = tripClient.getAllTrips();
        List<TripGoalResponseDTO> allTripGoals = responseEntity.getBody().getData();

        for(TripGoalResponseDTO tripGoalResponseDTO : allTripGoals){
            Integer tripId = tripGoalResponseDTO.getId();
            BigDecimal realisedProfit = tripGoalResponseDTO.getRealisedProfit() != null ? tripGoalResponseDTO.getRealisedProfit() : BigDecimal.ZERO;

            String cacheKey = "trip:" + tripId + "realisedProfit:" + realisedProfit;
            ValueOperations<String, String> ops = redisTemplate.opsForValue();
            ops.set(cacheKey, realisedProfit.toString());

            System.out.println("여행 목표 ID " + tripId + " | realisedProfit: " + realisedProfit + " 저장 완료!");
        }
        System.out.println("모든 사용자 실현 손익 저장 완료!");
    }

    // 장 마감 후 realisedProfit 저장
    public void storeAllUserRealisedProfit() {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();

        Set<String> keys = redisTemplate.keys("trip:*realisedProfit:*");

        for (String key : keys) {
            TripGoalUpdateDTO tripGoalUpdateDTO = TripGoalUpdateDTO.fromRedisKey(key);

            ResponseEntity<Response<TripGoalResponseDTO>> response = tripClient.updateRealisedProfit(tripGoalUpdateDTO);

            if(response.getStatusCode().is2xxSuccessful()){
                System.out.println("여행 목표 ID " + tripGoalUpdateDTO.getTripGoalId() + " | realisedProfit: " + tripGoalUpdateDTO.getRealisedProfit() + " 저장 완료!");
            }
            else {
                System.out.println("여행 목표 ID " + tripGoalUpdateDTO.getTripGoalId() + "찾을 수 없음");
            }
            redisTemplate.delete(key);
        }
        System.out.println("모든 realisedProfit DB 저장 완료!");
    }
}
