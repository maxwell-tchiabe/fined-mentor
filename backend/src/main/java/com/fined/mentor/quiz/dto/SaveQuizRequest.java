package com.fined.mentor.quiz.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaveQuizRequest {
    @NotBlank(message = "Topic is required")
    private String topic;

    @NotBlank(message = "Chat session ID is required")
    private String chatSessionId;

    @NotBlank(message = "Quiz JSON is required")
    private String quizJson;
}
