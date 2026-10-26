package com.hanjari.backend.restaurant;

// GET /api/restaurants 응답 한 건 (docs/api-spec.md 1번)
// maxGroupCapacity, hasRoom, mainImageUrl은 좌석·사진 엔티티를 만든 뒤 추가한다.
public record RestaurantSummaryResponse(
        Long id,
        String name,
        String district,
        String address,
        String category) {

    public static RestaurantSummaryResponse from(Restaurant restaurant) {
        return new RestaurantSummaryResponse(
                restaurant.getId(),
                restaurant.getName(),
                restaurant.getDistrict(),
                restaurant.getAddress(),
                restaurant.getCategory());
    }
}
