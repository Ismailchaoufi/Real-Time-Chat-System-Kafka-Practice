package com.chat.producer.chatproducerservice.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    private String id;
    private String roomId;
    private String senderId;
    private String sender;        // username
    private String displayName;
    private String avatarUrl;
    private String content;
    private String timestamp;
    private MessageType type;

    public enum MessageType {
        CHAT,
        JOIN,
        LEAVE,
        TYPING,
        DELIVERED,
        READ
    }
}
