package com.example.module_exchange.redisData.usOrderBooK;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
@NoArgsConstructor
public class USOrderBookDetailDTO {

    private Map<String, String> paskPrices = new HashMap<>();  //매도 호가
    private Map<String, String> pbidPrices = new HashMap<>();   //매수 호가
    private Map<String, String> vaskQuantities = new HashMap<>();  //매도 호가 잔량
    private Map<String, String> vbidQuantities = new HashMap<>();  //매수 호가 잔량

    @JsonAnySetter
    public void addDynamicPrice(String key, String value) {
        if (key.startsWith("pask")) {
            paskPrices.put(key, value);
        } else if (key.startsWith("pbid")) {
            pbidPrices.put(key, value);
        } else if (key.startsWith("vask")) {
            vaskQuantities.put(key, value);
        } else if (key.startsWith("vbid")) {
            vbidQuantities.put(key, value);
        }
    }
}
