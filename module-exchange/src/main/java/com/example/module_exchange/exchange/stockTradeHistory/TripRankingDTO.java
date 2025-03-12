package com.example.module_exchange.exchange.stockTradeHistory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@AllArgsConstructor
@Builder
public class TripRankingDTO {
    private Integer tripId;
    private BigDecimal assessmentAmountSum;
}
