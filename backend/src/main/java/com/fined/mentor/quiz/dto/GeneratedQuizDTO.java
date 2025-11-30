package com.fined.mentor.quiz.dto;

import com.fined.mentor.quiz.entity.QuizQuestion;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedQuizDTO {
    private String topic;
    private List<QuizQuestion> questions;
}
