package com.example.module_trip.tripGoal;

import com.example.module_trip.account.Account;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class TripGoalRequestDTO {

    private Integer id;
    private String name;
    private String country;
    private BigDecimal goalAmount;
    private BigDecimal profit;

    private int accountId;

    public TripGoal toEntity(Account account) {
        return TripGoal.builder()
                .name(name)
                .country(country)
                .goalAmount(goalAmount)
                .profit(profit)
                .account(account)
                .build();
    }
}
