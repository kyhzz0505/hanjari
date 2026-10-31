package com.hanjari.backend.auth;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.springframework.stereotype.Component;

// 비밀번호를 PBKDF2(자바 기본 기능)로 해시한다. 라이브러리를 추가하지 않기 위해 직접 구현한다.
// 저장 형식: pbkdf2$반복횟수$솔트(Base64)$해시(Base64)
@Component
public class PasswordHasher {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 210_000;
    private static final int SALT_BYTES = 16;
    private static final int HASH_BITS = 256;

    private final SecureRandom random = new SecureRandom();

    public String hash(String password) {
        byte[] salt = new byte[SALT_BYTES];
        random.nextBytes(salt);
        byte[] hash = pbkdf2(password, salt, ITERATIONS);
        return "pbkdf2$" + ITERATIONS + "$" + encode(salt) + "$" + encode(hash);
    }

    public boolean matches(String password, String stored) {
        String[] parts = stored == null ? new String[0] : stored.split("\\$");
        if (parts.length != 4 || !"pbkdf2".equals(parts[0])) {
            return false;
        }
        byte[] salt = Base64.getDecoder().decode(parts[2]);
        byte[] expected = Base64.getDecoder().decode(parts[3]);
        byte[] actual = pbkdf2(password, salt, Integer.parseInt(parts[1]));
        // 비교 시간이 일정하도록 MessageDigest.isEqual을 쓴다
        return MessageDigest.isEqual(expected, actual);
    }

    private byte[] pbkdf2(String password, byte[] salt, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, HASH_BITS);
        try {
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("비밀번호 해시를 만들 수 없습니다.", e);
        } finally {
            spec.clearPassword();
        }
    }

    private String encode(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }
}
