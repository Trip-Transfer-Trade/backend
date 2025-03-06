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

    public static TripGoalUpdateDTO fromRedisKey(String redisKey){
        String[] parts = redisKey.split("realisedProfit:");
        BigDecimal realisedProfit = new BigDecimal(parts[1]);
        Integer tripId = Integer.parseInt(parts[0].split(":")[1]);
        return new TripGoalUpdateDTO(tripId, realisedProfit);
    }
}
