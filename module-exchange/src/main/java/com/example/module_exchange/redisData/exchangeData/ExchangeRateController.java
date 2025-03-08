package com.example.module_exchange.redisData.exchangeData;

import com.example.module_exchange.redisData.exchangeData.exchangeRateChart.ExchangeRateChartDTO;
import com.example.module_exchange.redisData.exchangeData.exchangeRateChart.ExchangeRateChartService;
import com.example.module_utility.response.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/exchanges")
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;
    private final ExchangeRateChartService exchangeRateChartService;

    public ExchangeRateController(ExchangeRateService exchangeRateService, ExchangeRateChartService exchangeRateChartService) {
        this.exchangeRateService = exchangeRateService;
        this.exchangeRateChartService = exchangeRateChartService;
    }

    // 통화 23개 당일 환율 조회
    @GetMapping("/rate")
    public ResponseEntity<Response<ExchangeRateListDTO>> getExchangeRate() {
        ExchangeRateListDTO response = exchangeRateService.getExchangeRate();
        return ResponseEntity.ok(Response.success(response));
    }

    // 매일 새로운 환율 저장 -> scheduler 설정해둠
    @PostMapping("/save")
    public ResponseEntity<Response<Void>> saveExchangeRate() {
        exchangeRateChartService.saveExchangeRateChart();
        return ResponseEntity.ok(Response.successWithoutData());
    }

    @GetMapping("/rate/us")
    public ExchangeRateChartDTO.ExchangeRateData getUSExchangeRate(){
        return exchangeRateChartService.getUSExchangeRate();
    }

    // 환율 차트 데이터 조회
    @GetMapping("/chart")
    public ResponseEntity<Response<ExchangeRateChartDTO>> getExchangeRateChart(@RequestParam String code, @RequestParam int days) {
        return ResponseEntity.ok(Response.success(exchangeRateChartService.getExchangeRateChart(code, days)));
    }

    // 1년 환율 데이터 저장 함수 -> 초기 실행 후 사용 안 함
    @PostMapping("/test")
    public ResponseEntity<Response<Void>> saveTestExchangeRate() {
        exchangeRateChartService.saveTestData();
        return ResponseEntity.ok(Response.successWithoutData());
    }



}
