package com.hanjari.backend.auth;

import java.time.Duration;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.hanjari.backend.common.ClientAddress;
import com.hanjari.backend.common.RateLimiter;
import com.hanjari.backend.common.TooManyRequestsException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

// 체험 계정 로그인 (docs/api-spec.md 4번). 회원가입 API는 만들지 않는다.
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    // 같은 접속지에서 10분 동안 5번 틀리면 남은 시간 동안 로그인을 막는다 (비밀번호 무작정 대입 방지)
    private static final int MAX_FAILURES = 5;
    private static final Duration FAILURE_WINDOW = Duration.ofMinutes(10);

    private final AuthService authService;
    private final RateLimiter loginFailures = new RateLimiter(MAX_FAILURES, FAILURE_WINDOW);

    @PostMapping("/login")
    public MeResponse login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String client = ClientAddress.of(httpRequest);
        if (loginFailures.isLimited(client)) {
            long seconds = loginFailures.retryAfterSeconds(client);
            long minutes = (seconds + 59) / 60;
            throw new TooManyRequestsException(
                    "로그인 시도가 너무 많습니다. " + minutes + "분 뒤에 다시 시도해 주세요.", seconds);
        }
        User user;
        try {
            user = authService.authenticate(request.email(), request.password());
        } catch (UnauthorizedException e) {
            loginFailures.record(client);
            throw e;
        }
        loginFailures.reset(client);
        // 로그인 전 세션 id를 그대로 쓰지 않도록 새 id로 바꾼다 (세션 고정 공격 방지)
        httpRequest.getSession(true);
        httpRequest.changeSessionId();
        LoginSession.login(httpRequest.getSession(), user.getId());
        return MeResponse.from(user);
    }

    @GetMapping("/me")
    public MeResponse me(HttpServletRequest request) {
        return MeResponse.from(authService.getUser(LoginSession.requireUserId(request)));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}
