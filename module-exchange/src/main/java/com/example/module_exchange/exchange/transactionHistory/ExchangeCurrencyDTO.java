package com.example.module_exchange.exchange.transactionHistory;

import com.example.module_exchange.exchange.exchangeCurrency.ExchangeCurrency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeCurrencyDTO {
    private String currencyCode;
    private BigDecimal amount;

    public static ExchangeCurrencyDTO toDTO(ExchangeCurrency exchangeCurrency) {
        return ExchangeCurrencyDTO.builder()
                .currencyCode(exchangeCurrency.getCurrencyCode())
                .amount(exchangeCurrency.getAmount())
                .build();
    }
}
