package com.fined.mentor.quiz.service;

import com.fined.mentor.quiz.entity.Quiz;
import com.fined.mentor.quiz.entity.QuizState;

import reactor.core.publisher.Flux;

import org.springframework.transaction.annotation.Transactional;

public interface QuizService {
    Quiz generateQuiz(String topic, String chatSessionId);

    Flux<String> streamQuizGeneration(String topic);

    Quiz saveStreamedQuiz(String topic, String chatSessionId, String quizJson);

    @Transactional
    QuizState finishQuiz(String quizStateId);

    QuizState startQuiz(String quizId, String chatSessionId);

    QuizState submitAnswer(String quizStateId, int questionIndex, String answer);

    Quiz getQuizBySessionId(String sessionId);

    QuizState getQuizState(String quizStateId);

    QuizState getQuizStateBySessionId(String sessionId);

    QuizState updateCurrentQuestionIndex(String quizStateId, int index);
}
