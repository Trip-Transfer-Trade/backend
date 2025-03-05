package com.example.module_trip.tripGoal;

import com.example.module_trip.account.Account;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class TripGoalResponseDTO {
    private Integer id;
    private String name;
    private String country;
    private BigDecimal goalAmount;
    private BigDecimal profit;
    private BigDecimal realisedProfit;
    private LocalDate endDate;

    private int accountId;


    @Builder
    public TripGoalResponseDTO(Integer id, String name, String country, BigDecimal goalAmount, BigDecimal profit, BigDecimal realisedProfit, LocalDate endDate,int accountId) {
        this.id = id;
        this.name = name;
        this.country = country;
        this.goalAmount = goalAmount;
        this.profit = profit;
        this.realisedProfit = realisedProfit;
        this.endDate = endDate;
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
                .realisedProfit(tripGoal.getRealisedProfit())
                .endDate(tripGoal.getEndDate())
                .build();
    }

    public TripGoal toEntity(Account account) {
        return TripGoal.builder()
                .name(name)
                .country(country)
                .goalAmount(goalAmount)
                .profit(profit)
                .account(account)
                .realisedProfit(realisedProfit)
                .endDate(endDate)
                .build();
    }
}