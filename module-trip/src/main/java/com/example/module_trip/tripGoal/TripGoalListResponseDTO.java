package com.example.module_trip.tripGoal;

import lombok.AllArgsConstructor;
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
    private String accountNumber;
    private BigDecimal goalAmount;
    private LocalDate endDate;
    private BigDecimal profit; //원화 누적 수익금
    private BigDecimal profitUs; //달러 누적 수익금

    @Builder
    public TripGoalListResponseDTO(Integer id, String name, String accountNumber, String country, BigDecimal goalAmount, LocalDate endDate, BigDecimal profit, BigDecimal profitUs) {
        this.id = id;
        this.name = name;
        this.accountNumber = accountNumber;
        this.country = country;
        this.goalAmount = goalAmount;
        this.endDate = endDate;
        this.profit = (profit != null) ? profit : BigDecimal.ZERO;
        this.profitUs = (profitUs != null) ? profitUs : BigDecimal.ZERO;

    }


    @Builder
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

    @Builder
    public static TripGoalListResponseDTO toDTO(TripGoal tripGoal, String accountNumber) {
        return TripGoalListResponseDTO.builder()
                .id(tripGoal.getId())
                .name(tripGoal.getName())
                .accountNumber(accountNumber)
                .country(tripGoal.getCountry())
                .goalAmount(tripGoal.getGoalAmount())
                .endDate(tripGoal.getEndDate())
                .profit(tripGoal.getProfit())
                .profitUs(tripGoal.getProfitUs())
                .build();
    }

}

