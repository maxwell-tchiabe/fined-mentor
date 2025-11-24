package com.fined.mentor.chat.entity;

import com.fined.mentor.quiz.entity.Quiz;
import com.fined.mentor.quiz.entity.QuizState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chat_sessions")
public class ChatSession {
    @Id
    private String id;
    private String title;
    private LocalDateTime createdAt;
    private boolean active;
    private String userId;

    @Transient
    private List<ChatMessage> messages;
    @Transient
    private Quiz quiz;
    @Transient
    private QuizState quizState;
}
