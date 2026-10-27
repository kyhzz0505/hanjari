package com.hanjari.backend.restaurant;

import java.time.LocalDate;
import java.util.List;

// GET /api/restaurants/{id} 응답 (docs/api-spec.md 2번)
public record RestaurantDetailResponse(
        Long id,
        String name,
        String district,
        String address,
        String category,
        String phone,
        String businessHours,
        Integer maxGroupCapacity, // 좌석 정보가 없으면 null
        boolean hasRoom,
        LocalDate lastVerifiedAt, // JSON에서는 "2026-10-20" 형식
        List<SeatingResponse> seatings,
        List<ImageResponse> images) {

    // 좌석 한 종류
    public record SeatingResponse(
            Long id,
            String name,
            SeatingType seatingType,
            int minCapacity,
            int maxCapacity,
            int quantity,
            String description) {

        static SeatingResponse from(Seating seating) {
            return new SeatingResponse(
                    seating.getId(),
                    seating.getName(),
                    seating.getSeatingType(),
                    seating.getMinCapacity(),
                    seating.getMaxCapacity(),
                    seating.getQuantity(),
                    seating.getDescription());
        }
    }

    // 사진 한 장 (seatingId가 null이면 식당 전체 사진)
    public record ImageResponse(
            Long id,
            Long seatingId,
            String imageUrl,
            ImageType imageType,
            String caption,
            int sortOrder) {

        static ImageResponse from(RestaurantImage image) {
            return new ImageResponse(
                    image.getId(),
                    image.getSeating() == null ? null : image.getSeating().getId(),
                    image.getImageUrl(),
                    image.getImageType(),
                    image.getCaption(),
                    image.getSortOrder());
        }
    }
}
