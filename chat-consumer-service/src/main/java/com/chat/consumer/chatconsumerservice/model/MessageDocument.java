package com.chat.consumer.chatconsumerservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
// Compound index for efficient room history queries with pagination
@CompoundIndex(name = "room_timestamp_idx", def = "{'roomId': 1, 'timestamp': -1}")
public class MessageDocument {

    @Id
    private String id;

    @Indexed
    private String roomId;

    private String senderId;
    private String sender;
    private String displayName;
    private String avatarUrl;
    private String content;
    private Instant timestamp;
    private String messageType;  // CHAT, JOIN, LEAVE
    private boolean deleted;
    private Instant deletedAt;
}
