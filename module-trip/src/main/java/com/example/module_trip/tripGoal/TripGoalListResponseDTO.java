package com.example.module_trip.tripGoal;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class TripGoalListResponseDTO {

    private Integer id;
    private String name;
    private String country;
    private BigDecimal goalAmount;
    private LocalDate endDate;
    private BigDecimal profit;
    private BigDecimal profitUs;

    @Builder
    public TripGoalListResponseDTO(Integer id, String name, String country, BigDecimal goalAmount, LocalDate endDate, BigDecimal profit, BigDecimal profitUs) {
        this.id = id;
        this.name = name;
        this.country = country;
        this.goalAmount = goalAmount;
        this.endDate = endDate;
        this.profit = (profit != null) ? profit : BigDecimal.ZERO;
        this.profitUs = (profitUs != null) ? profitUs : BigDecimal.ZERO;

    }

    public static TripGoalListResponseDTO toDTO(TripGoal tripGoal) {
        return TripGoalListResponseDTO.builder()
                .id(tripGoal.getId())
                .name(tripGoal.getName())
                .country(tripGoal.getCountry())
                .goalAmount(tripGoal.getGoalAmount())
                .endDate(tripGoal.getEndDate())
                .profit(tripGoal.getProfit())
                .profitUs(tripGoal.getProfitUs())
                .build();
    }
}

