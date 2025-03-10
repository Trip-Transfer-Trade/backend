package com.example.module_trip.tripGoal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TripGoalUpdateDTO {
    private Integer tripGoalId;
    private BigDecimal realisedProfit;
    private BigDecimal realisedProfitUs;

    public static TripGoalUpdateDTO toDTO(Integer tripGoalId, BigDecimal realisedProfit,BigDecimal realisedProfitUs) {
        return new TripGoalUpdateDTO(tripGoalId, realisedProfit, realisedProfitUs);
    }
}
