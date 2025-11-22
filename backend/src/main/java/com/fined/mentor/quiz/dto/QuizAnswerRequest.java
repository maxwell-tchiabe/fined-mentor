package com.fined.mentor.quiz.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QuizAnswerRequest {
    @NotBlank(message = "Quiz state ID cannot be blank")
    private String quizStateId;

    @NotNull(message = "Question index cannot be null")
    private Integer questionIndex;

    @NotBlank(message = "Answer cannot be blank")
    private String answer;
}
