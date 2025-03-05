package com.example.module_alarm.alarm;

import com.example.module_utility.response.Response;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/alarms")
public class AlarmController {

    private final AlarmService alarmService;

    @PostMapping("")
    public ResponseEntity<Response<Void>> sendAlarm(@RequestBody AlarmRequestDTO alarmRequestDTO) {
        alarmService.sendAlarm(alarmRequestDTO);
        return ResponseEntity.ok(Response.successWithoutData());
    }

    @GetMapping("")
    public ResponseEntity<Response<List<AlarmResponseDTO>>> findAlarmByUserId(@RequestParam final Integer userId) {
        List<AlarmResponseDTO> response = alarmService.findAlarmByUserId(userId);
        return ResponseEntity.ok(Response.success(response));
    }

    @PatchMapping("/{alarmId}")
    public ResponseEntity<Response> updateAlarm(@PathVariable Integer alarmId) {
        Response response = alarmService.updateAlarmReadStatus(alarmId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PatchMapping("")
    public ResponseEntity<Response<Void>> updateAllAlarm(@RequestParam final Integer userId) {
        alarmService.updateAllAlarmReadStatus(userId);
        Response<Void> response = Response.successWithoutData();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("")
    public ResponseEntity<Response<Void>> deleteAllAlarm(@RequestParam final Integer userId) {
        alarmService.deleteAllAlarm(userId);
        return ResponseEntity.ok(Response.successWithoutData());
    }

    @DeleteMapping("/{alarmId}")
    public ResponseEntity<Response<Void>> deleteAlarmById(@PathVariable final Integer alarmId) {
        alarmService.deleteAlarmById(alarmId);
        return ResponseEntity.ok(Response.successWithoutData());
    }
}
