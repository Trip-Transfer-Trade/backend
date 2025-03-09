package com.example.module_exchange.redisData.exchangeData.exchangeRateChart;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Service
public class ExchangeRateChartService {
    private static final Logger logger = LoggerFactory.getLogger(ExchangeRateChartService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${AUTH_KEY}")
    private String authKey;

    public ExchangeRateChartService(RestTemplate restTemplate, ObjectMapper objectMapper, RedisTemplate<String, String> redisTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
    }

    public void saveExchangeRateChart() {
        String today = getDate(0);
        String lastDay = getDate(365);

        String referenceDate = today;
        LocalDate now = LocalDate.now();

        if (now.getDayOfWeek() == DayOfWeek.SATURDAY) {
            referenceDate = getDate(1);
        } else if (now.getDayOfWeek() == DayOfWeek.SUNDAY) {
            referenceDate = getDate(2);
        }

        String url = "https://www.koreaexim.go.kr/site/program/financial/exchangeJSON";
        String queryParam = "?authkey=" + authKey
                + "&searchdate=" + referenceDate
                + "&data=AP01";

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url + queryParam, HttpMethod.GET, entity, String.class);

        logger.info("한국수출입은행 API 응답: {}", response.getBody());

        try{
            List<ExchangeRateSaveDTO> rates = objectMapper.readValue(response.getBody(), new TypeReference<List<ExchangeRateSaveDTO>>() {});
            ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();

            for(ExchangeRateSaveDTO rate : rates){
                String code = rate.getCode();
                String exchangeRate = rate.getExchangeRate();

                String cacheKey = "exchangeRate:" + code;
                String redisValue = today + ":" + exchangeRate;

                zSetOperations.add(cacheKey, redisValue, Double.parseDouble(today));

                zSetOperations.removeRangeByScore(cacheKey, 0, Double.parseDouble(lastDay));

                logger.info("저장 완료");
            }

        } catch (Exception e) {
            logger.error("환율 데이터 저장 오류");
        }
    }

    public ExchangeRateChartDTO getExchangeRateChart(String code, int days) {
        ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();

        String cacheKey = "exchangeRate:" + code;
        List<ExchangeRateChartDTO.ExchangeRateData> exchangeRateDataList = new ArrayList<>();
        String lastRate = null;

        for(int i = 0; i < days; i++){
            String date = getDate(i);
            Set<String> rateData = zSetOperations.rangeByScore(cacheKey, Double.parseDouble(date), Double.parseDouble(date));

            if(rateData == null || rateData.isEmpty()){
                if(lastRate != null){
                    exchangeRateDataList.add(new ExchangeRateChartDTO.ExchangeRateData(date, lastRate));
                }
            } else {
                String[] parts = rateData.iterator().next().split(":");
                exchangeRateDataList.add(new ExchangeRateChartDTO.ExchangeRateData(parts[0], parts[1]));
            }
        }

        return new ExchangeRateChartDTO(code, exchangeRateDataList);
    }

    public ExchangeRateChartDTO.ExchangeRateData getUSExchangeRate() {
        ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
        String cacheKey = "exchangeRate:USD";
        Set<String> latestRateData = zSetOperations.reverseRange(cacheKey, 0, 0);

        if (latestRateData == null || latestRateData.isEmpty()) {
            return null; // 데이터가 없을 경우 null 반환
        }

        String latestData = latestRateData.iterator().next();
        String[] parts = latestData.split(":");

        return new ExchangeRateChartDTO.ExchangeRateData(parts[0], parts[1]);
    }

    // 1년 환율 데이터 저장 함수 -> 초기 실행 후 사용 안 함
    public void saveTestData() {
        ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();

        for (int i = 0; i < 365; i++) {
            String date = getDate(i);
            String lastDay = getDate(365);

            String url = "https://www.koreaexim.go.kr/site/program/financial/exchangeJSON";
            String queryParam = "?authkey=" + authKey + "&searchdate=" + date + "&data=AP01";

            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url + queryParam, HttpMethod.GET, entity, String.class);

            logger.info("한국수출입은행 API 응답: {}", response.getBody());

            try {
                List<ExchangeRateSaveDTO> rates = objectMapper.readValue(response.getBody(), new TypeReference<List<ExchangeRateSaveDTO>>() {});

                for (ExchangeRateSaveDTO rate : rates) {
                    String code = rate.getCode();
                    String exchangeRate = rate.getExchangeRate();

                    String cacheKey = "exchangeRate:" + code;
                    String redisValue = date + ":" + exchangeRate;

                    zSetOperations.add(cacheKey, redisValue, Double.parseDouble(date));

                    zSetOperations.removeRangeByScore(cacheKey, 0, Double.parseDouble(lastDay));
                }

                logger.info("{} 날짜의 환율 데이터 저장 완료", date);
            } catch (Exception e) {
                logger.error("{} 날짜의 환율 데이터 저장 오류", date, e);
            }
        }
    }

    private String getDate(int daysAgo){
        return LocalDate.now().minusDays(daysAgo).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    private String getAdjustedDate(){
        LocalDate date = LocalDate.now();
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        if(dayOfWeek == DayOfWeek.SATURDAY){
            date = date.minusDays(1);
        } else if(dayOfWeek == DayOfWeek.SUNDAY){
            date = date.minusDays(2);
        }

        return date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }
}
