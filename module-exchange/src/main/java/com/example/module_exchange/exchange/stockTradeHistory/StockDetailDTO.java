package com.example.module_exchange.exchange.stockTradeHistory;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockDetailDTO {
    private String stockCode;
    private String quantity;
    private String avgPrice;

    private String stockName;
    private String currencyPrice;

    public static StockDetailDTO fromBasic(StockDetailDTO basic, String stockName, String currencyPrice){
        return StockDetailDTO.builder()
                .stockCode(basic.getStockCode())
                .quantity(basic.getQuantity())
                .avgPrice(basic.getAvgPrice())
                .stockName(stockName)
                .currencyPrice(currencyPrice)
                .build();
    }

}
