package com.hanjari.backend.favorite;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    // 최근에 추가한 단골이 먼저 오도록 id 내림차순
    List<Favorite> findByUserIdOrderByIdDesc(Long userId);

    boolean existsByUserIdAndRestaurantId(Long userId, Long restaurantId);

    void deleteByUserIdAndRestaurantId(Long userId, Long restaurantId);
}
