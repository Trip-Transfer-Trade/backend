package com.example.module_exchange.exchange;

import com.example.module_utility.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor
public class ExchangeCurrency extends BaseEntity {

    @Id
    @GeneratedValue
    private Integer id;

    private String currencyCode;
    private BigDecimal ammount;

    @ManyToOne
    @JoinColumn(name = "wallet_id", nullable = false)
    ExchangeWallet exchangeWallet;

}
