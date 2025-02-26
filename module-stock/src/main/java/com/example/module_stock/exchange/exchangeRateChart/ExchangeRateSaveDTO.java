package com.example.module_stock.exchange.exchangeRateChart;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ExchangeRateSaveDTO {
    @JsonProperty("cur_nm")
    private String name;

    @JsonProperty("cur_unit")
    private String code;

    @JsonProperty("tts")
    private String exchangeRate;


}
