package com.fined.mentor.quiz.service;

import com.fined.mentor.quiz.dto.GeneratedQuizDTO;
import com.fined.mentor.quiz.entity.Quiz;
import com.fined.mentor.quiz.exception.QuizGenerationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
class QuizGenerationServiceImpl implements QuizGenerationService {

    private final ChatClient chatClient;

    public QuizGenerationServiceImpl(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @Override
    public Quiz generateQuiz(String topic) {
        try {
            log.info("Generating quiz for topic: {}", topic);

            // Use BeanOutputConverter with DTO to avoid ID generation issues
            BeanOutputConverter<GeneratedQuizDTO> outputConverter = new BeanOutputConverter<>(GeneratedQuizDTO.class);

            String promptString = """
                    Generate a 5-question beginner quiz on "{topic}" (finance/real estate/investment domain).

                    REASONING STEPS:
                    1. Identify 5 core concepts beginners should know about "{topic}"
                    2. For each concept, determine if multiple-choice or true/false fits best
                    3. Ensure balanced difficulty progression (easiest to moderate)
                    4. Write 1-2 sentence explanations using simple terms

                    EXAMPLE (DO NOT COPY):
                    Question: "What is compound interest?"
                    Type: Multiple Choice
                    Options: ["Interest on interest earned", "Simple interest rate", "Bank fee structure", "Investment loss"]
                    Correct: "Interest on interest earned"
                    Explanation: "Compound interest grows your money faster because you earn interest on both your initial deposit and previously earned interest."

                    REQUIREMENTS:
                    - Question mix: 3 multiple-choice + 2 true/false
                    - Each explanation: 1-2 sentences maximum
                    - Vocabulary: 8th-grade reading level

                    AVOID:
                    - Complex calculations or formulas
                    - Jargon without definitions
                    - Trick questions or ambiguous phrasing
                    - Questions requiring current market data

                    {format}
                    """;

            PromptTemplate promptTemplate = new PromptTemplate(promptString);
            Prompt prompt = promptTemplate.create(Map.of("topic", topic, "format", outputConverter.getFormat()));

            String content = chatClient.prompt(prompt).call().content();

            GeneratedQuizDTO generatedQuiz = outputConverter.convert(content);

            Quiz quiz = Quiz.builder()
                    .topic(generatedQuiz.getTopic())
                    .questions(generatedQuiz.getQuestions())
                    .createdAt(java.time.LocalDateTime.now())
                    .build();

            log.debug("Successfully generated quiz with {} questions", quiz.getQuestions().size());

            return quiz;

        } catch (Exception e) {
            log.error("Failed to generate quiz for topic: {}", topic, e);
            throw new QuizGenerationException("Could not generate a valid quiz. Please try again.", e);
        }
    }

}
