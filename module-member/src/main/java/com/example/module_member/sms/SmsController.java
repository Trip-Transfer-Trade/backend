package com.example.module_member.sms;

import com.example.module_utility.response.Response;
import lombok.AllArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members/")
@AllArgsConstructor
public class SmsController {
    final SmsService smsService;

    @PostMapping("/send")
    public ResponseEntity<Response<String>> send(@RequestBody String phoneNumber) {
        String code = smsService.sendSms(phoneNumber);
        return ResponseEntity.ok(Response.success(code));
    }
}
