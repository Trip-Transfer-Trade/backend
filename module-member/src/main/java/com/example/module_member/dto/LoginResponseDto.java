package com.example.module_member.dto;

import lombok.Getter;

@Getter
public class LoginResponseDto {
    private final String token;

    public LoginResponseDto(String token) {
        this.token = token;
    }
}