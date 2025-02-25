package com.example.module_exchange.exchange.stockTradeHistory;

import com.example.module_exchange.exchange.exchangeCurrency.ExchangeCurrency;
import com.example.module_utility.entity.BaseEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
public class StockTradeHistory extends BaseEntity {

    @Id
    @GeneratedValue
    private Integer id;

    @Enumerated(EnumType.STRING)
    private TradeType tradeType;
    private String stockCode;
    private Integer quantity;
    private BigDecimal pricePerUnit;
    private BigDecimal totalPrice;

    @ManyToOne
    @JoinColumn(name="exchange_currency_id",nullable = false)
    private ExchangeCurrency exchangeCurrency;

}
