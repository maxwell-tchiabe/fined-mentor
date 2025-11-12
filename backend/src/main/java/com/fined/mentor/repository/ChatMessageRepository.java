package com.fined.mentor.repository;

import com.fined.mentor.entity.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    List<ChatMessage> findByChatSessionIdOrderByTimestampAsc(String chatSessionId);
    void deleteByChatSessionId(String chatSessionId);
}