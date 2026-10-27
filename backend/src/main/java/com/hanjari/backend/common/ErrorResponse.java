package com.hanjari.backend.common;

// 오류 응답 형식 (docs/api-spec.md): { "message": "..." }
public record ErrorResponse(String message) {
}
