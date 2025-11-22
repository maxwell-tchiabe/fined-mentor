package com.fined.mentor.quiz.dto;

import com.fined.mentor.quiz.entity.QuizQuestion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizResponse {
    private String id;
    private String topic;
    private List<QuizQuestion> questions;
    private LocalDateTime createdAt;
}