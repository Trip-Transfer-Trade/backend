package com.example.module_exchange.exchange.exchangeCurrency;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class ExchangeGoalListDTO {
    Map<String,ExchangeGoalResult> exchanges;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ExchangeGoalResult{
        private BigDecimal amount;
        private String rate;
        private BigDecimal toAmount;
    }

}


