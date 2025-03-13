package com.example.module_alarm.application;

import com.example.module_alarm.alarm.AlarmRequestDTO;
import com.example.module_alarm.alarm.AlarmService;
import com.example.module_alarm.alarm.AlarmType;
import com.example.module_trip.tripGoal.TripGoalAlarmDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ForextAlertConsumer {
    private final AlarmService alarmService;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "queue.forex.alert")
    public void processForexAlert(String rate) {
        log.info("RabbitMQ에서 최저 환율 값 수신 : {}",rate);

        alarmService.sendAlarm(AlarmRequestDTO.builder()
                        .rate(rate)
                        .type(AlarmType.LOWEST_EXCHANGE_RATE)
                .build());
    }

    @RabbitListener(queues = "queue.goal.alert")
    public void processGoalAlert(String json) {
        TripGoalAlarmDTO tripGoalAlarmDTO = null;
        try {
            tripGoalAlarmDTO = objectMapper.readValue(json, TripGoalAlarmDTO.class);
            log.info("목표 도달 알림 tripGoalAlarm={}", tripGoalAlarmDTO.getTripName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        AlarmType type=AlarmType.GOAL_ACHIEVED;

        if (tripGoalAlarmDTO.getType()=="end"){
            type=AlarmType.GOAL_FAILED;
        }
        alarmService.sendAlarm(AlarmRequestDTO.builder()
                .tripName(tripGoalAlarmDTO.getTripName())
                .userId(tripGoalAlarmDTO.getUserId())
                .type(type).build());
    }

    @RabbitListener(queues = "queue.half.alert")
    public void processHalfAlert(String json) {
        TripGoalAlarmDTO tripGoalAlarmDTO = null;
        try{
            tripGoalAlarmDTO = objectMapper.readValue(json, TripGoalAlarmDTO.class);
            log.info("user={} 목표 기간 대비 수익 50% 미달 알림 tripGoalAlarm={}",tripGoalAlarmDTO.getUserId(), tripGoalAlarmDTO.getTripName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        alarmService.sendAlarm(AlarmRequestDTO.builder()
                .tripName(tripGoalAlarmDTO.getTripName())
                .userId(tripGoalAlarmDTO.getUserId())
                .type(AlarmType.GOAL_HALF_FAILED).build());
    }
}
