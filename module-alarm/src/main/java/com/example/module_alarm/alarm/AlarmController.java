package com.example.module_alarm.alarm;

import com.example.module_utility.response.Response;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/{userId}")
    public ResponseEntity<List<AlarmResponseDTO>> findAlarmByUserId(@PathVariable Integer userId) {
        Response<List<AlarmResponseDTO>> response = alarmService.findAlarmByUserId(userId);
        return ResponseEntity.ok(response.getData());
    }
}
