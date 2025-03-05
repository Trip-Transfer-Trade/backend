package com.example.module_exchange.exchange.exchangeCurrency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;


@Getter
@Builder
@AllArgsConstructor
public class WalletResponseDTO {
    private int accountId;
    private BigDecimal amount; // BigDecimal로 변경
    private String currencyCode;

    public static WalletResponseDTO toDto(ExchangeCurrency exchangeCurrency){
        return WalletResponseDTO.builder()
                .accountId(exchangeCurrency.getAccountId())
                .currencyCode(exchangeCurrency.getCurrencyCode())
                .amount(exchangeCurrency.getAmount())
                .build();
    }

}