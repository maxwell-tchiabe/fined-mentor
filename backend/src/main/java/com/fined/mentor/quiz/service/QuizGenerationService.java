package com.fined.mentor.quiz.service;

import com.fined.mentor.quiz.entity.Quiz;

public interface QuizGenerationService {
    Quiz generateQuiz(String topic);
}
