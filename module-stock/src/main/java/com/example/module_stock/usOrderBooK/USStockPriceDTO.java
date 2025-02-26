package com.example.module_stock.usOrderBooK;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class USStockPriceDTO {
    @JsonProperty("last")
    private String currentPrice;

    @JsonProperty("base")
    private String previousPrice;
}
