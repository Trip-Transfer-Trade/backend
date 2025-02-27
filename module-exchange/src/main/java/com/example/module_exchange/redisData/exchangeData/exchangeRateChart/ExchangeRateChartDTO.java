package com.example.module_exchange.redisData.exchangeData.exchangeRateChart;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRateChartDTO {
    private String code;
    private List<ExchangeRateData> rates;

    @Getter
    @AllArgsConstructor
    public static class ExchangeRateData {
        private String date;
        private String rate;
    }

}
