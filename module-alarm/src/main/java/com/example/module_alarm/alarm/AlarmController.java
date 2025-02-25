package com.example.module_alarm.alarm;

import com.google.firebase.messaging.FirebaseMessagingException;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/alarm")
public class AlarmController {

    private final AlarmService alarmService;

    @PostMapping("")
    public ResponseEntity sendAlarm(@RequestBody AlarmRequestDTO alarmRequestDTO) {
        alarmService.sendAlarm(alarmRequestDTO);
        return ResponseEntity.ok().build();
    }
}
