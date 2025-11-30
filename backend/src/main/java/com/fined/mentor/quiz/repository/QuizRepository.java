package com.fined.mentor.quiz.repository;

import com.fined.mentor.quiz.entity.Quiz;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface QuizRepository extends MongoRepository<Quiz, String> {
    Optional<Quiz> findByChatSessionId(String chatSessionId);

    Optional<Quiz> findFirstByChatSessionIdOrderByCreatedAtDesc(String chatSessionId);
}