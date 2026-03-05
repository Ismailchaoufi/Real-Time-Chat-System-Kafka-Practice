package com.chat.apigateway.config;

import com.chat.apigateway.filter.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()

                // ── Auth Service (public – no JWT required) ──────────────────────
                .route("auth-service", r -> r
                        .path("/api/auth/**")
                        .uri("http://auth-service:8081"))

                // ── Chat Service WebSocket ────────────────────────────────────────
                .route("chat-service-ws", r -> r
                        .path("/ws-chat/**")
                        .uri("ws://chat-service:8082"))

                // ── Chat Service REST (protected) ─────────────────────────────────
                .route("chat-service", r -> r
                        .path("/api/chat/**")
                        .filters(f -> f.filter(jwtAuthFilter.apply(new JwtAuthFilter.Config())))
                        .uri("http://chat-service:8082"))

                // ── Message Service REST (protected) ──────────────────────────────
                .route("message-service", r -> r
                        .path("/api/messages/**")
                        .filters(f -> f.filter(jwtAuthFilter.apply(new JwtAuthFilter.Config())))
                        .uri("http://message-service:8083"))

                .build();
    }
}