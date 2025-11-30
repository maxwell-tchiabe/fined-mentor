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
                    You are FinEd Mentor, an expert in finance, real estate, and investment.
                    Generate a 5-question quiz on the topic of "{topic}".
                    The quiz should be suitable for a beginner.
                    The questions should be a mix of multiple-choice and true/false.
                    Provide a concise explanation for each correct answer.
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
