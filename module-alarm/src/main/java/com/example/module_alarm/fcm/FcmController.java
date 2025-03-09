package com.example.module_alarm.fcm;

import com.example.module_utility.response.Response;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/alarms/fcm")
public class FcmController {
    private final FcmService fcmService;

    @PostMapping("")
    public ResponseEntity saveToken(@RequestBody FcmTokenDTO request) {
        if (request.getUserId() == null || request.getToken()==null) {
            return ResponseEntity.badRequest().body("userId or token is null");
        }
        Response<Void> response = fcmService.saveToken(request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}
