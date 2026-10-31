package com.hanjari.backend.favorite;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hanjari.backend.restaurant.RestaurantService;
import com.hanjari.backend.restaurant.RestaurantSummaryResponse;

import lombok.RequiredArgsConstructor;

// 회원별 단골 목록 (docs/api-spec.md 5번)
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final RestaurantService restaurantService;

    public List<RestaurantSummaryResponse> list(Long userId) {
        List<Long> restaurantIds = favoriteRepository.findByUserIdOrderByIdDesc(userId).stream()
                .map(Favorite::getRestaurantId)
                .toList();
        return restaurantService.findOpenSummaries(restaurantIds);
    }

    // 이미 단골이면 그대로 둔다 (같은 요청을 여러 번 보내도 결과가 같다)
    @Transactional
    public void add(Long userId, Long restaurantId) {
        restaurantService.requireOpenRestaurant(restaurantId);
        if (!favoriteRepository.existsByUserIdAndRestaurantId(userId, restaurantId)) {
            favoriteRepository.save(new Favorite(userId, restaurantId));
        }
    }

    @Transactional
    public void remove(Long userId, Long restaurantId) {
        favoriteRepository.deleteByUserIdAndRestaurantId(userId, restaurantId);
    }
}
