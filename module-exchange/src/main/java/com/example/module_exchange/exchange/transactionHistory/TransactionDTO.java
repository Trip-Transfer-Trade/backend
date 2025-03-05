package com.example.module_exchange.exchange.transactionHistory;

import com.example.module_exchange.exchange.TransactionCategory;
import com.example.module_exchange.exchange.exchangeCurrency.ExchangeCurrency;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class TransactionDTO {
    private Integer accountId;
    private BigDecimal amount;
    private String targetAccountNumber;
    private String currencyCode;

    public TransactionHistory toTransactionHistory(ExchangeCurrency exchangeCurrency,TransactionType transactionType, String description) {
        return TransactionHistory.builder()
                .transactionAmount(amount)
                .transactionType(transactionType)
                .transactionCategory(TransactionCategory.BASIC)
                .targetAccountNumber(targetAccountNumber)
                .description(description)
                .exchangeCurrency(exchangeCurrency)
                .build();
    }
}
