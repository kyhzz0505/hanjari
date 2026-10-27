package com.hanjari.backend.restaurant;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatingRepository extends JpaRepository<Seating, Long> {

    // SELECT * FROM seating WHERE restaurant_id IN (...)
    // 식당 여러 곳의 좌석을 쿼리 한 번으로 가져온다
    List<Seating> findByRestaurantIdIn(Collection<Long> restaurantIds);
}
