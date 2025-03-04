package com.example.module_exchange.exchange.transactionHistory;

import com.example.module_exchange.exchange.TransactionCategory;
import com.example.module_exchange.exchange.exchangeCurrency.ExchangeCurrency;
import com.example.module_utility.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class TransactionHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
