package com.fined.mentor.quiz.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizQuestion {
    private String question;
    private QuestionType type;
    private String[] options;
    private String correctAnswer;
    private String explanation;

    public enum QuestionType {
        MULTIPLE_CHOICE, TRUE_FALSE
    }
}