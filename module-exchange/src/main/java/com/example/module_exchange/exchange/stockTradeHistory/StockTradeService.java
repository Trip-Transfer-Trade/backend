package com.example.module_exchange.exchange.stockTradeHistory;

import com.example.module_exchange.clients.TripClient;
import com.example.module_exchange.exchange.exchangeCurrency.ExchangeCurrency;
import com.example.module_exchange.exchange.exchangeCurrency.ExchangeCurrencyRepository;
import com.example.module_exchange.exchange.transactionHistory.TransactionHistory;
import com.example.module_exchange.exchange.transactionHistory.TransactionHistoryRepository;
import com.example.module_exchange.exchange.transactionHistory.TransactionType;
import com.example.module_exchange.redisData.exchangeData.exchangeRateChart.ExchangeRateChartDTO;
import com.example.module_exchange.redisData.exchangeData.exchangeRateChart.ExchangeRateChartService;
import com.example.module_trip.tripGoal.TripGoalProfitUpdateDTO;
import com.example.module_trip.tripGoal.TripGoalResponseDTO;
import com.example.module_trip.tripGoal.TripGoalUpdateDTO;
import com.example.module_utility.response.Response;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.checkerframework.checker.units.qual.A;
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
import java.util.stream.Collectors;

@Service
public class StockTradeService {
    private static final Logger logger = LoggerFactory.getLogger(StockTradeService.class);

    private final TripClient tripClient;
    private final StockTradeHistoryRepository stockTradeHistoryRepository;
    private final ExchangeCurrencyRepository exchangeCurrencyRepository;
    private final TransactionHistoryRepository transactionHistoryRepository;
    private final ExchangeRateChartService exchangeRateChartService;
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

    public StockTradeService(TripClient tripClient, StockTradeHistoryRepository stockTradeHistoryRepository, ExchangeCurrencyRepository exchangeCurrencyRepository, TransactionHistoryRepository transactionHistoryRepository, RestTemplate restTemplate, ObjectMapper objectMapper, RedisTemplate<String, String> redisTemplate, ExchangeRateChartService exchangeRateChartService) {
        this.tripClient = tripClient;
        this.stockTradeHistoryRepository = stockTradeHistoryRepository;
        this.exchangeCurrencyRepository = exchangeCurrencyRepository;
        this.transactionHistoryRepository = transactionHistoryRepository;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
        this.exchangeRateChartService= exchangeRateChartService;
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
        realisedCalc(stockTradeDTO.getTripId(), stockTradeDTO.getStockCode(), stockTradeDTO.getQuantity());

        exchangeCurrency.changeAmount(amount);
    }

