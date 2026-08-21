package com.programmers.kdt.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// CORS는 공개 진입점인 게이트웨이에서만 처리한다. 개별 서비스(user/order/performance)까지
// 이 설정을 가지고 있으면 응답에 Access-Control-Allow-Origin이 게이트웨이와 중복으로 붙어서
// 브라우저가 응답을 거부한다 - 원래 common 모듈에 있던 걸 여기로 옮겨서 해결함(2026-08-20).
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${cors.allowed-origins:http://localhost:3000}")
    private String[] allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
