package com.hanjari.backend.common;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.hanjari.backend.auth.UnauthorizedException;
import com.hanjari.backend.restaurant.RestaurantNotFoundException;

import lombok.extern.slf4j.Slf4j;

// 모든 컨트롤러에서 던진 예외를 한곳에서 받아 명세에 맞는 오류 JSON { "message": "..." }으로 바꾼다
// 여기서 처리하지 않으면 Spring 기본 오류 형식(timestamp, status, trace 등)이 그대로 나간다
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RestaurantNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleRestaurantNotFound(RestaurantNotFoundException e) {
        return new ErrorResponse(e.getMessage());
    }

    // 로그인이 필요하거나 로그인에 실패한 경우
    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleUnauthorized(UnauthorizedException e) {
        return new ErrorResponse(e.getMessage());
    }

    // 요청 값이 잘못된 경우 (예: AI 검색 문장이 비어 있음)
    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadRequest(BadRequestException e) {
        return new ErrorResponse(e.getMessage());
    }

    // 같은 접속지에서 요청이 너무 많은 경우 (로그인 실패 반복, AI 검색 반복)
    // Retry-After 헤더로 몇 초 뒤에 다시 시도할 수 있는지 알려 준다
    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<ErrorResponse> handleTooManyRequests(TooManyRequestsException e) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header(HttpHeaders.RETRY_AFTER, String.valueOf(e.getRetryAfterSeconds()))
                .body(new ErrorResponse(e.getMessage()));
    }

    // 숫자 자리에 글자가 온 경우 (예: /api/restaurants/abc, ?people=abc)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return new ErrorResponse("요청 값의 형식이 올바르지 않습니다: " + e.getName());
    }

    // POST 요청의 JSON 본문이 비었거나 깨진 경우 (AI 검색 API에서 사용)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleNotReadable(HttpMessageNotReadableException e) {
        return new ErrorResponse("요청 본문을 읽을 수 없습니다.");
    }

    // 없는 주소 (예: /api/unknown)
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNoResource(NoResourceFoundException e) {
        return new ErrorResponse("요청한 주소를 찾을 수 없습니다.");
    }

    // 주소는 있지만 방식이 다른 경우 (예: GET만 되는 곳에 DELETE)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ErrorResponse handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return new ErrorResponse("지원하지 않는 요청 방식입니다.");
    }

    // 위에서 처리하지 못한 나머지 오류. 원인은 서버 로그에만 남기고 사용자에게는 내부 정보를 보여 주지 않는다
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleUnexpected(Exception e) {
        log.error("처리하지 못한 오류", e);
        return new ErrorResponse("서버 오류가 발생했습니다.");
    }
}
