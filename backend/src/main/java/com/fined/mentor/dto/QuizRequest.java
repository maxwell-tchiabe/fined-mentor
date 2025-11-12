package com.fined.mentor.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class QuizRequest {
    @NotBlank(message = "Topic cannot be blank")
    private String topic;

    @NotBlank(message = "Chat session ID cannot be blank")
    private String chatSessionId;
}
