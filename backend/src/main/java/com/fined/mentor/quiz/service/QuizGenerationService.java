package com.fined.mentor.quiz.service;

import com.fined.mentor.quiz.entity.Quiz;

import reactor.core.publisher.Flux;

public interface QuizGenerationService {
    Quiz generateQuiz(String topic);

    Flux<String> streamQuizGeneration(String topic);
}
