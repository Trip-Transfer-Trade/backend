package com.example.module_exchange.exchange.exchangeCurrency;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyWalletDTO {
    private String currencyCode;
    private BigDecimal totalAmount;
}
