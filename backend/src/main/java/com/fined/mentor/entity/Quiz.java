package com.fined.mentor.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "quizzes")
public class Quiz {
    @Id
    private String id;
    private String topic;
    private List<QuizQuestion> questions;
    private String chatSessionId;
    private LocalDateTime createdAt;
}