package com.chat.consumer.chatconsumerservice.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {
    private String id;
    private String roomId;
    private String senderId;
    private String sender;
    private String displayName;
    private String avatarUrl;
    private String content;
    private String timestamp;
    private String type;  // Stored as String to avoid enum deserialization issues across services
}
