package com.example.module_stock.orderBook;

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
public class OrderBookService {
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

    public OrderBookService(RestTemplate restTemplate, ObjectMapper objectMapper, RedisTemplate<String, String> redisTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
    }

    public OrderBookDTO getOrderBook(String code) {

        System.out.println(code);

        String cacheKey = "stock_order_book_" + code;
        ValueOperations<String, String> ops = redisTemplate.opsForValue();

        // redis 에서 데이터 조회
        String value = ops.get(cacheKey);
        if(value != null) {
            try {
                return objectMapper.convertValue(value, OrderBookDTO.class);
            } catch (Exception e) {
                logger.error("Redis 캐시 변환 오류");
            }
        }

        // 한투 API 호출
        String url = apiUrl + "/uapi/domestic-stock/v1/quotations/inquire-asking-price-exp-ccn";
        String queryParam = "?fid_cond_mrkt_div_code=J"
                + "&fid_input_iscd=" + code;

        HttpHeaders headers = new HttpHeaders();
        headers.set("authorization", "Bearer " + accessToken);
        headers.set("content-type", "application/json");
        headers.set("appkey", appKey);
        headers.set("appsecret", appSecret);
        headers.set("tr_id", "FHKST01010200");

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url + queryParam, HttpMethod.GET, entity, String.class);

        logger.info("한투 API 응답: {}", response.getBody());

        try {
            OrderBookDTO result = objectMapper.readValue(response.getBody(), OrderBookDTO.class);
            ops.set(cacheKey, objectMapper.writeValueAsString(result), 10, TimeUnit.SECONDS);
            return result;
        } catch (Exception e) {
            logger.error("API 호출 실패", e);
            return new OrderBookDTO();
        }
    }
}
