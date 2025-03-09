package com.example.module_alarm.application;

import com.example.module_alarm.alarm.AlarmRequestDTO;
import com.example.module_alarm.alarm.AlarmService;
import com.example.module_alarm.alarm.AlarmType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ForextAlertConsumer {
    private final AlarmService alarmService;

    @RabbitListener(queues = "queue.forex.alert")
    public void processForexAlert(String rate) {
        log.info("RabbitMQ에서 최저 환율 값 수신 : {}",rate);

        alarmService.sendAlarm(AlarmRequestDTO.builder()
                        .rate(rate)
                        .type(AlarmType.LOWEST_EXCHANGE_RATE)
                .build());
    }
}
