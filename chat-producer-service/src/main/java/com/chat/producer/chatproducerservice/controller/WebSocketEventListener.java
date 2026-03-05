package com.chat.producer.chatproducerservice.controller;

import com.chat.producer.chatproducerservice.service.KafkaProducerService;
import com.chat.producer.chatproducerservice.model.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final KafkaProducerService kafkaProducer;

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Map<String, Object> sessionAttrs = accessor.getSessionAttributes();

        if (sessionAttrs == null) return;

        String username = (String) sessionAttrs.get("username");
        String roomId = (String) sessionAttrs.get("roomId");

        if (username != null && roomId != null) {
            ChatMessage leaveMessage = ChatMessage.builder()
                .id(UUID.randomUUID().toString())
                .roomId(roomId)
                .sender(username)
                .content(username + " has left the chat")
                .timestamp(Instant.now().toString())
                .type(ChatMessage.MessageType.LEAVE)
                .build();

            log.info("WebSocket disconnect: user={} room={}", username, roomId);

            messagingTemplate.convertAndSend("/topic/room." + roomId, leaveMessage);
            kafkaProducer.publishMessage(leaveMessage);
        }
    }
}
