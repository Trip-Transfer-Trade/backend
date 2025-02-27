package com.example.module_exchange.redisData.exchangeData;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRateListDTO {
    private List<ExchangeRateDTO> rates;
}
