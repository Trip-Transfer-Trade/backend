package com.example.module_exchange.exchange.transactionHistory;

import com.example.module_trip.tripGoal.TripGoal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripGoalDTO {
    private String name;

    public static TripGoalDTO toDTO(TripGoal tripGoal) {
        return TripGoalDTO.builder()
                .name(tripGoal.getName())
                .build();
    }
}
