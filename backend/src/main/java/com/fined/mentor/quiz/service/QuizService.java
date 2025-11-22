package com.fined.mentor.quiz.service;

import com.fined.mentor.quiz.entity.Quiz;
import com.fined.mentor.quiz.entity.QuizState;
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
