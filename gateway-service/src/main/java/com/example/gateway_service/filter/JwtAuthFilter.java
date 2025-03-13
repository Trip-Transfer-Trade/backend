package com.example.gateway_service.filter;

import com.example.gateway_service.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        logger.info("📌 요청 경로: {}", request.getURI().getPath());
        logger.info("📌 요청 헤더: {}", request.getHeaders());

        if (isPublicEndpoint(request)) {
            return chain.filter(exchange);
        }

        String token = resolveToken(request);
        if (token == null || !jwtUtil.validateToken(token)) {
            logger.warn("❌ 인증 실패 - 유효하지 않은 토큰 (경로: {})", request.getURI().getPath());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        int userId = jwtUtil.extractUserId(token);
        String username = jwtUtil.extractUsername(token);
        logger.info("✅ 인증 성공 - 사용자: {} (경로: {})", userId, request.getURI().getPath());

        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-Authenticated-User", String.valueOf(userId))
                .header("X-Authenticated-Username", username)
                .build();

        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

    private boolean isPublicEndpoint(ServerHttpRequest request) {
        String path = request.getURI().getPath();
        return path.contains("/api/members/signup") || path.contains("/api/members/login") || path.contains("/api/members/send") || path.contains("/api/members/check") || path.contains("/api/exchanges/us/ranking") || path.contains("/api/exchanges/ranking");
    }

    private String resolveToken(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return request.getCookies().getFirst("token") != null
                ? Objects.requireNonNull(request.getCookies().getFirst("token")).getValue()
                : null;
    }
    @Override
    public int getOrder() {
        return -1;
    }
}
