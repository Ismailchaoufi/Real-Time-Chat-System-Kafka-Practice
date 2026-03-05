package com.chat.producer.chatproducerservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Clients subscribe to destinations with /topic prefix
        config.enableSimpleBroker("/topic", "/queue");

        // Messages sent from clients must start with /app
        config.setApplicationDestinationPrefixes("/app");

        // User-specific destinations (for private messages)
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-chat")
            .setAllowedOriginPatterns("*")
            .withSockJS();     // Fallback for browsers without WebSocket support
    }
}
