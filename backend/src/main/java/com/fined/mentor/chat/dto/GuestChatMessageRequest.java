package com.fined.mentor.chat.dto;

import com.fined.mentor.chat.entity.ChatMessage;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class GuestChatMessageRequest {
    @NotBlank(message = "Message cannot be blank")
    private String message;

    private List<ChatMessage> history;
}
