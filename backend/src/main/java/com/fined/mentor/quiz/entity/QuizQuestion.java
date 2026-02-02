package com.fined.mentor.quiz.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizQuestion {

    @JsonProperty(required = true)
    @JsonPropertyDescription("The question text to be asked")
    private String question;

    @JsonProperty(required = true)
    @JsonPropertyDescription("The type of question: MULTIPLE_CHOICE or TRUE_FALSE")
    private QuestionType type;

    @JsonProperty(required = true)
    @JsonPropertyDescription("Array of answer options. MUST contain exactly 4 options for MULTIPLE_CHOICE questions, and exactly 2 options ['True', 'False'] for TRUE_FALSE questions. This field is MANDATORY and cannot be null or empty.")
    private String[] options;

    @JsonProperty(required = true)
    @JsonPropertyDescription("The correct answer, which MUST be one of the strings from the options array")
    private String correctAnswer;

    @JsonProperty(required = true)
    @JsonPropertyDescription("A brief 1-2 sentence explanation of why the correct answer is correct")
    private String explanation;

    public enum QuestionType {
        MULTIPLE_CHOICE, TRUE_FALSE
    }
}