package com.example.module_member.dto;

import com.example.module_member.user.User;
import lombok.Getter;

@Getter
public class UserResponseDto {
    private final String userName;
    private final String name;
    private final String gender;
    private final String birthDate;
    private final String phoneNumber;
    private final String riskTolerance;
    private final String token;

    public UserResponseDto(User user, String token) {
        this.userName = user.getUserName();
        this.name = user.getName();
        this.gender = user.getGender();
        this.birthDate = String.valueOf(user.getBirthDate());
        this.phoneNumber = user.getPhoneNumber();
        this.riskTolerance = user.getRiskTolerance();
        this.token = token;
    }

    public static UserResponseDto fromEntity(User user, String token) {
        return new UserResponseDto(user, token);
    }
}

