package com.chat.consumer.chatconsumerservice.repository;


import com.chat.consumer.chatconsumerservice.model.MessageDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<MessageDocument, String> {

    /**
     * Paginated room history (newest first by default via index)
     */
    Page<MessageDocument> findByRoomIdAndDeletedFalseOrderByTimestampDesc(String roomId, Pageable pageable);

    /**
     * Messages before a given timestamp (for "load more" / cursor pagination)
     */
    List<MessageDocument> findByRoomIdAndTimestampBeforeAndDeletedFalseOrderByTimestampDesc(
        String roomId, Instant before, Pageable pageable);

    /**
     * Count unread messages since a timestamp
     */
    long countByRoomIdAndTimestampAfterAndSenderNotAndDeletedFalse(
        String roomId, Instant since, String senderToExclude);

    /**
     * Search messages in a room by keyword
     */
    @Query("{ 'roomId': ?0, 'content': { $regex: ?1, $options: 'i' }, 'deleted': false }")
    List<MessageDocument> searchByRoomIdAndContent(String roomId, String keyword, Pageable pageable);

    /**
     * Delete all messages in a room
     */
    void deleteAllByRoomId(String roomId);
}
