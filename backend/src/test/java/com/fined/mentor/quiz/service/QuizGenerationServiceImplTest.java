package com.fined.mentor.quiz.service;

import com.fined.mentor.quiz.entity.Quiz;
import com.fined.mentor.quiz.exception.QuizValidationException;
import com.fined.mentor.tavily.TavilySearchTool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuizGenerationServiceImplTest {

    @Mock
    private ChatModel chatModel;

    @Mock
    private TavilySearchTool tavilySearchTool;

    @Mock
    private TopicValidatorService topicValidatorService;

    private QuizGenerationServiceImpl quizGenerationService;

    private final String validJsonString = """
            {
              "topic": "Investment",
              "questions": [
                {
                  "question": "What is an ETF?",
                  "type": "MULTIPLE_CHOICE",
                  "options": ["Fund", "Bank", "Loan", "Card"],
                  "correctAnswer": "Fund",
                  "explanation": "Exchange Traded Fund"
                }
              ]
            }
            """;

    @BeforeEach
    void setUp() {
        ChatClient.Builder builder = ChatClient.builder(chatModel);
        quizGenerationService = new QuizGenerationServiceImpl(builder, tavilySearchTool, topicValidatorService);
    }

    @Test
    void generateQuiz_Success() {
        when(topicValidatorService.isValidTopic("Investment")).thenReturn(true);
        
        ChatResponse mockResponse = new ChatResponse(List.of(new Generation(new AssistantMessage(validJsonString))));
        when(chatModel.call(any(Prompt.class))).thenReturn(mockResponse);

        Quiz quiz = quizGenerationService.generateQuiz("Investment");

        assertNotNull(quiz);
        assertEquals("Investment", quiz.getTopic());
        assertEquals(1, quiz.getQuestions().size());
        assertEquals("What is an ETF?", quiz.getQuestions().get(0).getQuestion());
    }

    @Test
    void generateQuiz_InvalidTopic() {
        when(topicValidatorService.isValidTopic("Cooking")).thenReturn(false);
        when(topicValidatorService.getInvalidTopicMessage("Cooking")).thenReturn("Invalid topic.");

        assertThrows(QuizValidationException.class, () -> quizGenerationService.generateQuiz("Cooking"));
    }

    @Test
    void streamQuizGeneration_Success() {
        when(topicValidatorService.isValidTopic("Investment")).thenReturn(true);
        
        ChatResponse mockResponse1 = new ChatResponse(List.of(new Generation(new AssistantMessage("part1"))));
        ChatResponse mockResponse2 = new ChatResponse(List.of(new Generation(new AssistantMessage("part2"))));
        
        when(chatModel.stream(any(Prompt.class))).thenReturn(Flux.just(mockResponse1, mockResponse2));

        Flux<String> stream = quizGenerationService.streamQuizGeneration("Investment");

        StepVerifier.create(stream)
                .expectNext("part1")
                .expectNext("part2")
                .verifyComplete();
    }
}
