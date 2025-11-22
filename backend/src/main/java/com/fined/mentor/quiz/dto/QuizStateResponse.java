package com.fined.mentor.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizStateResponse {
    private String id;
    private String quizId;
    private String chatSessionId;
    private int currentQuestionIndex;
    private Map<Integer, String> userAnswers;
    private Map<Integer, Boolean> isSubmitted;    private Integer score;
    private boolean isFinished;
}
