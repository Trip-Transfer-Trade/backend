package com.example.module_exchange.exchange.exchangeCurrency;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class TripExchangeCurrencyDTO {
    private String name;
    private String country;
    private LocalDate endDate;
    private BigDecimal totalAmountInKRW; // 통합 예수금
    private BigDecimal totalProfit;  //누적수익금(원화 + 달러)

    public TripExchangeCurrencyDTO(String name, String country, LocalDate endDate, BigDecimal totalAmountInKRW, BigDecimal totalProfit) {
        this.name = name;
        this.country = country;
        this.endDate = endDate;
        this.totalAmountInKRW = totalAmountInKRW;
        this.totalProfit= totalProfit;
    }
}
