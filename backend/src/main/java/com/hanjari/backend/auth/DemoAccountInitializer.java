package com.hanjari.backend.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// 서버가 켜질 때 .env의 DEMO_EMAIL, DEMO_PASSWORD로 체험 계정을 만들거나 비밀번호를 맞춘다.
// 이메일·비밀번호는 코드나 SQL에 넣지 않는다. 값이 없으면 체험 계정 없이 시작한다.
@Slf4j
@Component
@RequiredArgsConstructor
public class DemoAccountInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    @Value("${demo.email:}")
    private String email;

    @Value("${demo.password:}")
    private String password;

    @Value("${demo.display-name:체험 회원}")
    private String displayName;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (email.isBlank() || password.isBlank()) {
            log.info("DEMO_EMAIL·DEMO_PASSWORD가 없어 체험 계정 없이 시작합니다.");
            return;
        }
        String normalized = email.trim().toLowerCase();
        String hash = passwordHasher.hash(password);
        userRepository.findByEmail(normalized).ifPresentOrElse(
                user -> user.update(hash, displayName),
                () -> userRepository.save(new User(normalized, hash, displayName)));
        log.info("체험 계정을 준비했습니다.");
    }
}
