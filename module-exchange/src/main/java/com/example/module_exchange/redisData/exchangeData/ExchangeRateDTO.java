package com.example.module_exchange.redisData.exchangeData;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
public class ExchangeRateDTO {

    @JsonProperty("cur_nm")
    private String name;

    @JsonProperty("tts")
    private String exchangeRate;

    @Setter
    private double changePrice;

    @Setter
    private double changeRate;

}

