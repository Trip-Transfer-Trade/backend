package com.example.module_trip.tripGoal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.math.BigDecimal;

@Builder
@AllArgsConstructor
@Getter
public class TripGoalProfitUpdateDTO implements Serializable {
    private Integer tripGoalId;
    private BigDecimal profit;
    private BigDecimal profitUs;
    private String rate;

    public static TripGoalProfitUpdateDTO toDTO(Integer tripGoalId, BigDecimal profit, BigDecimal profitUs, String rate) {
        return TripGoalProfitUpdateDTO.builder()
                .tripGoalId(tripGoalId)
                .profit(profit)
                .profitUs(profitUs)
                .rate(rate)
                .build();
    }

    @Override
    public String toString() {
        return "TripGoalProfitUpdateDTO{" +
                "tripGoalId=" + tripGoalId +
                ", profit=" + profit +
                ", profitUs=" + profitUs +
                '}';
    }
}
