package com.chat.producer.chatproducerservice.controller;

import com.chat.producer.chatproducerservice.service.KafkaProducerService;
import com.chat.producer.chatproducerservice.model.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final KafkaProducerService kafkaProducer;

    /**
     * Handles regular chat messages.
     * Client sends to: /app/chat.send
     * Broadcast to:    /topic/room.{roomId}
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessage message, Principal principal) {
        // Enrich message with server-side data
        message.setId(UUID.randomUUID().toString());
        message.setTimestamp(Instant.now().toString());
        message.setType(ChatMessage.MessageType.CHAT);

        if (principal != null) {
            message.setSender(principal.getName());
        }

        log.info("Message from {} in room {}: {}", message.getSender(), message.getRoomId(), message.getContent());

        // 1. Broadcast to all subscribers of this room immediately
        messagingTemplate.convertAndSend("/topic/room." + message.getRoomId(), message);

        // 2. Async persist via Kafka
        kafkaProducer.publishMessage(message);
    }

    /**
     * Handles user joining a room.
     * Client sends to: /app/chat.join
     */
    @MessageMapping("/chat.join")
    public void joinRoom(@Payload ChatMessage message,
                         SimpMessageHeaderAccessor headerAccessor,
                         Principal principal) {
        message.setId(UUID.randomUUID().toString());
        message.setTimestamp(Instant.now().toString());
        message.setType(ChatMessage.MessageType.JOIN);

        if (principal != null) {
            message.setSender(principal.getName());
        }

        // Store username in WebSocket session for disconnect tracking
        if (headerAccessor.getSessionAttributes() != null) {
            headerAccessor.getSessionAttributes().put("username", message.getSender());
            headerAccessor.getSessionAttributes().put("roomId", message.getRoomId());
        }

        log.info("User {} joined room {}", message.getSender(), message.getRoomId());

        messagingTemplate.convertAndSend("/topic/room." + message.getRoomId(), message);
        kafkaProducer.publishMessage(message);
    }

    /**
     * Handles user leaving a room.
     * Client sends to: /app/chat.leave
     */
    @MessageMapping("/chat.leave")
    public void leaveRoom(@Payload ChatMessage message, Principal principal) {
        message.setId(UUID.randomUUID().toString());
        message.setTimestamp(Instant.now().toString());
        message.setType(ChatMessage.MessageType.LEAVE);

        if (principal != null) {
            message.setSender(principal.getName());
        }

        log.info("User {} left room {}", message.getSender(), message.getRoomId());

        messagingTemplate.convertAndSend("/topic/room." + message.getRoomId(), message);
        kafkaProducer.publishMessage(message);
    }

    /**
     * Handles typing indicator.
     * Client sends to: /app/chat.typing
     * Broadcast to:    /topic/room.{roomId}.typing
     */
    @MessageMapping("/chat.typing")
    public void typingIndicator(@Payload ChatMessage message, Principal principal) {
        message.setType(ChatMessage.MessageType.TYPING);
        message.setTimestamp(Instant.now().toString());

        if (principal != null) {
            message.setSender(principal.getName());
        }

        // Broadcast typing event (NOT persisted to Kafka)
        messagingTemplate.convertAndSend(
            "/topic/room." + message.getRoomId() + ".typing", message
        );
    }
}
