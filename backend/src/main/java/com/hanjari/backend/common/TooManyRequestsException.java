package com.hanjari.backend.common;

import lombok.Getter;

// 같은 접속지에서 요청이 너무 많을 때 던진다 → GlobalExceptionHandler가 429 { "message": ... }로 응답
@Getter
public class TooManyRequestsException extends RuntimeException {

    // 다시 시도할 수 있을 때까지 남은 초. 응답 헤더 Retry-After에 넣는다
    private final long retryAfterSeconds;

    public TooManyRequestsException(String message, long retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
    }
}
