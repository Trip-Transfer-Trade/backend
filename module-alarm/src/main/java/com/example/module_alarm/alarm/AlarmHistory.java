package com.example.module_alarm.alarm;

import com.example.module_utility.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class AlarmHistory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private boolean isRead;
    private Integer userId;
    private String tripName;

    @Enumerated(EnumType.STRING)
    private AlarmType alarmType;

    @Builder
    public AlarmHistory(Integer userId,String tripName, AlarmType type) {
        this.userId = userId;
        this.tripName = tripName;
        this.alarmType = type;
        this.isRead = false;
    }

    public void updateReadStatus() {
        this.isRead = true;
    }

}

