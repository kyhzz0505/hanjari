package com.hanjari.backend.restaurant;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

// restaurants 테이블을 조회하는 창구
// 메서드 이름만 규칙대로 지으면 Spring Data JPA가 SQL을 만들어 준다.
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    // SELECT * FROM restaurants WHERE status = ? ORDER BY id ASC
    List<Restaurant> findByStatusOrderByIdAsc(RestaurantStatus status);
}
