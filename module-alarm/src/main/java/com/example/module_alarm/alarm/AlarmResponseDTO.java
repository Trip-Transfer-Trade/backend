package com.example.module_alarm.alarm;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AlarmResponseDTO {
    private Integer alarmId;
    private AlarmType alarmType;
    private String tripName="";
    private boolean isRead;

    @Builder
    public AlarmResponseDTO(Integer alarmId, AlarmType alarmType, String tripName, boolean isRead) {
        this.alarmId = alarmId;
        this.alarmType = alarmType;
        this.tripName = tripName;
        this.isRead = isRead;
    }
    public static AlarmResponseDTO toDTO(AlarmHistory alarmHistory) {
        return AlarmResponseDTO.builder()
                .alarmId(alarmHistory.getId())
                .alarmType(alarmHistory.getAlarmType())
                .tripName(alarmHistory.getTripName())
                .isRead(alarmHistory.isRead())
                .build();
    }
}
