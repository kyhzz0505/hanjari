package com.hanjari.backend.ai;

// POST /api/ai/search 요청 본문: { "query": "마포구에서 10명 회식할 건데 룸이면 좋겠어" }
public record AiSearchRequest(String query) {
}
