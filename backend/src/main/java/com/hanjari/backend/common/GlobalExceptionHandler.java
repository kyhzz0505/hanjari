package com.hanjari.backend.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.hanjari.backend.restaurant.RestaurantNotFoundException;

// 모든 컨트롤러에서 던진 예외를 한곳에서 받아 명세에 맞는 오류 JSON으로 바꾼다
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RestaurantNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleRestaurantNotFound(RestaurantNotFoundException e) {
        return new ErrorResponse(e.getMessage());
    }
}
