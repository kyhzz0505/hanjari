package com.hanjari.backend.auth;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

// 체험 계정 로그인 (docs/api-spec.md 4번). 회원가입 API는 만들지 않는다.
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public MeResponse login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        User user = authService.authenticate(request.email(), request.password());
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
