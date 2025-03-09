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

    public AlarmHistory toEntity(){
        return AlarmHistory.builder()
                .tripName(tripName)
                .userId(userId)
                .type(type)
                .build();
    }
}
