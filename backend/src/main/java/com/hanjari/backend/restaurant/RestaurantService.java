package com.hanjari.backend.restaurant;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

// 식당 관련 비즈니스 규칙을 담는 곳 (예: "영업 중인 식당만 보여 준다")
@Service
@RequiredArgsConstructor // final 필드를 받는 생성자를 Lombok이 만들어 줌 → Spring이 Repository를 주입
@Transactional(readOnly = true)
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    public List<RestaurantSummaryResponse> findOpenRestaurants() {
        return restaurantRepository.findByStatusOrderByIdAsc(RestaurantStatus.OPEN).stream()
                .map(RestaurantSummaryResponse::from)
                .toList();
    }
}
