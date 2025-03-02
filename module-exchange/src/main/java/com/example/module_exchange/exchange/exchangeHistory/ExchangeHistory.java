package com.example.module_exchange.exchange.exchangeHistory;

import com.example.module_exchange.exchange.exchangeCurrency.ExchangeCurrency;
import com.example.module_utility.entity.BaseEntity;
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
    @JoinColumn(name="exchange_currency_id",nullable = false)
    private ExchangeCurrency exchangeCurrency;

}
