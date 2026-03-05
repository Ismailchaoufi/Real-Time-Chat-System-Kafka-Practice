package com.chat.consumer.chatconsumerservice.controller;

import com.chat.consumer.chatconsumerservice.model.MessageDocument;
import com.chat.consumer.chatconsumerservice.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageRepository messageRepository;

    /**
     * GET /api/messages/{roomId}/history?page=0&size=50
     * Returns paginated message history for a room (newest first)
     */
    @GetMapping("/{roomId}/history")
    public ResponseEntity<Page<MessageDocument>> getRoomHistory(
            @PathVariable String roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        Page<MessageDocument> messages =
            messageRepository.findByRoomIdAndDeletedFalseOrderByTimestampDesc(roomId, pageable);

        log.debug("Fetched {} messages for room {}", messages.getTotalElements(), roomId);
        return ResponseEntity.ok(messages);
    }

    /**
     * GET /api/messages/{roomId}/before?timestamp=...&limit=50
     * Cursor-based pagination – load messages before a given timestamp
     */
    @GetMapping("/{roomId}/before")
    public ResponseEntity<List<MessageDocument>> getMessagesBefore(
            @PathVariable String roomId,
            @RequestParam String timestamp,
            @RequestParam(defaultValue = "50") int limit) {

        Instant before = Instant.parse(timestamp);
        Pageable pageable = PageRequest.of(0, Math.min(limit, 100));

        List<MessageDocument> messages =
            messageRepository.findByRoomIdAndTimestampBeforeAndDeletedFalseOrderByTimestampDesc(
                roomId, before, pageable);

        return ResponseEntity.ok(messages);
    }

    /**
     * GET /api/messages/{roomId}/search?q=keyword&limit=20
     * Full-text search in room
     */
    @GetMapping("/{roomId}/search")
    public ResponseEntity<List<MessageDocument>> searchMessages(
            @PathVariable String roomId,
            @RequestParam String q,
            @RequestParam(defaultValue = "20") int limit) {

        Pageable pageable = PageRequest.of(0, Math.min(limit, 50));
        List<MessageDocument> results =
            messageRepository.searchByRoomIdAndContent(roomId, q, pageable);

        return ResponseEntity.ok(results);
    }

    /**
     * DELETE /api/messages/{messageId}
     * Soft-delete a message
     */
    @DeleteMapping("/{messageId}")
    public ResponseEntity<Map<String, String>> deleteMessage(
            @PathVariable String messageId,
            @RequestHeader("X-Auth-Username") String username) {

        return messageRepository.findById(messageId)
            .map(msg -> {
                if (!msg.getSender().equals(username)) {
                    return ResponseEntity.status(403)
                        .<Map<String, String>>body(Map.of("error", "You can only delete your own messages"));
                }
                msg.setDeleted(true);
                msg.setDeletedAt(Instant.now());
                messageRepository.save(msg);
                log.info("Message {} soft-deleted by {}", messageId, username);
                return ResponseEntity.ok(Map.of("status", "deleted", "id", messageId));
            })
            .orElse(ResponseEntity.notFound().<Map<String, String>>build());
    }

    /**
     * GET /api/messages/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "message-service"));
    }
}
