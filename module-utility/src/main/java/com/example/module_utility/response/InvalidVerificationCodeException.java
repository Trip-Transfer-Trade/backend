package com.example.module_utility.response;

public class InvalidVerificationCodeException extends RuntimeException{
    public InvalidVerificationCodeException(String message) {
        super(message);
    }
}
