package com.hanjari.backend.auth;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hanjari.backend.common.BadRequestException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    // 이메일이 없는 경우와 비밀번호가 틀린 경우를 같은 문장으로 알려서, 어느 쪽이 틀렸는지 드러내지 않는다
    private static final String LOGIN_FAILED = "이메일 또는 비밀번호가 올바르지 않습니다.";

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public User authenticate(String email, String password) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            throw new BadRequestException("이메일과 비밀번호를 입력해 주세요.");
        }
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new UnauthorizedException(LOGIN_FAILED));
        if (!passwordHasher.matches(password, user.getPasswordHash())) {
            throw new UnauthorizedException(LOGIN_FAILED);
        }
        return user;
    }

    public User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException(LoginSession.LOGIN_REQUIRED));
    }
}
