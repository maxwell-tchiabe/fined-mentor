package com.fined.mentor.quiz.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "quiz_states")
public class QuizState {
    @Id
    private String id;
    private String chatSessionId;
    private String quizId;
    private int currentQuestionIndex;
    private Map<Integer, String> userAnswers;
    private Map<Integer, Boolean> isSubmitted;
    private int score;
    private boolean isFinished;
}
