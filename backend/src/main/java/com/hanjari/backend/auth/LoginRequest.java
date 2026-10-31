package com.hanjari.backend.auth;

// POST /api/auth/login 요청 본문
public record LoginRequest(String email, String password) {
}
