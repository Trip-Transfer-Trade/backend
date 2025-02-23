package com.example.module_exchange.exchange;

import com.example.module_utility.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor
public class ExchangeHistory extends BaseEntity {

    @Id
    @GeneratedValue
    private int id;
    private String fromCurrency;
    private String toCurrency;
    private BigDecimal fromAmount;
    private BigDecimal toAmount;
    private BigDecimal exchangeRate;

    @ManyToOne
    @JoinColumn(name = "wallet_id", nullable = false)
    ExchangeWallet exchangeWallet;

}

