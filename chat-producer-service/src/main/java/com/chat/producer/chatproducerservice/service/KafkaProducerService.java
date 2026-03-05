package com.chat.producer.chatproducerservice.service;


import com.chat.producer.chatproducerservice.model.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;


@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private static final String TOPIC = "chat-messages";

    private final KafkaTemplate<String, ChatMessage> kafkaTemplate;

    /**
     * Publishes a ChatMessage to the Kafka topic.
     * Key = roomId ensures messages in the same room go to the same partition (ordered delivery).
     */
    public void publishMessage(ChatMessage message) {
        CompletableFuture<SendResult<String, ChatMessage>> future =
            kafkaTemplate.send(TOPIC, message.getRoomId(), message);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.debug("Message published to Kafka | topic={} | partition={} | offset={} | room={}",
                    TOPIC,
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset(),
                    message.getRoomId()
                );
            } else {
                log.error("Failed to publish message to Kafka | room={} | error={}",
                    message.getRoomId(), ex.getMessage());
            }
        });
    }
}
