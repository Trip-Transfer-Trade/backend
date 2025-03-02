package com.example.module_exchange.redisData.exchangeData.exchangeRateChart;

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
