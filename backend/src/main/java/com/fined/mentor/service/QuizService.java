package com.fined.mentor.service;

import com.fined.mentor.entity.Quiz;
import com.fined.mentor.entity.QuizState;
import org.springframework.transaction.annotation.Transactional;

public interface QuizService {
    Quiz generateQuiz(String topic, String chatSessionId);

    @Transactional
    QuizState finishQuiz(String quizStateId);

    QuizState startQuiz(String quizId, String chatSessionId);
    QuizState submitAnswer(String quizStateId, int questionIndex, String answer);
    Quiz getQuizBySessionId(String sessionId);
    QuizState getQuizState(String quizStateId);
    QuizState getQuizStateBySessionId(String sessionId);
}
