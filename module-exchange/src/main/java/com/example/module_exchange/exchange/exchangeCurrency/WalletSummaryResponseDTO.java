package com.example.module_exchange.exchange.exchangeCurrency;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class WalletSummaryResponseDTO {
    private String currencyCode;
    private BigDecimal amount;

    public static WalletSummaryResponseDTO toDto(String currencyCode, BigDecimal amount) {
        return WalletSummaryResponseDTO.builder()
                .currencyCode(currencyCode)
                .amount(amount)
                .build();
    }
}
