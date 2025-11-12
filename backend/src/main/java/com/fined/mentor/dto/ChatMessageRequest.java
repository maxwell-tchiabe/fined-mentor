package com.fined.mentor.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatMessageRequest {
    @NotBlank(message = "Message cannot be blank")
    private String message;

    @NotBlank(message = "Chat session ID cannot be blank")
    private String chatSessionId;
}
