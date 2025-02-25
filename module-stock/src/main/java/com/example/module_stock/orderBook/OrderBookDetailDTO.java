package com.example.module_stock.orderBook;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
@NoArgsConstructor
public class OrderBookDetailDTO {

    private Map<String, String> askPrices = new HashMap<>();
    private Map<String, String> bidPrices = new HashMap<>();
    private Map<String, String> askQuantities = new HashMap<>();
    private Map<String, String> bidQuantities = new HashMap<>();

    @JsonAnySetter
    public void setDynamicProperties(String key, String value) {
        if (key.startsWith("askp") && !key.contains("rsqn") && !key.contains("icdc")) {
            askPrices.put(key, value);
        } else if (key.startsWith("bidp") && !key.contains("rsqn") && !key.contains("icdc")) {
            bidPrices.put(key, value);
        } else if (key.startsWith("askp_rsqn") && !key.contains("icdc")) {
            askQuantities.put(key, value);
        } else if (key.startsWith("bidp_rsqn") && !key.contains("icdc")) {
            bidQuantities.put(key, value);
        }
    }
}
