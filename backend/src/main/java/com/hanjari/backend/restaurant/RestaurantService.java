package com.hanjari.backend.restaurant;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

// 식당 관련 비즈니스 규칙을 담는 곳 (예: "영업 중인 식당만 보여 준다")
@Service
@RequiredArgsConstructor // final 필드를 받는 생성자를 Lombok이 만들어 줌 → Spring이 Repository를 주입
@Transactional(readOnly = true)
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final SeatingRepository seatingRepository;
    private final RestaurantImageRepository restaurantImageRepository;

    public List<RestaurantSummaryResponse> findOpenRestaurants() {
        List<Restaurant> restaurants = restaurantRepository.findByStatusOrderByIdAsc(RestaurantStatus.OPEN);
        List<Long> ids = restaurants.stream().map(Restaurant::getId).toList();

        // 식당이 몇 곳이든 좌석·사진은 쿼리 한 번씩만 실행하고, 식당 id별로 묶는다
        Map<Long, List<Seating>> seatingsByRestaurant = seatingRepository.findByRestaurantIdIn(ids).stream()
                .collect(Collectors.groupingBy(seating -> seating.getRestaurant().getId()));

        // 정렬된 순서대로 들어오므로 식당별 첫 사진만 남기면 대표 사진이 된다
        Map<Long, String> mainImageUrlByRestaurant = restaurantImageRepository
                .findByRestaurantIdInOrderBySortOrderAscIdAsc(ids).stream()
                .collect(Collectors.toMap(
                        image -> image.getRestaurant().getId(),
                        RestaurantImage::getImageUrl,
                        (first, ignored) -> first));

        return restaurants.stream()
                .map(restaurant -> {
                    List<Seating> seatings = seatingsByRestaurant.getOrDefault(restaurant.getId(), List.of());
                    Integer maxGroupCapacity = seatings.stream()
                            .map(Seating::getMaxCapacity)
                            .max(Integer::compare)
                            .orElse(null);
                    boolean hasRoom = seatings.stream()
                            .anyMatch(seating -> seating.getSeatingType() == SeatingType.ROOM);
                    return RestaurantSummaryResponse.of(
                            restaurant, maxGroupCapacity, hasRoom, mainImageUrlByRestaurant.get(restaurant.getId()));
                })
                .toList();
    }
}
