package com.example.module_trip.tripGoal;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class TripGoalResponseDTO {
    private Integer id;
    private String name;
    private String country;
    private BigDecimal goalAmount;
    private BigDecimal profit;

    private int accountId;


    @Builder
    public TripGoalResponseDTO(Integer id, String name, String country, BigDecimal goalAmount, BigDecimal profit, int accountId) {
        this.id = id;
        this.name = name;
        this.country = country;
        this.goalAmount = goalAmount;
        this.profit = profit;
        this.accountId = accountId;

    }

    public static TripGoalResponseDTO toDTO(TripGoal tripGoal) {
        return TripGoalResponseDTO.builder()
                .id(tripGoal.getId())
                .name(tripGoal.getName())
                .country(tripGoal.getCountry())
                .goalAmount(tripGoal.getGoalAmount())
                .profit(tripGoal.getProfit())
                .accountId(tripGoal.getAccount().getId())
                .build();
    }
}