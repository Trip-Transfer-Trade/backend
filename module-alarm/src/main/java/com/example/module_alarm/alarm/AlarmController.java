package com.example.module_alarm.alarm;

import com.example.module_utility.response.Response;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/alarms")
public class AlarmController {

    private final AlarmService alarmService;

    @PostMapping("")
    public ResponseEntity sendAlarm(@RequestBody AlarmRequestDTO alarmRequestDTO) {
        alarmService.sendAlarm(alarmRequestDTO);
        return ResponseEntity.ok().build();
    }

    @GetMapping("")
    public ResponseEntity<List<AlarmResponseDTO>> findAlarmByUserId(@RequestParam final Integer userId) {
        Response<List<AlarmResponseDTO>> response = alarmService.findAlarmByUserId(userId);
        return ResponseEntity.ok(response.getData());
    }

    @PatchMapping("/{alarmId}")
    public ResponseEntity updateAlarm(@PathVariable Integer alarmId) {
        Response response = alarmService.updateAlarmReadStatus(alarmId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PatchMapping("")
    public ResponseEntity updateAllAlarm(@RequestParam final Integer userId) {
        Response response = alarmService.updateAllAlarmReadStatus(userId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}
