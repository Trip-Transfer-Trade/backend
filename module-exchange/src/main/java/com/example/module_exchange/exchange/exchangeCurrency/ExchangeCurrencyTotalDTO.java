package com.example.module_exchange.exchange.exchangeCurrency;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class ExchangeCurrencyTotalDTO {

    private Integer accountId;
    private String amountNumber;
    private BigDecimal amount;
    private BigDecimal amountUS;
    private BigDecimal totalAmountInKRW;

    public ExchangeCurrencyTotalDTO(Integer accountId, String amountNumber, BigDecimal amount, BigDecimal amountUS, BigDecimal totalAmountInKRW) {
        this.accountId = accountId;
        this.amountNumber = amountNumber;
        this.amount = amount;
        this.amountUS = amountUS;
        this.totalAmountInKRW = totalAmountInKRW;
    }


}
