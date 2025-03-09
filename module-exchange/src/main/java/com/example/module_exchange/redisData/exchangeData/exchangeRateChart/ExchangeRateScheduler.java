package com.example.module_exchange.redisData.exchangeData.exchangeRateChart;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ExchangeRateScheduler {

    private final ExchangeRateChartService exchangeRateChartService;

    public ExchangeRateScheduler(ExchangeRateChartService exchangeRateChartService) {
        this.exchangeRateChartService = exchangeRateChartService;
    }

    @Scheduled(cron = "0 12 21 * * *")
    public void schedule() {
        exchangeRateChartService.saveExchangeRateChart();
        exchangeRateChartService.sendForexAlert();
    }

//    @Scheduled(cron = "0 */1 * * * *") //알림 test
//    public void schedule2() {
//        exchangeRateChartService.sendForexAlertTest();
//    }
}
