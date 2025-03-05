package com.example.module_member.dto;

import com.example.module_member.user.User;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UserResponseDto {
    private final String userName;
    private final String name;
    private final String gender;
    private final String birthDate;
    private final String phoneNumber;
    private final String riskTolerance;

    @Builder
    public UserResponseDto(User user) {
        this.userName = user.getUserName();
        this.name = user.getName();
        this.gender = user.getGender();
        this.birthDate = String.valueOf(user.getBirthDate());
        this.phoneNumber = user.getPhoneNumber();
        this.riskTolerance = user.getRiskTolerance();
    }

    public static UserResponseDto fromEntity(User user) {
        return UserResponseDto.builder()
                .user(user)
                .build();
    }

}

