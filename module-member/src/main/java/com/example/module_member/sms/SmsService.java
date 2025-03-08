package com.example.module_member.sms;

import com.example.module_member.dto.CheckRequestDTO;
import com.example.module_member.dto.SmsSendRequestDTO;
import com.example.module_utility.response.InvalidVerificationCodeException;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class SmsService {
    private final String apiKey;
    private final String apiSecret;
    private final String fromPhoneNumber;
    private final DefaultMessageService messageService;
    private final RedisTemplate<String, Object> redisTemplate;

    public SmsService(@Value("${coolsms.api.key}") String apiKey,
                      @Value("${coolsms.api.secret}") String apiSecret,
                      @Value("${coolsms.api.number}") String fromPhoneNumber, RedisTemplate<String, Object> redisTemplate) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.fromPhoneNumber = fromPhoneNumber;
        this.messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecret, "https://api.coolsms.co.kr");
        this.redisTemplate = redisTemplate;
    }
    private static final long CODE_TTL_SECONDS = 300;

    public String sendSms(SmsSendRequestDTO requestDTO) {
        String phoneNumber = requestDTO.getPhoneNumber();
        try {
            String code = generateRandomNumber();
            Message message = new Message();

            message.setFrom(fromPhoneNumber);
            message.setTo(phoneNumber);
            message.setText("TTT 인증 번호는 "+code+"입니다.");

            saveCode(phoneNumber, code);
            messageService.sendOne(new SingleMessageSendingRequest(message));

            return code;
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    private String generateRandomNumber() {
        Random rand = new Random();
        StringBuilder numStr = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            numStr.append(rand.nextInt(10));
        }
        return numStr.toString();
    }

    public void saveCode(String phoneNumber, String code){
        redisTemplate.opsForValue().set(phoneNumber, code, CODE_TTL_SECONDS, TimeUnit.SECONDS);
    }

    public void check(CheckRequestDTO checkRequestDTO) {
        String storedCode = (String) redisTemplate.opsForValue().get(checkRequestDTO.getPhoneNumber());

        if (storedCode == null || !storedCode.equals(checkRequestDTO.getCode())) {
            throw new InvalidVerificationCodeException("코드가 일치하지 않습니다.");
        }
    }
}
