package com.example.module_member.dto;

import com.example.module_member.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserResponseDto {
    private String userName;
    private String name;
    private String gender;
    private String birthDate;
    private String phoneNumber;
    private String riskTolerance;

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

