package com.hanjari.backend.restaurant;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

// HTTP 요청을 받는 입구. URL과 서비스 메서드를 연결하고, 반환값을 JSON으로 바꿔 응답한다.
@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    // GET /api/restaurants?district=마포구&people=8&room=true (파라미터는 모두 생략 가능)
    // name을 꼭 적는다: VS Code(F5) 빌드는 파라미터 이름 정보를 남기지 않아 생략하면 500 오류가 난다
    @GetMapping
    public List<RestaurantSummaryResponse> getRestaurants(
            @RequestParam(name = "district", required = false) String district,
            @RequestParam(name = "people", required = false) Integer people,
            @RequestParam(name = "room", defaultValue = "false") boolean room) {
        return restaurantService.searchRestaurants(district, people, room);
    }

    // GET /api/restaurants/1 → 식당 상세. 없거나 영업 중이 아니면 404
    @GetMapping("/{id}")
    public RestaurantDetailResponse getRestaurant(@PathVariable("id") Long id) {
        return restaurantService.getRestaurant(id);
    }
}
