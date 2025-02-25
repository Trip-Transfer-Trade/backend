package com.example.module_alarm.alarm;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class AlarmHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private boolean isRead;
    private Integer userId;
    private Integer tripId;

    @Enumerated(EnumType.STRING)
    private AlarmType alarmType;

    @Builder
    public AlarmHistory(Integer userId, Integer tripId, AlarmType type) {
        this.userId = userId;
        this.tripId = tripId;
        this.alarmType = type;
        this.isRead = false;
    }

}

