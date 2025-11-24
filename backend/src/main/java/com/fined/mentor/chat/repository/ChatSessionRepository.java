package com.fined.mentor.chat.repository;

import com.fined.mentor.chat.entity.ChatSession;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatSessionRepository extends MongoRepository<ChatSession, String> {
    Optional<ChatSession> findByIdAndActiveTrue(String id);

    List<ChatSession> findByActiveTrueOrderByCreatedAtDesc();

    List<ChatSession> findByUserIdAndActiveTrueOrderByCreatedAtDesc(String userId);
}
