package com.example.module_stock.usStockList;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class StockUSRankingDTO {
    @JsonProperty("output2")
    private List<StockUSDTO> output;
}
