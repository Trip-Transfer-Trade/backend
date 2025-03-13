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
    public ResponseEntity<Response<Void>> saveToken(@RequestHeader("X-Authenticated-User") int userId, @RequestBody String token) {
        if (token==null) {
            return ResponseEntity.badRequest().body(Response.error(400,"token is null"));
        }
        Response<Void> response = fcmService.saveToken(new FcmTokenDTO(userId,token));
        return ResponseEntity.ok(response);
    }
}
