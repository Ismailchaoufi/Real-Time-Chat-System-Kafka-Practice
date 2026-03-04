package com.chat.producer.chatproducerservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// ChatMessage.java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage {
    private String messageId;      // UUID
    private String senderId;
    private String recipientId;    // or roomId for group chat
    private String content;
    private MessageType type;      // TEXT, IMAGE, JOIN, LEAVE
    private LocalDateTime timestamp;

    public enum MessageType { TEXT, IMAGE, JOIN, LEAVE }
    // constructors, getters, setters
}
