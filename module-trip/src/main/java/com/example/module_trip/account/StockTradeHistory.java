package com.example.module_trip.account;

import com.example.module_utility.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor
public class StockTradeHistory extends BaseEntity{

    @Id
    @GeneratedValue
    private Integer id;

    private TradeType tradeType;
    private String stockCode;
    private int quantity;
    private BigDecimal pricePerUnit;
    private BigDecimal totalPrice;

    @ManyToOne
    @JoinColumn(name="account_id", nullable = false)
    private Account account;

}
