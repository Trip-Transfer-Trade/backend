package com.example.module_exchange.exchange.transactionHistory;

import com.example.module_exchange.exchange.TransactionCategory;
import com.example.module_exchange.exchange.exchangeCurrency.ExchangeCurrency;
import com.example.module_utility.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionHistory extends BaseEntity {

    @Id
    @GeneratedValue
    private Integer id;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    private TransactionCategory transactionCategory;

    private BigDecimal transactionAmount;
    private String description;
    private String targetAccountNumber;

    @ManyToOne
    @JoinColumn(name="exchange_currency_id",nullable = false)
    private ExchangeCurrency exchangeCurrency;
}
