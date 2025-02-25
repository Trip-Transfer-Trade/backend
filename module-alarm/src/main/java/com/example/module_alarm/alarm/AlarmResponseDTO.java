package com.example.module_alarm.alarm;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AlarmResponseDTO {
    private Integer alarmId;
    private AlarmType alarmType;
    private Integer tripId;
    private boolean isRead;

    @Builder
    public AlarmResponseDTO(Integer alarmId, AlarmType alarmType, Integer tripId, boolean isRead) {
        this.alarmId = alarmId;
        this.alarmType = alarmType;
        this.tripId = tripId;
        this.isRead = isRead;
    }
    public static AlarmResponseDTO toDTO(AlarmHistory alarmHistory) {
        return AlarmResponseDTO.builder()
                .alarmId(alarmHistory.getId())
                .alarmType(alarmHistory.getAlarmType())
                .tripId(alarmHistory.getTripId())
                .isRead(alarmHistory.isRead())
                .build();
    }
}
