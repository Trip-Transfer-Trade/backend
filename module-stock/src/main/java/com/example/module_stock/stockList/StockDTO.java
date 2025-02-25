package com.example.module_stock.stockList;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StockDTO {
    @JsonProperty("data_rank")
    private int rank;

    @JsonProperty("hts_kor_isnm")
    private String name;

    @JsonProperty("mksc_shrn_iscd")
    private String ticker;

    @JsonProperty("stck_prpr")
    private Long currentPrice;

    @JsonProperty("prdy_vrss_sign")
    private int priceChangeSign;

    @JsonProperty("prdy_ctrt")
    private String priceChangeRate;
}
