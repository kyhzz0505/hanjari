package com.hanjari.backend.common;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// CORS: 브라우저는 다른 주소(포트)의 서버를 부를 때 그 서버가 허락했는지 확인한다.
// 프론트(Live Server, 5500번 포트)가 이 서버(8080번 포트)의 /api를 호출할 수 있게 허락한다.
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://127.0.0.1:5500", "http://localhost:5500")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                // 로그인 세션 쿠키(JSESSIONID)를 함께 보내도록 허용 (프론트는 credentials: 'include')
                .allowCredentials(true);
    }
}
