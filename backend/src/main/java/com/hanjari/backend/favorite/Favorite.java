package com.hanjari.backend.favorite;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// favorites 테이블의 한 행: "이 회원이 이 식당을 단골로 저장했다"
@Entity
@Table(name = "favorites")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long restaurantId;

    // created_at은 DB가 자동으로 채우므로 매핑하지 않는다.

    public Favorite(Long userId, Long restaurantId) {
        this.userId = userId;
        this.restaurantId = restaurantId;
    }
}
