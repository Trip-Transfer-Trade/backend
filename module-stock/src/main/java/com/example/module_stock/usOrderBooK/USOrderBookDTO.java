package com.example.module_stock.usOrderBooK;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class USOrderBookDTO {

    @JsonProperty("output1")
    private USStockPriceDTO priceUS;

    @JsonProperty("output2")
    private USOrderBookDetailDTO usDetail;
}
