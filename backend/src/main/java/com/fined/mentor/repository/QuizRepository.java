package com.fined.mentor.repository;

import com.fined.mentor.entity.Quiz;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface QuizRepository extends MongoRepository<Quiz, String> {
    Optional<Quiz> findByChatSessionId(String chatSessionId);
}