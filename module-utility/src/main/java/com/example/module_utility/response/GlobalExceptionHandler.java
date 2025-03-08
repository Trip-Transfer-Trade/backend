package com.example.module_utility.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<Response<Void>> handleRuntimeException(RuntimeException e) {

        Response<Void> response =Response.error(500, e.getMessage());
        return new ResponseEntity<>(response,HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InvalidVerificationCodeException.class)
    public ResponseEntity<Response<Void>> handleInvalidVerificationCodeException(InvalidVerificationCodeException e) {
        Response<Void> response =Response.error(400, e.getMessage());
        return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
    }

}
