package com.hanjari.backend.restaurant;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// restaurants 테이블을 조회하는 창구
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    // SELECT * FROM restaurants WHERE id = ? AND status = ?
    Optional<Restaurant> findByIdAndStatus(Long id, RestaurantStatus status);

    // 검색 규칙 (CLAUDE.md): 영업 중 AND 지역 일치 AND
    // "인원이 들어가는 좌석(room=true면 룸)"이 하나라도 있는 식당
    // 조건 값이 null(또는 room=false)이면 그 조건은 건너뛴다
    @Query("""
            select r from Restaurant r
            where r.status = com.hanjari.backend.restaurant.RestaurantStatus.OPEN
              and (:district is null or r.district = :district)
              and ((:people is null and :room = false)
                   or exists (select s from Seating s
                              where s.restaurant = r
                                and (:people is null or (s.minCapacity <= :people and s.maxCapacity >= :people))
                                and (:room = false or s.seatingType = com.hanjari.backend.restaurant.SeatingType.ROOM)))
            order by r.id
            """)
    List<Restaurant> search(
            @Param("district") String district,
            @Param("people") Integer people,
            @Param("room") boolean room);
}
