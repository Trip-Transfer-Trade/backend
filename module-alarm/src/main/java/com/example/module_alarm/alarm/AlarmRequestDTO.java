package com.example.module_alarm.alarm;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AlarmRequestDTO {
    private Integer userId;
    private Integer tripId;
    private AlarmType type;

    public AlarmHistory toEntity(){
        return AlarmHistory.builder()
                .tripId(tripId)
                .userId(userId)
                .type(type)
                .build();
    }
}
