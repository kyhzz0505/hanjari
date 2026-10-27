package com.hanjari.backend.restaurant;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RestaurantImageRepository extends JpaRepository<RestaurantImage, Long> {

    // SELECT * FROM restaurant_images WHERE restaurant_id IN (...) ORDER BY sort_order, id
    // 식당마다 맨 앞에 오는 사진이 대표 사진이 된다
    List<RestaurantImage> findByRestaurantIdInOrderBySortOrderAscIdAsc(Collection<Long> restaurantIds);

    // 식당 한 곳의 사진을 표시 순서대로 (상세 화면용)
    List<RestaurantImage> findByRestaurantIdOrderBySortOrderAscIdAsc(Long restaurantId);
}
