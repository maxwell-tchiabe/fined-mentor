package com.fined.mentor.chat.dto;

import com.fined.mentor.chat.entity.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {
    private String id;
    private ChatMessage.Role role;
    private String text;
    private Instant timestamp;
    private String chatSessionId;
}