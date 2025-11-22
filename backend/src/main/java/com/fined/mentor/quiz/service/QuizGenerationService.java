package com.fined.mentor.quiz.service;

import com.fined.mentor.quiz.entity.Quiz;
import com.fined.mentor.quiz.entity.QuizQuestion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

import java.util.List;

public interface QuizGenerationService {
    Quiz generateQuiz(String topic);
}
