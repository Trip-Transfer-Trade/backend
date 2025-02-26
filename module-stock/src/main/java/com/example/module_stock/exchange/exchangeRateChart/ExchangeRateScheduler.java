package com.example.module_stock.exchange.exchangeRateChart;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ExchangeRateScheduler {

    private final ExchangeRateChartService exchangeRateChartService;

    public ExchangeRateScheduler(ExchangeRateChartService exchangeRateChartService) {
        this.exchangeRateChartService = exchangeRateChartService;
    }

    @Scheduled(cron = "0 0 9 * * *")
    public void schedule() {
        exchangeRateChartService.saveExchangeRateChart();
    }
}
