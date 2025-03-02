package com.example.module_exchange.redisData.exchangeData;

import com.example.module_exchange.redisData.exchangeData.exchangeRateChart.ExchangeRateChartDTO;
import com.example.module_exchange.redisData.exchangeData.exchangeRateChart.ExchangeRateChartService;
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
    public ExchangeRateListDTO getExchangeRate() {
        return exchangeRateService.getExchangeRate();
    }

    // 매일 새로운 환율 저장 -> scheduler 설정해둠
    @PostMapping("/save")
    public String saveExchangeRate() {
        exchangeRateChartService.saveExchangeRateChart();
        return "success";
    }

    // 환율 차트 데이터 조회
    @GetMapping("/chart")
    public ExchangeRateChartDTO getExchangeRateChart(@RequestParam String code, @RequestParam int days) {
        return exchangeRateChartService.getExchangeRateChart(code, days);
    }

    // 1년 환율 데이터 저장 함수 -> 초기 실행 후 사용 안 함
    @PostMapping("/test")
    public String saveTestExchangeRate() {
        exchangeRateChartService.saveTestData();
        return "success";
    }
}
