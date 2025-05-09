package com.example.module_trip.tripGoal;

import com.example.module_trip.account.Account;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class TripGoalRequestDTO {

    private Integer id;
    private String name;
    private String country;
    private BigDecimal goalAmount;
    private BigDecimal profit;
    private LocalDate endDate;

    private int accountId;

    public TripGoal toEntity() {
        return TripGoal.builder()
                .name(name)
                .country(country)
                .goalAmount(goalAmount)
                .endDate(endDate)
                .profit(profit)
                .build();
    }
}
