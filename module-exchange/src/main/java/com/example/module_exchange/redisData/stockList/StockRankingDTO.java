package com.example.module_exchange.redisData.stockList;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class StockRankingDTO {
    @JsonProperty("output")
    private List<StockDTO> output;
}
