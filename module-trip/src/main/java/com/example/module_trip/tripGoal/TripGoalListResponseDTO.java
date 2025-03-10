package com.example.module_trip.tripGoal;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class TripGoalListResponseDTO {
    private String name;
    private String country;
    private LocalDate endDate;
    private BigDecimal profit;
    private BigDecimal profitUs;

    @Builder
    public TripGoalListResponseDTO(String name, String country, LocalDate endDate, BigDecimal profit, BigDecimal profitUs) {
        this.name = name;
        this.country = country;
        this.endDate = endDate;
        this.profit = (profit != null) ? profit : BigDecimal.ZERO;
        this.profitUs = (profitUs != null) ? profitUs : BigDecimal.ZERO;

    }

    public static TripGoalListResponseDTO toDTO(TripGoal tripGoal) {
        return TripGoalListResponseDTO.builder()
                .name(tripGoal.getName())
                .country(tripGoal.getCountry())
                .endDate(tripGoal.getEndDate())
                .profit(tripGoal.getProfit())
                .profitUs(tripGoal.getProfitUs())
                .build();
    }
}

