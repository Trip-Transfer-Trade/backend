package com.example.module_alarm.alarm;

import lombok.Builder;
import lombok.Getter;

@Getter
public class AlarmResponseDTO {
    private AlarmType alarmType;
    private Integer tripId;
    private boolean isRead;

    @Builder
    public AlarmResponseDTO(AlarmType alarmType, Integer tripId, boolean isRead) {
        this.alarmType = alarmType;
        this.tripId = tripId;
        this.isRead = isRead;
    }
    public static AlarmResponseDTO toDTO(AlarmHistory alarmHistory) {
        return AlarmResponseDTO.builder()
                .alarmType(alarmHistory.getAlarmType())
                .tripId(alarmHistory.getTripId())
                .isRead(alarmHistory.isRead())
                .build();
    }
}
