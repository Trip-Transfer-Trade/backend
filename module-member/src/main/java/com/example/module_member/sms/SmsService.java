package com.example.module_member.sms;

import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class SmsService {
    private final String apiKey;
    private final String apiSecret;
    private final String fromPhoneNumber;
    private final DefaultMessageService messageService;

    public SmsService(@Value("${coolsms.api.key}") String apiKey,
                      @Value("${coolsms.api.secret}") String apiSecret,
                      @Value("${coolsms.api.number}") String fromPhoneNumber) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.fromPhoneNumber = fromPhoneNumber;
        this.messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecret, "https://api.coolsms.co.kr");
    }

    public String sendSms(String phoneNumber) {
        try {
            // 랜덤한 4자리 인증번호 생성
            String numStr = generateRandomNumber();
            Message message = new Message();

            message.setFrom(fromPhoneNumber);
            message.setTo(phoneNumber);
            message.setText("TTT 인증 번호는 "+numStr+"입니다.");

            SingleMessageSentResponse response = messageService.sendOne(new SingleMessageSendingRequest(message));
            System.out.println(response);

            return numStr; // 생성된 인증번호 반환
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    // 랜덤한 6자리 숫자 생성 메서드x
    private String generateRandomNumber() {
        Random rand = new Random();
        StringBuilder numStr = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            numStr.append(rand.nextInt(10));
        }
        return numStr.toString();
    }
}
