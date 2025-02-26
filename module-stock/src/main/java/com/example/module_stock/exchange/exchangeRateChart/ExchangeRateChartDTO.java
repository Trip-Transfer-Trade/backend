package com.example.module_stock.exchange.exchangeRateChart;

import com.example.module_stock.exchange.ExchangeRateDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.units.qual.A;

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
