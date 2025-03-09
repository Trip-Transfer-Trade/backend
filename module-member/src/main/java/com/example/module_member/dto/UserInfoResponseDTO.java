package com.example.module_member.dto;

import com.example.module_member.user.User;
import lombok.Builder;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
public class UserInfoResponseDTO {

    private String name;
    private String birthDate;
    private String phoneNumber;

    @Builder
    public UserInfoResponseDTO(String name, String birthDate, String phoneNumber) {
        this.name = name;
        this.birthDate = birthDate;
        this.phoneNumber = phoneNumber;
    }

    public static UserInfoResponseDTO basicInfoFromEntity(User user) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return UserInfoResponseDTO.builder()
                .name(user.getName())
                .birthDate(user.getBirthDate().format(formatter))
                .phoneNumber(user.getPhoneNumber())
                .build();
    }
}
