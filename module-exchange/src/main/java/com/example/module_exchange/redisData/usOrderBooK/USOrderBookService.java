package com.example.module_exchange.redisData.usOrderBooK;

import com.example.module_exchange.redisData.orderBook.OrderBookService;
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
public class USOrderBookService {
    private static final Logger logger = LoggerFactory.getLogger(OrderBookService.class);

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

    public USOrderBookService(RestTemplate restTemplate, ObjectMapper objectMapper, RedisTemplate<String, String> redisTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
    }

    public USOrderBookDTO getUSOrderBook(String code) {
        System.out.println(code);

        String cacheKey = "stock_us_order_book_" + code;
        ValueOperations<String, String> ops = redisTemplate.opsForValue();

        // redis 데이터 조회
        String value = ops.get(cacheKey);
        if (value != null) {
            try{
                return objectMapper.readValue(value, USOrderBookDTO.class);
            } catch (Exception e) {
                logger.error("Redis 캐시 변환 오류");
            }
        }

        // 한투 API 호출
        String url = apiUrl + "/uapi/overseas-price/v1/quotations/inquire-asking-price";
        String queryParam = "?AUTH=" + "&EXCD=NAS" + "&SYMB=" + code;

        HttpHeaders headers = new HttpHeaders();
        headers.set("authorization", "Bearer " + accessToken);
        headers.set("content-type", "application/json");
        headers.set("appkey", appKey);
        headers.set("appsecret", appSecret);
        headers.set("tr_id", "HHDFS76200100");
        headers.set("custtype", "P");

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url + queryParam, HttpMethod.GET, entity, String.class);

        logger.info("한투 API 응답: {}", response.getBody());

        try{
            USOrderBookDTO result = objectMapper.readValue(response.getBody(), USOrderBookDTO.class);
            ops.set(cacheKey, objectMapper.writeValueAsString(result), 1000, TimeUnit.SECONDS);
            return result;
        } catch (Exception e) {
            logger.error("API 호출 실패", e);
            return new USOrderBookDTO();
        }

    }
}
