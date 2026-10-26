package com.hanjari.backend.restaurant;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

// HTTP 요청을 받는 입구. URL과 서비스 메서드를 연결하고, 반환값을 JSON으로 바꿔 응답한다.
@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    // GET /api/restaurants
    @GetMapping
    public List<RestaurantSummaryResponse> getRestaurants() {
        return restaurantService.findOpenRestaurants();
    }
}
