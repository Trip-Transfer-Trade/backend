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
}