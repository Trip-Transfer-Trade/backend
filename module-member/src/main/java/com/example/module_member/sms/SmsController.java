package com.example.module_member.sms;

import com.example.module_member.dto.CheckRequestDTO;
import com.example.module_member.dto.SmsSendRequestDTO;
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
    public ResponseEntity<Response<String>> send(@RequestBody SmsSendRequestDTO smsSendRequestDTO) {
        String code = smsService.sendSms(smsSendRequestDTO);
        return ResponseEntity.ok(Response.success(code));
    }

    @PostMapping("/check")
    public ResponseEntity<Response<Void>> check(@RequestBody CheckRequestDTO checkRequestDTO) {
        smsService.check(checkRequestDTO);
        return ResponseEntity.ok(Response.successWithoutData());
    }
}
