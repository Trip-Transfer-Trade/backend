package com.example.module_exchange.redisData.usStockList;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StockUSDTO {
    @JsonProperty("rank")
    private int rank;

    @JsonProperty("knam")
    private String name;

    @JsonProperty("symb")
    private String ticker;

    @JsonProperty("last")
    private String currentPrice;

    @JsonProperty("rate")
    private String priceChangeRate;
}
