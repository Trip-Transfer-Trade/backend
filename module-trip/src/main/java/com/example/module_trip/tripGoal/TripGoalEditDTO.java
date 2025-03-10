package com.example.module_trip.tripGoal;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class TripGoalEditDTO {
    private final Integer tripId;
    private final String country;
    private final BigDecimal goalAmount;
    private final String endDate;
    private final String name;
}
