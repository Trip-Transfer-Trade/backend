package com.example.module_exchange.exchange.stockTradeHistory;

import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;

public interface StockTradeHistoryRepository extends JpaRepository<StockTradeHistory, Integer> {

    @Query("SELECT SUM(s.quantity) " +
            "FROM StockTradeHistory s " +
            "WHERE s.tradeType = 'BUY' AND s.stockCode = :stockCode")
    BigDecimal findTotalBuyQuantityByStockCode(@Param("stockCode") String stockCode);


    @Query("SELECT SUM(s.quantity) " +
            "FROM StockTradeHistory s " +
            "WHERE s.tradeType = 'SELL' AND s.stockCode = :stockCode")
    BigDecimal findTotalSellQuantityByStockCode(@Param("stockCode") String stockCode);

    //통화에 해당하는 Buy 한 것들 다 더함.
    @Query("SELECT COALESCE(SUM(s.totalPrice), 0) " +
            "FROM StockTradeHistory s " +
            "WHERE s.tradeType = 'BUY' AND s.exchangeCurrency.currencyCode = :currencyCode " +
            "AND s.exchangeCurrency.accountId = :accountId")
    BigDecimal findTotalBuyAmountByAccountAndCurrency(@Param("accountId") Integer accountId,
                                                      @Param("currencyCode") String currencyCode);
    //통화에 해당하는 SELL 한 것들 다 더함.
    @Query("SELECT COALESCE(SUM(s.totalPrice), 0) " +
            "FROM StockTradeHistory s " +
            "WHERE s.tradeType = 'SELL' AND s.exchangeCurrency.currencyCode = :currencyCode " +
            "AND s.exchangeCurrency.accountId = :accountId")
    BigDecimal findTotalSellAmountByAccountAndCurrency(@Param("accountId") Integer accountId,
                                                       @Param("currencyCode") String currencyCode);

}