package com.hanjari.backend.common;

import jakarta.servlet.http.HttpServletRequest;

// 요청한 사람의 접속 주소(IP). 횟수 제한에서 "같은 사람"을 구분할 때 쓴다.
public final class ClientAddress {

    // 임시 터널(Cloudflare)은 실제 접속 주소를 이 헤더에 넣는다. 사용자가 보낸 같은 이름의 헤더는 Cloudflare가 덮어쓴다.
    // X-Forwarded-For는 맨 앞 값을 사용자가 꾸며 보낼 수 있어서 쓰지 않는다.
    private static final String CLOUDFLARE_HEADER = "CF-Connecting-IP";

    private ClientAddress() {
    }

    public static String of(HttpServletRequest request) {
        String cloudflare = request.getHeader(CLOUDFLARE_HEADER);
        if (cloudflare != null && !cloudflare.isBlank()) {
            return cloudflare.trim();
        }
        return request.getRemoteAddr();
    }
}
