package com.example.module_exchange.exchange.stockTradeHistory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class ProfitScheduler {
    private static final Logger logger = LoggerFactory.getLogger(ProfitScheduler.class);
    private final StockTradeService stockTradeService;
    private final RedisTemplate redisTemplate;

    public ProfitScheduler(StockTradeService stockTradeService, RedisTemplate redisTemplate) {
        this.stockTradeService = stockTradeService;
        this.redisTemplate = redisTemplate;
    }

    @Scheduled(cron = "0 30 13 * * *")
    public void schedule() {
        logger.info("ProfitScheduler 실행됨");
        stockTradeService.getAllUserRealisedProfit();
    }
  
    @Scheduled(cron = "0 35 13 * * *")
    public void schedule2() {
        Set<String> keys = redisTemplate.keys("userProfit:*");
        if (keys != null) {
            stockTradeService.storeAllUserProfit();
            System.out.println("주식 장 마감 : Redis 데이터 삭제");
        }
        stockTradeService.checkGoal();
    }

//    @Scheduled(cron = "0 * * * * *") // 테스트 코드
//    public void scheduleTest(){
//        stockTradeService.getAllUserRealisedProfit();
//        Set<String> keys = redisTemplate.keys("userProfit:*");
//        if (keys != null) {
//            stockTradeService.storeAllUserProfit();
//            System.out.println("주식 장 마감 : Redis 데이터 삭제");
//        }
//    }
}
