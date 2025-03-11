package com.example.module_exchange.exchange.exchangeCurrency;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class TripExchangeCurrencyDTO {
    private Integer tripId;
    private String name;
    private String country;
    private BigDecimal goalAmount;
    private LocalDate endDate;
    private BigDecimal amount;
    private BigDecimal amountUS;
    private BigDecimal totalAmountInKRW; // 통합 예수금
    private BigDecimal profit;
    private BigDecimal profitUs;
    private BigDecimal totalProfit;  //누적수익금(원화 + 달러)

    public TripExchangeCurrencyDTO(Integer tripId, String name, String country, BigDecimal goalAmount, LocalDate endDate, BigDecimal amount, BigDecimal amountUS, BigDecimal totalAmountInKRW, BigDecimal profit, BigDecimal profitUs, BigDecimal totalProfit) {
        this.tripId = tripId;
        this.name = name;
        this.country = country;
        this.goalAmount = goalAmount;
        this.endDate = endDate;
        this.amount = amount;
        this.amountUS = amountUS;
        this.totalAmountInKRW = totalAmountInKRW;
        this.profit = profit;
        this.profitUs = profitUs;
        this.totalProfit= totalProfit;
    }
}
