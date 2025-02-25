package com.example.module_stock.orderBook;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StockPriceDTO {
    @JsonProperty("stck_prpr")
    private String currencyPrice;

    @JsonProperty("antc_cntg_vrss_sign")
    private int priceChangeSign;

    @JsonProperty("antc_cntg_prdy_ctrt")
    private String priceChangeRate;
}
