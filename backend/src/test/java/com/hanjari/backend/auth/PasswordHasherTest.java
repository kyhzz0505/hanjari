package com.hanjari.backend.auth;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

// 비밀번호 해시가 원문을 담지 않고, 맞는 비밀번호만 통과시키는지 확인 (DB 없이 실행)
class PasswordHasherTest {

    private final PasswordHasher hasher = new PasswordHasher();

    @Test
    void 맞는_비밀번호만_통과한다() {
        String stored = hasher.hash("correct-horse");
        assertTrue(hasher.matches("correct-horse", stored));
        assertFalse(hasher.matches("wrong-horse", stored));
        assertFalse(hasher.matches("", stored));
    }

    @Test
    void 저장값에_원문이_없고_매번_다르다() {
        String first = hasher.hash("correct-horse");
        String second = hasher.hash("correct-horse");
        assertFalse(first.contains("correct-horse"));
        assertTrue(first.startsWith("pbkdf2$"));
        // 솔트가 매번 달라서 같은 비밀번호도 저장값이 다르다
        assertNotEquals(first, second);
    }

    @Test
    void 형식이_잘못된_저장값은_통과하지_않는다() {
        assertFalse(hasher.matches("anything", null));
        assertFalse(hasher.matches("anything", "plain-text"));
    }
}
