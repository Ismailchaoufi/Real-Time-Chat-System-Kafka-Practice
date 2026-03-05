package com.chat.consumer.chatconsumerservice.consumer;

import com.chat.consumer.chatconsumerservice.model.ChatMessage;
import com.chat.consumer.chatconsumerservice.model.MessageDocument;
import com.chat.consumer.chatconsumerservice.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final MessageRepository messageRepository;

    /**
     * Listens to the chat-messages Kafka topic.
     * Persists every incoming message to MongoDB.
     * Uses manual acknowledgment for reliability.
     */
    @KafkaListener(
        topics = "chat-messages",
        groupId = "message-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(
            @Payload ChatMessage message,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        try {
            log.debug("Consuming message | partition={} | offset={} | room={} | sender={}",
                partition, offset, message.getRoomId(), message.getSender());

            // Only persist actual chat / join / leave messages (skip typing indicators)
            if (shouldPersist(message.getType())) {
                MessageDocument doc = toDocument(message);
                messageRepository.save(doc);
                log.debug("Persisted message id={} to MongoDB", doc.getId());
            }

            // Acknowledge offset after successful processing
            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Failed to process Kafka message | room={} | error={}",
                message.getRoomId(), e.getMessage(), e);
            // Don't acknowledge — message will be retried
        }
    }

    private boolean shouldPersist(String type) {
        if (type == null) return true;
        return switch (type.toUpperCase()) {
            case "TYPING", "DELIVERED", "READ" -> false;
            default -> true;
        };
    }

    private MessageDocument toDocument(ChatMessage msg) {
        Instant timestamp;
        try {
            timestamp = msg.getTimestamp() != null
                ? Instant.parse(msg.getTimestamp())
                : Instant.now();
        } catch (Exception e) {
            timestamp = Instant.now();
        }

        return MessageDocument.builder()
            .id(msg.getId())
            .roomId(msg.getRoomId())
            .senderId(msg.getSenderId())
            .sender(msg.getSender())
            .displayName(msg.getDisplayName())
            .avatarUrl(msg.getAvatarUrl())
            .content(msg.getContent())
            .timestamp(timestamp)
            .messageType(msg.getType())
            .deleted(false)
            .build();
    }
}
