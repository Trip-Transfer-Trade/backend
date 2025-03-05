package com.example.module_exchange.exchange.stockTradeHistory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ProfitScheduler {
    private static final Logger logger = LoggerFactory.getLogger(ProfitScheduler.class);
    private final StockTradeService stockTradeService;

    public ProfitScheduler(StockTradeService stockTradeService) {
        this.stockTradeService = stockTradeService;
    }

    @Scheduled(cron = "0 21 14 * * *")
    public void schedule() {
        logger.info("ProfitScheduler 실행됨");
        stockTradeService.storeAllUserProfit();
    }
}
