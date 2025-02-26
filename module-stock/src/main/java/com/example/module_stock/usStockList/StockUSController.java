package com.example.module_stock.usStockList;

import com.example.module_stock.stockList.StockRankingDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/api/stocks/us")
public class StockUSController {

    private final StockUSRankingService stockUSRankingService;

    public StockUSController(StockUSRankingService stockUSRankingService) {
        this.stockUSRankingService = stockUSRankingService;
    }

    private static final Set<String> VALID_TYPES = Set.of("top", "low", "popular", "volume");

    @GetMapping
    public StockUSRankingDTO getStockUSRanking(@RequestParam String type) {
        if (VALID_TYPES.contains(type)) {
            return stockUSRankingService.getStockUSRanking(type);
        }
        throw new IllegalArgumentException("Invalid type. Use 'volume'.");
    }
}
