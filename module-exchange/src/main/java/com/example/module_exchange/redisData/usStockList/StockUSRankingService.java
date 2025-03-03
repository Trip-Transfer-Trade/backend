package com.example.module_exchange.redisData.usStockList;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

@Service
public class StockUSRankingService {
    private static final Logger logger = LoggerFactory.getLogger(StockUSRankingService.class);

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

    public StockUSRankingService(RestTemplate restTemplate, ObjectMapper objectMapper, RedisTemplate<String, String> redisTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
    }

    public StockUSRankingDTO getStockUSRanking(String type) {
        System.out.println(type);
        String cacheKey = "stock_us_ranking:" + type;
        ValueOperations<String, String> ops = redisTemplate.opsForValue();

        // redis 데이터 조회
        String value = ops.get(cacheKey);
        if(value != null){
            try{
                logger.info("redis 응답");
                return objectMapper.readValue(value, StockUSRankingDTO.class);
            } catch (Exception e) {
                logger.error("US Redis 캐시 변환 오류");
            }
        }

        // 한투 API 호출
        String url = getUrl(type);
        String queryParam = getQueryParam(type);

        HttpHeaders headers = new HttpHeaders();
        headers.set("authorization", "Bearer " + accessToken);
        headers.set("content-type", "application/json");
        headers.set("appkey", appKey);
        headers.set("appsecret", appSecret);
        headers.set("tr_id", "HHDFS76270000");
        headers.set("custtype", "P");

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url + queryParam, HttpMethod.GET, entity, String.class);

        logger.info("한투 API 응답: {}", response.getBody());

        try{
            StockUSRankingDTO result = objectMapper.readValue(response.getBody(), StockUSRankingDTO.class);
            ops.set(cacheKey, objectMapper.writeValueAsString(result), 1000, TimeUnit.SECONDS);

            return result;
        } catch (Exception e) {
            logger.error("API 호출 실패", e);
            return new StockUSRankingDTO();
        }
    }

    private String getUrl(String type){
        return switch (type) {
            case "top", "low" -> apiUrl + "/uapi/overseas-stock/v1/ranking/updown-rate";
            case "volume" -> apiUrl + "/uapi/overseas-stock/v1/ranking/trade-vol";
            case "popular" -> apiUrl + "/uapi/overseas-stock/v1/ranking/volume-power";
            default -> throw new IllegalArgumentException("Invalid API type: " + type);
        };
    }

    private String getQueryParam(String type){
        return switch (type) {
            case "top" -> "?AUTH&EXCD=NAS&GUBN=1&NDAY=0&VOL_RANG=2&KEYB";
            case "low" -> "?AUTH&EXCD=NAS&GUBN=0&NDAY=0&VOL_RANG=2&KEYB";
            case "volume" -> "?AUTH&EXCD=NAS&NDAY=0&PRC1=0&PRC2=0&VOL_RANG=0&KEYB";
            case "popular" -> "?AUTH&EXCD=NAS&NDAY=0&VOL_RANG=2&KEYB";
            default -> throw new IllegalArgumentException("Invalid API type: " + type);
        };
    }
}
