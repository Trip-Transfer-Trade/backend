package com.example.module_exchange.exchange.stockTradeHistory;

import com.example.module_exchange.exchange.TransactionCategory;
import com.example.module_exchange.exchange.exchangeCurrency.ExchangeCurrency;
import com.example.module_exchange.exchange.transactionHistory.TransactionHistory;
import com.example.module_exchange.exchange.transactionHistory.TransactionType;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class StockTradeDTO {
    private Integer tripId;
    private BigDecimal amount;
    private String currencyCode;
    private String stockCode;
    private Integer quantity;
    private BigDecimal pricePerUnit;
    private BigDecimal totalPrice;


    public StockTradeHistory toStockTradeHistory(ExchangeCurrency exchangeCurrency, TradeType tradeType) {
        return StockTradeHistory.builder()
                .tradeType(tradeType)
                .stockCode(stockCode)
                .quantity(quantity)
                .pricePerUnit(pricePerUnit)
                .totalPrice(totalPrice)
                .exchangeCurrency(exchangeCurrency)
                .build();
    }

    public StockTradeHistory toStockTradeHistory(ExchangeCurrency exchangeCurrency, TradeType tradeType,
                                                 String stockCode, Integer quantity, BigDecimal pricePerUnit, BigDecimal totalPrice) {
        return StockTradeHistory.builder()
                .tradeType(tradeType)
                .stockCode(stockCode)
                .quantity(quantity)
                .pricePerUnit(pricePerUnit)
                .totalPrice(totalPrice)
                .exchangeCurrency(exchangeCurrency)
                .build();
    }


    public TransactionHistory toTransactionHistory(ExchangeCurrency exchangeCurrency, TransactionType transactionType, BigDecimal TransactionAmount) {
        return TransactionHistory.builder()
                .transactionType(transactionType)
                .transactionCategory(TransactionCategory.STOCK_TRADE)
                .transactionAmount(TransactionAmount)
                .exchangeCurrency(exchangeCurrency)
                .build();
    }
}
