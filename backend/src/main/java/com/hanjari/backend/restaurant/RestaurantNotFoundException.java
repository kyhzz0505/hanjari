package com.hanjari.backend.restaurant;

// 없는 식당이거나 폐업·비공개 식당일 때 던진다 → GlobalExceptionHandler가 404로 응답
public class RestaurantNotFoundException extends RuntimeException {

    public RestaurantNotFoundException() {
        super("식당을 찾을 수 없습니다.");
    }
}
