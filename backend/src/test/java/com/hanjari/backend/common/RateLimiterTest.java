package com.hanjari.backend.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

// 횟수 제한이 한도에서 막고, 시간이 지나거나 기록을 지우면 다시 풀리는지 확인 (DB 없이 실행)
class RateLimiterTest {

    // 테스트에서 시간을 직접 움직이는 시계
    private static class TestClock extends Clock {
        private Instant now = Instant.parse("2026-11-05T00:00:00Z");

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public Instant instant() {
            return now;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }
    }

    private final TestClock clock = new TestClock();
    private final RateLimiter limiter = new RateLimiter(3, Duration.ofMinutes(1), clock);

    @Test
    void 한도까지만_허용한다() {
        assertTrue(limiter.tryAcquire("a"));
        assertTrue(limiter.tryAcquire("a"));
        assertTrue(limiter.tryAcquire("a"));
        assertFalse(limiter.tryAcquire("a"));
        // 다른 접속지는 따로 센다
        assertTrue(limiter.tryAcquire("b"));
    }

    @Test
    void 시간이_지나면_다시_허용한다() {
        for (int i = 0; i < 3; i++) {
            limiter.tryAcquire("a");
        }
        assertFalse(limiter.tryAcquire("a"));
        assertEquals(60, limiter.retryAfterSeconds("a"));

        clock.advance(Duration.ofSeconds(40));
        assertFalse(limiter.tryAcquire("a"));
        assertEquals(20, limiter.retryAfterSeconds("a"));

        clock.advance(Duration.ofSeconds(20));
        assertTrue(limiter.tryAcquire("a"));
    }

    @Test
    void 실패_횟수는_기록할_때만_늘고_지우면_풀린다() {
        assertFalse(limiter.isLimited("a"));
        limiter.record("a");
        limiter.record("a");
        assertFalse(limiter.isLimited("a"));
        limiter.record("a");
        assertTrue(limiter.isLimited("a"));

        limiter.reset("a");
        assertFalse(limiter.isLimited("a"));
    }
}
