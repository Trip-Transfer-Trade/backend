package com.example.module_exchange.exchange.stockTradeHistory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockHoldingsDTO {
    private List<StockDetailDTO> stockHoldings;
}
