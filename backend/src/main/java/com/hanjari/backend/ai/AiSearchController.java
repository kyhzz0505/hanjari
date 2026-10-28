package com.hanjari.backend.ai;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

// AI 자연어 검색 입구: POST /api/ai/search  { "query": "..." }
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiSearchController {

    private final AiSearchService aiSearchService;

    @PostMapping("/search")
    public AiSearchResponse search(@RequestBody AiSearchRequest request) {
        return aiSearchService.search(request.query());
    }
}
