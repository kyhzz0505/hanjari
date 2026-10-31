package com.hanjari.backend.auth;

// 로그인한 사용자 정보 (비밀번호 해시 같은 내부 값은 내보내지 않는다)
public record MeResponse(String email, String displayName) {

    static MeResponse from(User user) {
        return new MeResponse(user.getEmail(), user.getDisplayName());
    }
}
