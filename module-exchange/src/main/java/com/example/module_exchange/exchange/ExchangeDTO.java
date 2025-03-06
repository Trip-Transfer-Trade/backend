package com.example.module_exchange.exchange;

import com.example.module_exchange.exchange.exchangeCurrency.ExchangeCurrency;
import com.example.module_exchange.exchange.exchangeHistory.ExchangeHistory;
import com.example.module_exchange.exchange.exchangeHistory.ExchangeType;
import com.example.module_exchange.exchange.transactionHistory.TransactionHistory;
import com.example.module_exchange.exchange.transactionHistory.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@Builder
public class ExchangeDTO {

    private Integer accountId;
    private BigDecimal fromAmount; // 현재 환전하고 싶은 금액 (환전 전 금액)
    private String fromCurrency;   // 현재 환전하고 싶은 금액에 해당하는 통화 코드 (환전 전 통화 코드)
    private String toCurrency;     // 환전하고 싶은 통화 (환전 후 통화 코드)
    private BigDecimal toAmount;   // 환전 후 금액
    private BigDecimal exchangeRate; // 적용된 환율

    public ExchangeHistory toExchangeHistory(ExchangeCurrency exchangeCurrency, ExchangeType exchangeType, BigDecimal amount) {
        return ExchangeHistory.builder()
                .amount(amount)
                .exchangeRate(exchangeRate)
                .exchangeType(exchangeType)
                .exchangeCurrency(exchangeCurrency)
                .build();
    }
    public TransactionHistory toTransactionHistory(ExchangeCurrency exchangeCurrency, TransactionType transactionType, BigDecimal TransactionAmount) {
        return TransactionHistory.builder()
                .transactionType(transactionType)
                .transactionCategory(TransactionCategory.EXCHANGE)
                .transactionAmount(TransactionAmount)
                .exchangeCurrency(exchangeCurrency)
                .build();
    }



}