    @Transactional
    public void orderBulkSell(StockTradeDTO stockTradeDTO) {
        TradeType tradeType = TradeType.SELL;
        String pattern = "trip:" + stockTradeDTO.getTripId() + ":stock:*";
        Set<String> keys = redisTemplate.keys(pattern);
        String field = "total_quantity";

        Map<String, Integer> totalQuantity = keys.stream()
                .collect(Collectors.toMap(
                        key -> key.split(":")[key.split(":").length - 1], // 주식 코드 추출
                        key -> {
                            String quantityStr = (String) redisTemplate.opsForHash().get(key, field);
                            return (quantityStr != null) ? Integer.parseInt(quantityStr) : 0;
                        }
                ));

        System.out.println("Stock Keys: " + keys);
        System.out.println("Total Quantities: " + totalQuantity);

        Map<String, BigDecimal> stockPrices = totalQuantity.keySet().stream()
                .collect(Collectors.toMap(
                        stockCode -> stockCode,
                        stockCode -> {
                            String priceStr = getStockPrice(stockCode);
                            return (priceStr != null && !priceStr.isEmpty()) ? new BigDecimal(priceStr) : BigDecimal.ZERO;
                        }
                ));

        Map<String, BigDecimal> stockTotalAmounts = totalQuantity.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> stockPrices.get(entry.getKey()).multiply(BigDecimal.valueOf(entry.getValue()))
                ));

        BigDecimal totalSellAmount = stockTotalAmounts.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        redisTemplate.delete(keys);

        Integer accountId = getAccountIdFromTripId(stockTradeDTO.getTripId());
        ExchangeCurrency exchangeCurrency = getExchangeCurrencyFromAccountId(accountId, stockTradeDTO.getCurrencyCode());

        for (Map.Entry<String, Integer> entry : totalQuantity.entrySet()) {
            String stockCode = entry.getKey();
            int quantity = entry.getValue();
            BigDecimal pricePerUnit = stockPrices.get(stockCode);
            BigDecimal totalPrice = stockTotalAmounts.get(stockCode);

            StockTradeHistory stockTradeHistory = stockTradeDTO.toStockTradeHistory(exchangeCurrency, tradeType, stockCode, quantity, pricePerUnit, totalPrice);
            stockTradeHistoryRepository.save(stockTradeHistory);

            TransactionHistory transactionHistory = stockTradeDTO.toTransactionHistory(exchangeCurrency, TransactionType.DEPOSIT, totalPrice);
            transactionHistoryRepository.save(transactionHistory);
        }

        exchangeCurrency.changeAmount(totalSellAmount);
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

        boolean isUs = stockCode.chars().allMatch(Character::isAlphabetic);

        String url;
        String queryParam;
        String trId = "";
        String custType = "";
        String stockName = "";

        // 한투 API 호출
        if(isUs){
            url = apiUrl + "/uapi/overseas-price/v1/quotations/search-info";
            queryParam = "?PDNO=" + stockCode + "&PRDT_TYPE_CD=512";
            trId = "CTPF1702R";
            custType = "P";
        } else {
            url = apiUrl + "/uapi/domestic-stock/v1/quotations/search-info";
            queryParam = "?PDNO=" + stockCode + "&PRDT_TYPE_CD=300";
            trId = "CTPF1604R";
            custType = "";
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("authorization", "Bearer " + accessToken);
        headers.set("content-type", "application/json");
        headers.set("appkey", appKey);
        headers.set("appsecret", appSecret);
        headers.set("tr_id", trId);
        headers.set("custtype", custType);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url + queryParam, HttpMethod.GET, entity, String.class);

        logger.info("한투 API 응답: {}", response.getBody());

        try{
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            if(isUs){
                stockName = rootNode.path("output").path("prdt_eng_name").asText();
            } else {
                stockName = rootNode.path("output").path("prdt_abrv_name").asText();
            }
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

        if(Strings.isNotBlank(value)){
            try{
                return value;
            } catch (Exception e){
                logger.error("Redis 캐시 변환 오류");
            }
        }

        boolean isUs = stockCode.chars().allMatch(Character::isAlphabetic);

        String url;
        String queryParam;
        String trId = "";
        String currencyPrice = "";

        // 한투 API 호출
        if(isUs){
            url = apiUrl + "/uapi/overseas-price/v1/quotations/price-detail";
            queryParam = "?AUTH=&EXCD=NAS" + "&SYMB=" + stockCode;
            trId = "HHDFS76200200";
        } else {
            url = apiUrl + "/uapi/domestic-stock/v1/quotations/inquire-price";
            queryParam = "?fid_cond_mrkt_div_code=J" + "&fid_input_iscd=" + stockCode;
            trId = "FHKST01010100";
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("authorization", "Bearer " + accessToken);
        headers.set("content-type", "application/json");
        headers.set("appkey", appKey);
        headers.set("appsecret", appSecret);
        headers.set("tr_id", trId);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url + queryParam, HttpMethod.GET, entity, String.class);

        logger.info("한투 API 응답: {}", response.getBody());

        try{
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            if(isUs){
                currencyPrice = rootNode.path("output").path("last").asText();
                logger.info("currencyPrice : "+currencyPrice);
            } else {
                currencyPrice = rootNode.path("output").path("stck_prpr").asText();
                logger.info("KR currencyPrice : "+currencyPrice);
            }
            ops.set(cacheKey, currencyPrice, 1000, TimeUnit.SECONDS);
            return currencyPrice;
        }catch (Exception e){
            logger.error("API 호출 실패", e);
            return null;
        }

    }
    public Map<String,BigDecimal> unrealisedCalc(int tripId){
        Set<String> stockKeys = redisTemplate.keys("trip:"+tripId+":stock:*");
        BigDecimal profitKRW = BigDecimal.ZERO;
        BigDecimal profitUSD = BigDecimal.ZERO;
        logger.info(stockKeys.toString());

        for (String stockKey : stockKeys) {
            String stockCode = stockKey.split(":stock:")[1];

            Object quantityObj = redisTemplate.opsForHash().get(stockKey, "total_quantity");
            if (quantityObj == null || Integer.parseInt(quantityObj.toString()) <= 0) {
                continue;
            }
            int quantity = Integer.parseInt(quantityObj.toString());
            BigDecimal avgPrice = new BigDecimal(getAvgPrice(tripId, stockCode).replace(",",""));
            logger.info("avgPrice"+avgPrice);
            BigDecimal currentPrice = new BigDecimal(getStockPrice(stockCode).replace(",",""));
            logger.info("avgPrice : "+avgPrice +", currentPrice : "+currentPrice);

            boolean isUs = stockCode.chars().allMatch(Character::isAlphabetic);

            BigDecimal stockProfit = currentPrice.subtract(avgPrice).multiply(new BigDecimal(quantity));

            if(isUs){
                profitUSD = profitUSD.add(stockProfit);
            } else{
                profitKRW = profitKRW.add(stockProfit);
            }

            logger.info("주식 코드 : " + stockCode + "미국 통화 : "+isUs+ "수량 : " + quantity + "평단가 : " + avgPrice + " 종가 : " + currentPrice + "수익 : " + stockProfit );
        }
        logger.info("총 평가 손익 : " + profitKRW + ", "+profitUSD);
        Map<String,BigDecimal> profitMap = new HashMap<>();
        profitMap.put("profitKRW", profitKRW);
        profitMap.put("profitUSD", profitUSD);
        return profitMap;
    }


    // 매도 발생 시 실현 손익 계산
    private void realisedCalc(int tripId, String stockCode, int quantity){
        boolean isUs = stockCode.chars().allMatch(Character::isAlphabetic);

        String currency = isUs ? "USD" : "KRW";
        String key = "trip:" + tripId + "realisedProfit:" + currency;

        String value = redisTemplate.opsForValue().get(key);
        BigDecimal realisedProfit = (value != null) ? new BigDecimal(value) : BigDecimal.ZERO;

        BigDecimal currencyPrice = new BigDecimal(getStockPrice(stockCode));
        BigDecimal avgPrice = new BigDecimal(getAvgPrice(tripId, stockCode));

        BigDecimal profit = currencyPrice.subtract(avgPrice).multiply(new BigDecimal(quantity));
        BigDecimal update = realisedProfit.add(profit);

        redisTemplate.opsForValue().set(key, update.toString());

        System.out.println("✅ realisedProfit 업데이트 완료!");
        System.out.println("기존 realisedProfit: " + value + " → 새로운 realisedProfit: " + update);
        System.out.println("currencyPrice: " + currencyPrice + " | avgPrice: " + avgPrice + " | profit: " + profit);

    }

    // 평단가 가져오기
    private String getAvgPrice(int tripId, String stockCode){
        String key = "trip:" + tripId + ":stock:" + stockCode;
        String field = "average_price";

        Object avgPriceObj = redisTemplate.opsForHash().get(key, field);
        String avgPrice = avgPriceObj.toString();
        return avgPrice;
    }

    // 전날 realisedProfit 가져오기
    @Transactional
    public void getAllUserRealisedProfit(){
        ResponseEntity<Response<List<TripGoalResponseDTO>>> responseEntity = tripClient.getAllTrips();
        List<TripGoalResponseDTO> allTripGoals = responseEntity.getBody().getData();

        for(TripGoalResponseDTO tripGoalResponseDTO : allTripGoals){
            Integer tripId = tripGoalResponseDTO.getId();

            BigDecimal realisedProfitKRW = tripGoalResponseDTO.getRealisedProfit() != null ? tripGoalResponseDTO.getRealisedProfit() : BigDecimal.ZERO;
            BigDecimal realisedProfitUSD = tripGoalResponseDTO.getRealisedProfitUs() != null ? tripGoalResponseDTO.getRealisedProfitUs() : BigDecimal.ZERO;

            String cacheKeyKRW = "trip:" + tripId + "realisedProfit:KRW";
            String cacheKeyUSD = "trip:" + tripId + "realisedProfit:USD";

            ValueOperations<String, String> ops = redisTemplate.opsForValue();
            ops.set(cacheKeyKRW, realisedProfitKRW.toString());
            ops.set(cacheKeyUSD, realisedProfitUSD.toString());

            System.out.println("여행 목표 ID " + tripId + " | realisedProfit (USD): " + realisedProfitUSD + " 저장 완료!");
            System.out.println("여행 목표 ID " + tripId + " | realisedProfit (KRW): " + realisedProfitKRW + " 저장 완료!");
        }
        System.out.println("모든 사용자 실현 손익 저장 완료!");
    }

    // 장 마감 후 realisedProfit + Profit저장
    @Transactional
    public void storeAllUserProfit() {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        Set<String> keys = redisTemplate.keys("trip:*realisedProfit:*");
        logger.info("🔍 Redis keys 조회 결과: {}", keys);

        Map<Integer, BigDecimal> realisedProfitKRWMap = new HashMap<>();
        Map<Integer, BigDecimal> realisedProfitUSDMap = new HashMap<>();

        for (String key : keys) {
            if (key.contains("realisedProfit:KRW")) {
                String tripIdStr = key.replace("trip:", "").replace("realisedProfit:KRW", "");
                try {
                    Integer tripId = Integer.parseInt(tripIdStr);
                    BigDecimal value = Optional.ofNullable(ops.get(key))
                            .map(BigDecimal::new)
                            .orElse(BigDecimal.ZERO);
                    realisedProfitKRWMap.put(tripId, value);
                } catch (NumberFormatException e) {
                    logger.error("TripId 파싱 실패 for key: {}", key, e);
                }
            } else if (key.contains("realisedProfit:USD")) {
                String tripIdStr = key.replace("trip:", "").replace("realisedProfit:USD", "");
                try {
                    Integer tripId = Integer.parseInt(tripIdStr);
                    BigDecimal value = Optional.ofNullable(ops.get(key))
                            .map(BigDecimal::new)
                            .orElse(BigDecimal.ZERO);
                    realisedProfitUSDMap.put(tripId, value);
                } catch (NumberFormatException e) {
                    logger.error("TripId 파싱 실패 for key: {}", key, e);
                }
            }
        }

        Set<Integer> tripIds = new HashSet<>();
        tripIds.addAll(realisedProfitKRWMap.keySet());
        tripIds.addAll(realisedProfitUSDMap.keySet());
        logger.info("🔍 추출된 여행 목표 IDs: {}", tripIds);

        for (Integer tripId : tripIds) {
            BigDecimal realisedProfitKRW = realisedProfitKRWMap.getOrDefault(tripId, BigDecimal.ZERO);
            BigDecimal realisedProfitUSD = realisedProfitUSDMap.getOrDefault(tripId, BigDecimal.ZERO);

            Map<String, BigDecimal> unrealisedProfits = unrealisedCalc(tripId);
            BigDecimal unrealisedProfitKRW = unrealisedProfits.getOrDefault("profitKRW", BigDecimal.ZERO);
            BigDecimal unrealisedProfitUSD = unrealisedProfits.getOrDefault("profitUSD", BigDecimal.ZERO);

            BigDecimal totalProfitKRW = unrealisedProfitKRW.add(realisedProfitKRW);
            BigDecimal totalProfitUSD = unrealisedProfitUSD.add(realisedProfitUSD);

            logger.info("🚀 여행 목표 ID {} | KRW: unrealisedProfit: {}, realisedProfit: {}, totalProfit: {}",
                    tripId, unrealisedProfitKRW, realisedProfitKRW, totalProfitKRW);
            logger.info("🚀 여행 목표 ID {} | USD: unrealisedProfit: {}, realisedProfit: {}, totalProfit: {}",
                    tripId, unrealisedProfitUSD, realisedProfitUSD, totalProfitUSD);

            ResponseEntity<Response<TripGoalResponseDTO>> response1 = tripClient.updateRealisedProfit(
                    TripGoalUpdateDTO.toDTO(tripId, realisedProfitKRW, realisedProfitUSD));

            ExchangeRateChartDTO.ExchangeRateData rateData = exchangeRateChartService.getUSExchangeRate();

            ResponseEntity<Response<TripGoalResponseDTO>> response2 = tripClient.updateProfit(
                    TripGoalProfitUpdateDTO.toDTO(tripId, totalProfitKRW, totalProfitUSD,rateData.getRate()));

            if (response1.getStatusCode().is2xxSuccessful() && response2.getStatusCode().is2xxSuccessful()) {
                logger.info("✅ 여행 목표 ID {} 저장 완료!", tripId);
                redisTemplate.delete("trip:" + tripId + "realisedProfit:KRW");
                redisTemplate.delete("trip:" + tripId + "realisedProfit:USD");
            } else {
                logger.error("❌ 여행 목표 ID {} 저장 실패. 응답 상태: {}, {}",
                        tripId, response1.getStatusCode(), response2.getStatusCode());
            }
        }
        logger.info("🎉 모든 realisedProfit DB 저장 완료!");
    }

    // 평가 손익 redis 계산
    public void getMtmProfit(int tripId) {
        // 해당 계좌의 모든 주식의 (현재가 - 평단가) * 수량 합

    }

    // 매수 매도 발생 시 평가 손익 계산
    public void calcMtmProfit(int tripId) {
        BigDecimal totalMtmProfitUSD = BigDecimal.ZERO;
        BigDecimal totalMtmProfitKRW = BigDecimal.ZERO;

        String pattern = "trip:" + tripId + ":stock:*";
        Set<String> keys = redisTemplate.keys(pattern);
        logger.info("trip id: " + tripId + " 조회된 redis 키 목록 : " + keys);

        for(String key : keys){
            String stockCode = key.split(":")[3];
            Map<Object, Object> stockMap = redisTemplate.opsForHash().entries(key);

            BigDecimal avgPrice = new BigDecimal(stockMap.get("average_price").toString());
            int quantity = Integer.parseInt(stockMap.get("total_quantity").toString());
            BigDecimal currentPrice = new BigDecimal(getStockPrice(stockCode));
            logger.info("currentPrice : " + currentPrice);

            BigDecimal mtmProfit = currentPrice.subtract(avgPrice).multiply(new BigDecimal(quantity));

            if(Character.isAlphabetic(stockCode.charAt(0))) {
                totalMtmProfitUSD = totalMtmProfitUSD.add(mtmProfit);
            } else {
                totalMtmProfitKRW = totalMtmProfitKRW.add(mtmProfit);
            }

            logger.info("✅ Trip ID " + tripId + "의 평가 손익 계산 완료!");
            logger.info("USD 평가 손익: " + totalMtmProfitUSD);
            logger.info("KRW 평가 손익: " + totalMtmProfitKRW);

        }
    }
}
