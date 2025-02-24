package com.example.module_stock.stockList;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

@Service
public class StockRankingService {
    private static final Logger logger = LoggerFactory.getLogger(StockRankingService.class);

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

    public StockRankingService(RestTemplate restTemplate, ObjectMapper objectMapper, RedisTemplate<String, String> redisTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
    }

    public StockRankingDTO getStockRanking(String type) {
        String cacheKey = "stock_ranking:" + type;
        ValueOperations<String, String> ops = redisTemplate.opsForValue();

        // redis 에서 데이터 조회
        String value = ops.get(cacheKey);
        if (value != null) {
            try{
                return objectMapper.readValue(value, StockRankingDTO.class);
            } catch (Exception e){
                logger.error("Redis 캐시 변환 오류");
            }
        }

        // 한투 API 호출
        String url = apiUrl + "/uapi/domestic-stock/v1/quotations/volume-rank";
        String queryParam = "?FID_COND_MRKT_DIV_CODE=J"
                + "&FID_COND_SCR_DIV_CODE=20171"
                + "&FID_INPUT_ISCD=0002"
                + "&FID_DIV_CLS_CODE=0"
                + "&FID_BLNG_CLS_CODE=0"
                + "&FID_TRGT_CLS_CODE=111111111"
                + "&FID_TRGT_EXLS_CLS_CODE=000000"
                + "&FID_INPUT_PRICE_1=0"
                + "&FID_INPUT_PRICE_2=0"
                + "&FID_VOL_CNT=0"
                + "&FID_INPUT_DATE_1=0";

        HttpHeaders headers = new HttpHeaders();
        headers.set("authorization", "Bearer " + accessToken);
        headers.set("content-type", "application/json");
        headers.set("appkey", appKey);
        headers.set("appsecret", appSecret);
        headers.set("tr_id", "FHPST01710000");

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url + queryParam, HttpMethod.GET, entity, String.class);

        logger.info("한투 API 응답: {}", response.getBody());

        try {
            StockRankingDTO result = objectMapper.readValue(response.getBody(), StockRankingDTO.class);
            ops.set(cacheKey, objectMapper.writeValueAsString(result), 10, TimeUnit.SECONDS);

            return result;
        } catch (Exception e) {
            logger.error("API 호출 실패", e);
            return new StockRankingDTO();
        }
    }
}
