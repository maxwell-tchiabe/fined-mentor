package com.fined.mentor.quiz.repository;

import com.fined.mentor.quiz.entity.QuizState;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface QuizStateRepository extends MongoRepository<QuizState, String> {
    Optional<QuizState> findByChatSessionId(String chatSessionId);
    Optional<QuizState> findByQuizId(String quizId);
}