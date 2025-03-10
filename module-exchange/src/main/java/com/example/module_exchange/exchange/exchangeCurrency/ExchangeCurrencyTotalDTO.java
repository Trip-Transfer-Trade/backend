package com.example.module_exchange.exchange.exchangeCurrency;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class ExchangeCurrencyTotalDTO {

    private String amountNumber;
    private BigDecimal totalAmountInKRW;

    public ExchangeCurrencyTotalDTO(String amountNumber, BigDecimal totalAmountInKRW) {
        this.amountNumber = amountNumber;
        this.totalAmountInKRW = totalAmountInKRW;
    }


}
