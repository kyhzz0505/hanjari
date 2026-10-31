package com.hanjari.backend.auth;

// 로그인이 필요하거나 로그인에 실패했을 때 던진다 → GlobalExceptionHandler가 401로 응답
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
