package com.example.module_trip.tripGoal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class TripGoalAlarmDTO {
    private String tripName;
    private Integer userId;
    private String type;

    public static TripGoalAlarmDTO toDTO(String name, Integer userId, String type) {
        return TripGoalAlarmDTO.builder()
                .tripName(name)
                .userId(userId)
                .type(type)
                .build();
    }
}
