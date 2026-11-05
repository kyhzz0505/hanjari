package com.hanjari.backend.ai;

import java.time.Duration;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hanjari.backend.common.ClientAddress;
import com.hanjari.backend.common.RateLimiter;
import com.hanjari.backend.common.TooManyRequestsException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

// AI 자연어 검색 입구: POST /api/ai/search  { "query": "..." }
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiSearchController {

    // 같은 접속지는 1분에 10번까지만 (누군가 반복 호출해서 Gemini 무료 사용량을 다 쓰지 않도록)
    private static final int MAX_REQUESTS = 10;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    private final AiSearchService aiSearchService;
    private final RateLimiter limiter = new RateLimiter(MAX_REQUESTS, WINDOW);

    @PostMapping("/search")
    public AiSearchResponse search(@RequestBody AiSearchRequest request, HttpServletRequest httpRequest) {
        String client = ClientAddress.of(httpRequest);
        if (!limiter.tryAcquire(client)) {
            throw new TooManyRequestsException("AI 검색 요청이 너무 많습니다. 잠시 후 다시 시도해 주세요.",
                    limiter.retryAfterSeconds(client));
        }
        return aiSearchService.search(request.query());
    }
}
