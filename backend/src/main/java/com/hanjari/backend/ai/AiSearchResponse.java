package com.hanjari.backend.ai;

import java.util.List;

import com.hanjari.backend.restaurant.RestaurantSummaryResponse;

// POST /api/ai/search 응답 (docs/api-spec.md 3번)
// message: AI가 검색 결과만 근거로 쓴 추천 설명. AI 호출이 실패하면 null
// restaurants: 목록 API(GET /api/restaurants)와 같은 형식
public record AiSearchResponse(
        SearchConditions conditions,
        String message,
        List<RestaurantSummaryResponse> restaurants) {
}
