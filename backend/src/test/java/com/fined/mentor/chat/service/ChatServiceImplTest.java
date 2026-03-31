package com.fined.mentor.chat.service;

import com.fined.mentor.chat.entity.ChatMessage;
import com.fined.mentor.chat.entity.ChatSession;
import com.fined.mentor.chat.exception.ChatException;
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

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceImplTest {

    @Mock
    private ChatModel chatModel;

    @Mock
    private ChatSessionService chatSessionService;

    @Mock
    private ChatMessageService chatMessageService;

    @Mock
    private TavilySearchTool tavilySearchTool;

    private ChatServiceImpl chatService;

    @BeforeEach
    void setUp() {
        ChatClient.Builder builder = ChatClient.builder(chatModel);
        chatService = new ChatServiceImpl(builder, chatSessionService, chatMessageService, tavilySearchTool);
    }

    @Test
    void getChatResponse_Success() {
        String sessionId = "session1";
        String userMessageText = "What is a 401k?";
        String aiResponseText = "A 401k is a retirement savings account.";

        ChatMessage userMessage = ChatMessage.builder()
                .chatSessionId(sessionId)
                .role(ChatMessage.Role.USER)
                .text(userMessageText)
                .build();
                
        ChatMessage aiMessage = ChatMessage.builder()
                .chatSessionId(sessionId)
                .role(ChatMessage.Role.MODEL)
                .text(aiResponseText)
                .build();

        doNothing().when(chatSessionService).findById(sessionId);
        when(chatMessageService.saveMessage(any(ChatMessage.class))).thenReturn(userMessage).thenReturn(aiMessage);
        when(chatMessageService.getMessagesBySessionId(sessionId)).thenReturn(Collections.singletonList(userMessage));

        ChatResponse mockResponse = new ChatResponse(List.of(new Generation(new AssistantMessage(aiResponseText))));
        when(chatModel.call(any(Prompt.class))).thenReturn(mockResponse);

        ChatMessage response = chatService.getChatResponse(sessionId, userMessageText);

        assertNotNull(response);
        assertEquals(aiResponseText, response.getText());
        assertEquals(ChatMessage.Role.MODEL, response.getRole());
        
        verify(chatMessageService, times(2)).saveMessage(any(ChatMessage.class));
    }

    @Test
    void getChatResponse_Exception() {
        String sessionId = "session1";
        doThrow(new RuntimeException("DB Error")).when(chatSessionService).findById(sessionId);

        assertThrows(ChatException.class, () -> chatService.getChatResponse(sessionId, "Hello"));
    }

    @Test
    void streamChatResponse_Success() {
        String sessionId = "session1";
        String userMessageText = "Hello";
        
        ChatMessage userMessage = ChatMessage.builder()
                .chatSessionId(sessionId)
                .role(ChatMessage.Role.USER)
                .text(userMessageText)
                .build();

        doNothing().when(chatSessionService).findById(sessionId);
        when(chatMessageService.saveMessage(any(ChatMessage.class))).thenReturn(userMessage);
        when(chatMessageService.getMessagesBySessionId(sessionId)).thenReturn(Collections.singletonList(userMessage));

        ChatResponse mockResponse1 = new ChatResponse(List.of(new Generation(new AssistantMessage("Hello"))));
        ChatResponse mockResponse2 = new ChatResponse(List.of(new Generation(new AssistantMessage(" there!"))));
        
        when(chatModel.stream(any(Prompt.class))).thenReturn(Flux.just(mockResponse1, mockResponse2));

        Flux<String> responseFlux = chatService.streamChatResponse(sessionId, userMessageText);

        StepVerifier.create(responseFlux)
                .expectNext("Hello")
                .expectNext(" there!")
                .verifyComplete();
                
        verify(chatMessageService, times(2)).saveMessage(any(ChatMessage.class));
    }

    @Test
    void streamGuestChatResponse_Success() {
        String userMessageText = "What is ETF?";

        ChatMessage historyMessage = ChatMessage.builder()
                .role(ChatMessage.Role.USER)
                .text("Hello")
                .build();
        List<ChatMessage> history = Collections.singletonList(historyMessage);

        ChatResponse mockResponse1 = new ChatResponse(List.of(new Generation(new AssistantMessage("ETF is"))));
        ChatResponse mockResponse2 = new ChatResponse(List.of(new Generation(new AssistantMessage(" a fund."))));
        
        when(chatModel.stream(any(Prompt.class))).thenReturn(Flux.just(mockResponse1, mockResponse2));

        Flux<String> responseFlux = chatService.streamGuestChatResponse(history, userMessageText);

        StepVerifier.create(responseFlux)
                .expectNext("ETF is")
                .expectNext(" a fund.")
                .verifyComplete();
    }

    @Test
    void getChatHistory_Success() {
        String sessionId = "session1";
        when(chatMessageService.getMessagesBySessionId(sessionId)).thenReturn(Collections.emptyList());

        List<ChatMessage> history = chatService.getChatHistory(sessionId);

        assertNotNull(history);
        assertTrue(history.isEmpty());
    }

    @Test
    void createChatSession_Success() {
        ChatSession session = new ChatSession();
        when(chatSessionService.createSession("Title", "user1")).thenReturn(session);

        ChatSession created = chatService.createChatSession("Title", "user1");

        assertNotNull(created);
    }

    @Test
    void deactivateChatSession_Success() {
        doNothing().when(chatSessionService).deactivateSession("session1");

        chatService.deactivateChatSession("session1");

        verify(chatSessionService).deactivateSession("session1");
    }
}
