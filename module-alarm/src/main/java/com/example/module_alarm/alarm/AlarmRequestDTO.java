package com.example.module_alarm.alarm;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AlarmRequestDTO {
    private Integer userId;
    private String tripName="";
    private AlarmType type;

    public AlarmHistory toEntity(){
        return AlarmHistory.builder()
                .tripName(tripName)
                .userId(userId)
                .type(type)
                .build();
    }
}
