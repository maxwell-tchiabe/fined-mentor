package com.fined.mentor.quiz.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GuestQuizRequest {
    @NotBlank(message = "Topic cannot be blank")
    private String topic;
}
