package com.hanjari.backend.restaurant;

// GET /api/restaurants 응답 한 건 (docs/api-spec.md 1번)
public record RestaurantSummaryResponse(
        Long id,
        String name,
        String district,
        String address,
        String category,
        Integer maxGroupCapacity, // 좌석 정보가 없으면 null
        boolean hasRoom,
        String mainImageUrl) {    // 사진이 없으면 null

    public static RestaurantSummaryResponse of(
            Restaurant restaurant, Integer maxGroupCapacity, boolean hasRoom, String mainImageUrl) {
        return new RestaurantSummaryResponse(
                restaurant.getId(),
                restaurant.getName(),
                restaurant.getDistrict(),
                restaurant.getAddress(),
                restaurant.getCategory(),
                maxGroupCapacity,
                hasRoom,
                mainImageUrl);
    }
}
