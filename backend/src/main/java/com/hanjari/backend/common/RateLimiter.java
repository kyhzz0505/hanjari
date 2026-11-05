package com.hanjari.backend.common;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// 같은 접속지의 요청 횟수를 정해진 시간(window) 동안 센다. 시간이 지나면 다시 0부터 센다.
// 서버 메모리에만 두므로 서버를 다시 켜면 초기화된다. 라이브러리를 추가하지 않기 위해 직접 구현한다.
public class RateLimiter {

    // 접속지가 너무 많이 쌓이면 시간이 지난 기록을 지운다
    private static final int CLEANUP_THRESHOLD = 10_000;

    private final int limit;
    private final Duration window;
    private final Clock clock;
    private final Map<String, Count> counts = new ConcurrentHashMap<>();

    public RateLimiter(int limit, Duration window) {
        this(limit, window, Clock.systemUTC());
    }

    // 테스트에서 시간을 직접 움직이기 위한 생성자
    RateLimiter(int limit, Duration window, Clock clock) {
        this.limit = limit;
        this.window = window;
        this.clock = clock;
    }

    // 이미 한도까지 썼는지 (세지는 않는다)
    public boolean isLimited(String key) {
        Count count = counts.get(key);
        return count != null && !count.expired(clock.instant(), window) && count.value() >= limit;
    }

    // 한 번 센다
    public void record(String key) {
        Instant now = clock.instant();
        cleanUpIfLarge(now);
        counts.compute(key, (k, count) -> count == null || count.expired(now, window)
                ? new Count(now, 1)
                : new Count(count.start(), count.value() + 1));
    }

    // 한도 안이면 세고 true, 이미 한도까지 썼으면 세지 않고 false
    public boolean tryAcquire(String key) {
        Instant now = clock.instant();
        cleanUpIfLarge(now);
        boolean[] allowed = new boolean[1];
        counts.compute(key, (k, count) -> {
            if (count == null || count.expired(now, window)) {
                allowed[0] = true;
                return new Count(now, 1);
            }
            allowed[0] = count.value() < limit;
            return allowed[0] ? new Count(count.start(), count.value() + 1) : count;
        });
        return allowed[0];
    }

    // 기록을 지운다 (예: 로그인에 성공하면 실패 횟수를 지운다)
    public void reset(String key) {
        counts.remove(key);
    }

    // 다시 시도할 수 있을 때까지 남은 초 (최소 1초)
    public long retryAfterSeconds(String key) {
        Count count = counts.get(key);
        if (count == null) {
            return 1;
        }
        long seconds = Duration.between(clock.instant(), count.start().plus(window)).toSeconds();
        return Math.max(1, seconds);
    }

    private void cleanUpIfLarge(Instant now) {
        if (counts.size() > CLEANUP_THRESHOLD) {
            counts.values().removeIf(count -> count.expired(now, window));
        }
    }

    private record Count(Instant start, int value) {
        boolean expired(Instant now, Duration window) {
            return !now.isBefore(start.plus(window));
        }
    }
}
