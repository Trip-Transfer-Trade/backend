package com.example.module_exchange.redisData.exchangeData;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class USExChangeRateDTO {
    @JsonProperty("tts")
    private String exchangeRate;

    @Builder
    public USExChangeRateDTO(String exchangeRate) {
        this.exchangeRate = exchangeRate;
    }
}
