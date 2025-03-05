package com.example.module_exchange.redisData.stockList;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/api/exchanges")
public class StockController {

    private final StockRankingService tradingVolumeService;

    public StockController(StockRankingService tradingVolumeService) {
        this.tradingVolumeService = tradingVolumeService;
    }

    private static final Set<String> VALID_TYPES = Set.of("top", "low", "popular", "volume");

    @GetMapping("/ranking")
    public StockRankingDTO getStockRanking(@RequestParam String type) {
        if (VALID_TYPES.contains(type)) {
            return tradingVolumeService.getStockRanking(type);
        }
        throw new IllegalArgumentException("Invalid type. Use 'volume'.");
    }
}
