package com.example.module_exchange.redisData.stockList;

import com.fasterxml.jackson.annotation.JsonCreator;
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

    private String ticker;

    @JsonProperty("stck_prpr")
    private Long currentPrice;

    @JsonProperty("prdy_vrss_sign")
    private int priceChangeSign;

    @JsonProperty("prdy_ctrt")
    private String priceChangeRate;

    @JsonCreator
    public StockDTO(
            @JsonProperty("data_rank") int rank,
            @JsonProperty("hts_kor_isnm") String name,
            @JsonProperty("mksc_shrn_iscd") String mkscTicker,
            @JsonProperty("stck_shrn_iscd") String stckTicker, // 추가
            @JsonProperty("stck_prpr") Long currentPrice,
            @JsonProperty("prdy_vrss_sign") int priceChangeSign,
            @JsonProperty("prdy_ctrt") String priceChangeRate
    ) {
        this.rank = rank;
        this.name = name;
        this.ticker = (mkscTicker != null) ? mkscTicker : stckTicker;
        this.currentPrice = currentPrice;
        this.priceChangeSign = priceChangeSign;
        this.priceChangeRate = priceChangeRate;
    }
}
