package com.example.module_stock.exchange;

import com.example.module_stock.exchange.exchangeRateChart.ExchangeRateChartDTO;
import com.example.module_stock.exchange.exchangeRateChart.ExchangeRateChartService;
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

    @GetMapping("/rate")
    public ExchangeRateListDTO getExchangeRate() {
        return exchangeRateService.getExchangeRate();
    }

    @PostMapping("/save")
    public String saveExchangeRate() {
        exchangeRateChartService.saveExchangeRateChart();
        return "success";
    }

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
