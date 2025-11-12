package com.fined.mentor.dto;

import com.fined.mentor.entity.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {
    private String id;
    private ChatMessage.Role role;
    private String text;
    private LocalDateTime timestamp;
    private String chatSessionId;
}