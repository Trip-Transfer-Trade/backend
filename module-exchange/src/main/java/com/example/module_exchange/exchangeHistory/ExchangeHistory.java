package com.example.module_exchange.exchangeHistory;

import com.example.module_exchange.exchangeWallet.ExchangeWallet;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String fromCurrency;
    private BigDecimal fromAmount;
    private String toCurrency;
    private BigDecimal toAmount;
    private BigDecimal exchangeRate;

    @ManyToOne
    @JoinColumn(name = "wallet_id", referencedColumnName = "id", nullable = false)
    private ExchangeWallet wallet;

}
