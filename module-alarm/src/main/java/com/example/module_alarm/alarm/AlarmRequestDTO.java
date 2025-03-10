package com.example.module_alarm.alarm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlarmRequestDTO {
    @Builder.Default
    private Integer userId=0;
    @Builder.Default
    private String tripName="";
    private AlarmType type;
    @Builder.Default
    private String rate="";

    public static AlarmHistory toEntity(AlarmRequestDTO alarmRequestDTO) {
        return AlarmHistory.builder()
                .tripName(alarmRequestDTO.getTripName())
                .userId(alarmRequestDTO.getUserId())
                .type(alarmRequestDTO.getType())
                .build();
    }
}
