package com.example.module_alarm.fcm;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FcmTokenDTO {
    private Integer userId;
    private String token;

    public Fcm toEntity(){
        return Fcm.builder()
                .userId(userId)
                .token(token)
                .build();
    }
}
