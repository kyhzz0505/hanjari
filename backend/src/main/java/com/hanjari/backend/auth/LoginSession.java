package com.hanjari.backend.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

// 서버 세션에 로그인한 사용자 id를 저장·조회하는 곳 (쿠키 이름: JSESSIONID)
public final class LoginSession {

    private static final String USER_ID = "loginUserId";
    public static final String LOGIN_REQUIRED = "로그인이 필요합니다.";

    private LoginSession() {
    }

    static void login(HttpSession session, Long userId) {
        session.setAttribute(USER_ID, userId);
    }

    // 로그인하지 않았으면 401. 로그인 안 한 사람에게 세션을 새로 만들지 않도록 getSession(false)를 쓴다
    public static Long requireUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object userId = session == null ? null : session.getAttribute(USER_ID);
        if (userId instanceof Long id) {
            return id;
        }
        throw new UnauthorizedException(LOGIN_REQUIRED);
    }
}
