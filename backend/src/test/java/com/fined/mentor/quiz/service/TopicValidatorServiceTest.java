package com.fined.mentor.quiz.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TopicValidatorServiceTest {

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.CallResponseSpec responseSpec;

    @InjectMocks
    private TopicValidatorService topicValidatorService;

    @BeforeEach
    void setUp() {
        // We'll mock the builder's build() method in individual tests if needed,
        // but often it's needed for the validateWithAI calls.
    }

    @Test
    void isValidTopic_KeywordMatch_Success() {
        assertTrue(topicValidatorService.isValidTopic("Investment strategies"));
        assertTrue(topicValidatorService.isValidTopic("Immobilienmarkt"));
        assertTrue(topicValidatorService.isValidTopic("Bourse en France"));
    }

    @Test
    void isValidTopic_EmptyTopic_ReturnsFalse() {
        assertFalse(topicValidatorService.isValidTopic(null));
        assertFalse(topicValidatorService.isValidTopic("   "));
    }

    @Test
    void isValidTopic_AIValidation_Success() {
        // Mock ChatClient fluent API
        when(chatClientBuilder.build()).thenReturn(chatClient);
        when(chatClient.prompt(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenReturn(" YES ");

        // Topic that doesn't contain the hardcoded keywords
        assertTrue(topicValidatorService.isValidTopic("Macroeconomics and inflation"));
        
        verify(chatClientBuilder).build();
        verify(chatClient).prompt(anyString());
    }

    @Test
    void isValidTopic_AIValidation_Failure() {
        when(chatClientBuilder.build()).thenReturn(chatClient);
        when(chatClient.prompt(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenReturn("NO");

        assertFalse(topicValidatorService.isValidTopic("How to bake cookie"));
    }

    @Test
    void isValidTopic_AIException_ReturnsFalse() {
        when(chatClientBuilder.build()).thenReturn(chatClient);
        when(chatClient.prompt(anyString())).thenThrow(new RuntimeException("AI service down"));

        assertFalse(topicValidatorService.isValidTopic("Something valid but needing AI"));
    }

    @Test
    void getInvalidTopicMessage_French() {
        String message = topicValidatorService.getInvalidTopicMessage("La cuisine française");
        assertTrue(message.contains("Le sujet") && message.contains("pas lié à la finance"));
    }

    @Test
    void getInvalidTopicMessage_German() {
        String message = topicValidatorService.getInvalidTopicMessage("Das Wetter heute");
        assertTrue(message.contains("Das Thema") && message.contains("bezieht sich nicht auf Finanzen"));
    }

    @Test
    void getInvalidTopicMessage_DefaultEnglish() {
        String message = topicValidatorService.getInvalidTopicMessage("Tennis coach");
        assertTrue(message.contains("The topic") && message.contains("is not related to finance"));
    }

    @Test
    void testValidateWithAI_DirectAccess() {
        when(chatClientBuilder.build()).thenReturn(chatClient);
        when(chatClient.prompt(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenReturn("YES");

        boolean result = ReflectionTestUtils.invokeMethod(topicValidatorService, "validateWithAI", "Test Topic");
        assertTrue(result);
    }
}
