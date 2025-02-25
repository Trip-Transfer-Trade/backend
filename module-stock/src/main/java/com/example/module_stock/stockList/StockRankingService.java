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
        System.out.println(type);
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
        String url = getUrl(type);
        String queryParam = getQueryParam(type);

        HttpHeaders headers = new HttpHeaders();
        headers.set("authorization", "Bearer " + accessToken);
        headers.set("content-type", "application/json");
        headers.set("appkey", appKey);
        headers.set("appsecret", appSecret);

        if(type.equals("volume")){
            headers.set("tr_id", "FHPST01710000");
        }
        if(type.equals("popular")) {
            headers.set("tr_id", "FHPST01800000");
            headers.set("custtype", "P");
        }
        if(type.equals("top") || type.equals("low")) {
            headers.set("tr_id", "FHPST01700000");
            headers.set("custtype", "P");
        }

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

    private String getUrl(String type){
        return switch (type) {
            case "top", "low" -> apiUrl + "/uapi/domestic-stock/v1/ranking/fluctuation";
            case "volume" -> apiUrl + "/uapi/domestic-stock/v1/quotations/volume-rank";
            case "popular" -> apiUrl + "/uapi/domestic-stock/v1/ranking/top-interest-stock";
            default -> throw new IllegalArgumentException("Invalid API type: " + type);
        };
    }

    private String getQueryParam(String type){
        return switch (type) {
            case "top" -> "?fid_cond_mrkt_div_code=J"
                    + "&fid_cond_scr_div_code=20170"
                    + "&fid_input_iscd=0000"
                    + "&fid_rank_sort_cls_code=0"
                    + "&fid_input_cnt_1=0"
                    + "&fid_prc_cls_code=0"
                    + "&fid_input_price_1="
                    + "&fid_input_price_2="
                    + "&fid_vol_cnt="
                    + "&fid_trgt_cls_code=0"
                    + "&fid_trgt_exls_cls_code=0"
                    + "&fid_div_cls_code=0"
                    + "&fid_rsfl_rate1="
                    + "&fid_rsfl_rate2=";
            case "low" -> "?fid_cond_mrkt_div_code=J"
                    + "&fid_cond_scr_div_code=20170"
                    + "&fid_input_iscd=0000"
                    + "&fid_rank_sort_cls_code=1"
                    + "&fid_input_cnt_1=0"
                    + "&fid_prc_cls_code=0"
                    + "&fid_input_price_1="
                    + "&fid_input_price_2="
                    + "&fid_vol_cnt="
                    + "&fid_trgt_cls_code=0"
                    + "&fid_trgt_exls_cls_code=0"
                    + "&fid_div_cls_code=0"
                    + "&fid_rsfl_rate1="
                    + "&fid_rsfl_rate2=";
            case "volume" -> "?FID_COND_MRKT_DIV_CODE=J"
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
            case "popular" -> "?fid_cond_mrkt_div_code=J"
                    + "&fid_cond_scr_div_code=20180"
                    + "&fid_input_iscd=0000"
                    + "&fid_trgt_exls_cls_code=0"
                    + "&fid_trgt_cls_code=0"
                    + "&fid_input_price_1="
                    + "&fid_input_price_2="
                    + "&fid_vol_cnt="
                    + "&fid_div_cls_code=0"
                    + "&fid_input_iscd_2=000000"
                    + "&fid_input_cnt_1=1";
            default -> throw new IllegalArgumentException("Invalid API type: " + type);
        };
    }
}
