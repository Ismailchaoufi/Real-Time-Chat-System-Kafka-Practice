package com.chat.producer.chatproducerservice.config;

import com.chat.producer.chatproducerservice.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
            MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    String username = jwtService.extractUsername(token);
                    String userId = jwtService.extractUserId(token);

                    Authentication auth = new UsernamePasswordAuthenticationToken(
                        username, null, Collections.emptyList()
                    );
                    accessor.setUser(auth);
                    accessor.setNativeHeader("X-Auth-Username", username);
                    accessor.setNativeHeader("X-Auth-UserId", userId);

                    log.info("WebSocket CONNECT authenticated: {}", username);
                } catch (Exception e) {
                    log.warn("WebSocket CONNECT rejected – invalid token: {}", e.getMessage());
                    throw new IllegalArgumentException("Invalid JWT token");
                }
            }
        }
        return message;
    }
}
