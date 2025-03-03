package com.example.module_member.dto;

import com.example.module_member.user.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.Date;

@Getter
public class SignUpRequestDto {

    @NotBlank(message = "아이디를 입력하세요.")
    private final String userName;

    @NotBlank(message = "비밀번호를 입력하세요.")
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
    private final String password;

    @NotBlank(message = "이름을 입력하세요.")
    private final String name;

    private final String gender;
    private final Date birthDate;
    private final String phoneNumber;
    private final String riskTolerance;

    public SignUpRequestDto(String userName, String password, String name, String gender, Date birthDate, String phoneNumber, String riskTolerance) {
        this.userName = userName;
        this.password = password;
        this.name = name;
        this.gender = gender;
        this.birthDate = birthDate;
        this.phoneNumber = phoneNumber;
        this.riskTolerance = riskTolerance;
    }
    public User toEntity(String encodedPassword) {
        return User.builder()
                .userName(this.userName)
                .password(encodedPassword) // 여기서 암호화된 비밀번호 사용
                .name(this.name)
                .gender(this.gender)
                .birthDate(this.birthDate)
                .phoneNumber(this.phoneNumber)
                .riskTolerance(this.riskTolerance)
                .build();
    }
}

