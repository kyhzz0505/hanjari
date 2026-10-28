package com.hanjari.backend.restaurant;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hanjari.backend.common.BadRequestException;

import lombok.RequiredArgsConstructor;

// 식당 관련 비즈니스 규칙을 담는 곳 (예: "영업 중인 식당만 보여 준다")
@Service
@RequiredArgsConstructor // final 필드를 받는 생성자를 Lombok이 만들어 줌 → Spring이 Repository를 주입
@Transactional(readOnly = true)
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final SeatingRepository seatingRepository;
    private final RestaurantImageRepository restaurantImageRepository;

    public List<RestaurantSummaryResponse> searchRestaurants(String district, Integer people, boolean room) {
        if (people != null && people < 1) {
            throw new BadRequestException("인원은 1명 이상이어야 합니다.");
        }
        // "district=" 처럼 값 없이 오면 지역 조건 없음으로 처리
        String districtOrNull = (district == null || district.isBlank()) ? null : district.trim();
        List<Restaurant> restaurants = restaurantRepository.search(districtOrNull, people, room);
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
                    return RestaurantSummaryResponse.of(
                            restaurant,
                            maxGroupCapacity(seatings),
                            hasRoom(seatings),
                            mainImageUrlByRestaurant.get(restaurant.getId()));
                })
                .toList();
    }

    public RestaurantDetailResponse getRestaurant(Long id) {
        // 영업 중이 아니면(폐업·비공개) 없는 식당과 똑같이 404
        Restaurant restaurant = restaurantRepository.findByIdAndStatus(id, RestaurantStatus.OPEN)
                .orElseThrow(RestaurantNotFoundException::new);
        List<Seating> seatings = seatingRepository.findByRestaurantIdOrderByIdAsc(id);
        List<RestaurantImage> images = restaurantImageRepository.findByRestaurantIdOrderBySortOrderAscIdAsc(id);

        return new RestaurantDetailResponse(
                restaurant.getId(),
                restaurant.getName(),
                restaurant.getDistrict(),
                restaurant.getAddress(),
                restaurant.getCategory(),
                restaurant.getPhone(),
                restaurant.getBusinessHours(),
                maxGroupCapacity(seatings),
                hasRoom(seatings),
                restaurant.getLastVerifiedAt(),
                seatings.stream().map(RestaurantDetailResponse.SeatingResponse::from).toList(),
                images.stream().map(RestaurantDetailResponse.ImageResponse::from).toList());
    }

    // 한 그룹이 함께 앉을 수 있는 최대 인원 = 좌석 중 가장 큰 max_capacity (좌석이 없으면 null)
    private static Integer maxGroupCapacity(List<Seating> seatings) {
        return seatings.stream()
                .map(Seating::getMaxCapacity)
                .max(Integer::compare)
                .orElse(null);
    }

    private static boolean hasRoom(List<Seating> seatings) {
        return seatings.stream()
                .anyMatch(seating -> seating.getSeatingType() == SeatingType.ROOM);
    }
}
