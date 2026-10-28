package com.hanjari.backend.common;

// 요청 값이 잘못됐을 때 던진다 → GlobalExceptionHandler가 400 { "message": ... }로 응답
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
