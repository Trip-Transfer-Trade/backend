package com.example.module_exchange.redisData.exchangeData;
import com.fasterxml.jackson.core.type.TypeReference;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class ExchangeRateService {
    private static final Logger logger = LoggerFactory.getLogger(ExchangeRateService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${AUTH_KEY}")
    private String authKey;

    public ExchangeRateService(RestTemplate restTemplate, ObjectMapper objectMapper, RedisTemplate<String, String> redisTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
    }

    public ExchangeRateListDTO getExchangeRate() {
        int todayDate = 0;
        int yesterdayDate = 1;

        DayOfWeek dayOfWeek = LocalDate.now().getDayOfWeek();
        LocalTime now = LocalTime.now();

        logger.info("Current Day: {}", dayOfWeek);
        logger.info("Current Time: {}", now);

        if(dayOfWeek == DayOfWeek.SATURDAY) {
            todayDate = 1;
            yesterdayDate = 2;
        } else if(dayOfWeek == DayOfWeek.SUNDAY) {
            todayDate = 2;
            yesterdayDate = 3;
        }

        if (dayOfWeek == DayOfWeek.MONDAY && now.isBefore(LocalTime.of(10, 0))) {
            todayDate = 3;
            yesterdayDate = 4;
        } else if (dayOfWeek == DayOfWeek.TUESDAY && now.isBefore(LocalTime.of(10, 0))) {
            todayDate = 1;
            yesterdayDate = 4;
        } else if (now.isBefore(LocalTime.of(10, 0))) {
            todayDate = 1;
            yesterdayDate = 2;
        }

        ExchangeRateListDTO today = getRedisAPI(getDate(todayDate));
        ExchangeRateListDTO yesterday = getRedisAPI(getDate(yesterdayDate));

        logger.info("Today rates: {}", today.getRates());
        logger.info("Yesterday rates: {}", yesterday.getRates());

        calculate(today, yesterday);

        return today;
    }

    private String getDate(int daysAgo){
        logger.info("Current Day: {}", LocalDate.now().minusDays(daysAgo).format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        return LocalDate.now().minusDays(daysAgo).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    private ExchangeRateListDTO getRedisAPI(String date){
        String cacheKey = "exchangeRate:" + date;
        ValueOperations<String, String> ops = redisTemplate.opsForValue();

        // redis 에서 데이터 조회
        String value = ops.get(cacheKey);
        if (value != null) {
            try{
                return objectMapper.readValue(value, ExchangeRateListDTO.class);
            } catch (Exception e) {
                logger.error("Redis 캐시 변환 오류");
            }
        }

        // 한국수출입은행 환율 API 호출
        String url = "https://www.koreaexim.go.kr/site/program/financial/exchangeJSON";
        String queryParam = "?authkey=" + authKey
                + "&searchdate=" + date
                + "&data=AP01";

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url + queryParam, HttpMethod.GET, entity, String.class);

        logger.info("한국수출입은행 API 응답: {}", response.getBody());

        try{
            List<ExchangeRateDTO> rates = objectMapper.readValue(response.getBody(), new TypeReference<List<ExchangeRateDTO>>() {});

            ExchangeRateListDTO result = new ExchangeRateListDTO(rates);
            ops.set(cacheKey, objectMapper.writeValueAsString(result), 10, TimeUnit.SECONDS);

            return result;
        } catch (Exception e) {
            return new ExchangeRateListDTO();
        }

    }

    // 변동 가격 계산
    private void calculate(ExchangeRateListDTO today, ExchangeRateListDTO yesterday) {
        Map<String, ExchangeRateDTO> yesterdayMap = new HashMap<>();
        for (ExchangeRateDTO rate : yesterday.getRates()) {
            yesterdayMap.put(rate.getName(), rate);
        }

        for (ExchangeRateDTO todayRate : today.getRates()) {
            ExchangeRateDTO yesterdayRate = yesterdayMap.get(todayRate.getName());
            if (yesterdayRate != null) {
                try {
                    BigDecimal todayExchangeRate = new BigDecimal(todayRate.getExchangeRate().replace(",", ""));
                    BigDecimal yesterdayExchangeRate = new BigDecimal(yesterdayRate.getExchangeRate().replace(",", ""));

                    BigDecimal changePrice = todayExchangeRate.subtract(yesterdayExchangeRate)
                            .setScale(2, RoundingMode.HALF_UP);

                    BigDecimal changeRate = BigDecimal.ZERO;

                    if (yesterdayExchangeRate.compareTo(BigDecimal.ZERO) != 0) {
                        changeRate = changePrice.divide(yesterdayExchangeRate, 4, RoundingMode.HALF_UP)
                                .multiply(new BigDecimal(100))
                                .setScale(2, RoundingMode.HALF_UP);
                    }

                    todayRate.setChangePrice(changePrice.doubleValue());
                    todayRate.setChangeRate(changeRate.doubleValue());

                } catch (NumberFormatException e) {
                    logger.error("환율 변환 오류: todayRate={}, yesterdayRate={}", todayRate.getExchangeRate(), yesterdayRate.getExchangeRate(), e);
                }
            }
        }
    }
}