package com.fined.mentor.chat.service;

import com.fined.mentor.chat.entity.ChatMessage;
import com.fined.mentor.chat.entity.ChatSession;
import com.fined.mentor.chat.exception.ChatMessageException;
import com.fined.mentor.chat.exception.ChatMessageNotFoundException;
import com.fined.mentor.chat.exception.ChatSessionNotFoundException;
import com.fined.mentor.chat.repository.ChatMessageRepository;
import com.fined.mentor.chat.repository.ChatSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatMessageServiceImplTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private ChatSessionRepository chatSessionRepository;

    @InjectMocks
    private ChatMessageServiceImpl chatMessageService;

    private ChatSession sampleSession;
    private ChatMessage sampleMessage;

    @BeforeEach
    void setUp() {
        sampleSession = ChatSession.builder()
                .id("session1")
                .active(true)
                .build();

        sampleMessage = ChatMessage.builder()
                .id("message1")
                .chatSessionId("session1")
                .text("Hello AI")
                .role(ChatMessage.Role.USER)
                .timestamp(Instant.now())
                .build();
    }

    @Test
    void saveMessage_Success() {
        when(chatSessionRepository.findByIdAndActiveTrue("session1")).thenReturn(Optional.of(sampleSession));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(sampleMessage);

        ChatMessage savedMessage = chatMessageService.saveMessage(sampleMessage);

        assertNotNull(savedMessage);
        assertEquals("message1", savedMessage.getId());
        verify(chatMessageRepository).save(any(ChatMessage.class));
    }

    @Test
    void saveMessage_SessionNotFound() {
        when(chatSessionRepository.findByIdAndActiveTrue("session1")).thenReturn(Optional.empty());

        assertThrows(ChatMessageException.class, () -> chatMessageService.saveMessage(sampleMessage));
    }

    @Test
    void getMessage_Success() {
        when(chatMessageRepository.findById("message1")).thenReturn(Optional.of(sampleMessage));

        ChatMessage message = chatMessageService.getMessage("message1");

        assertNotNull(message);
        assertEquals("message1", message.getId());
    }

    @Test
    void getMessage_NotFound() {
        when(chatMessageRepository.findById("unknown")).thenReturn(Optional.empty());

        assertThrows(ChatMessageNotFoundException.class, () -> chatMessageService.getMessage("unknown"));
    }

    @Test
    void getMessagesBySessionId_Success() {
        when(chatSessionRepository.findByIdAndActiveTrue("session1")).thenReturn(Optional.of(sampleSession));
        when(chatMessageRepository.findByChatSessionIdOrderByTimestampAsc("session1"))
                .thenReturn(Collections.singletonList(sampleMessage));

        List<ChatMessage> messages = chatMessageService.getMessagesBySessionId("session1");

        assertFalse(messages.isEmpty());
        assertEquals(1, messages.size());
    }

    @Test
    void getMessagesBySessionId_SessionNotFound() {
        when(chatSessionRepository.findByIdAndActiveTrue("session1")).thenReturn(Optional.empty());

        assertThrows(ChatMessageException.class, () -> chatMessageService.getMessagesBySessionId("session1"));
    }

    @Test
    void deleteMessage_Success() {
        when(chatMessageRepository.findById("message1")).thenReturn(Optional.of(sampleMessage));
        doNothing().when(chatMessageRepository).delete(sampleMessage);

        chatMessageService.deleteMessage("message1");

        verify(chatMessageRepository).delete(sampleMessage);
    }

    @Test
    void deleteAllMessagesBySessionId_Success() {
        when(chatSessionRepository.findById("session1")).thenReturn(Optional.of(sampleSession));
        doNothing().when(chatMessageRepository).deleteByChatSessionId("session1");

        chatMessageService.deleteAllMessagesBySessionId("session1");

        verify(chatMessageRepository).deleteByChatSessionId("session1");
    }

    @Test
    void updateMessage_Success() {
        when(chatMessageRepository.findById("message1")).thenReturn(Optional.of(sampleMessage));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(sampleMessage);

        ChatMessage updated = chatMessageService.updateMessage("message1", "Updated Hello AI");

        assertEquals("Updated Hello AI", updated.getText());
        verify(chatMessageRepository).save(sampleMessage);
    }

    @Test
    void updateMessage_ModelRole_ThrowsException() {
        sampleMessage.setRole(ChatMessage.Role.MODEL);
        when(chatMessageRepository.findById("message1")).thenReturn(Optional.of(sampleMessage));

        assertThrows(ChatMessageException.class, () -> chatMessageService.updateMessage("message1", "Text"));
    }

    @Test
    void updateMessage_EmptyText_ThrowsException() {
        when(chatMessageRepository.findById("message1")).thenReturn(Optional.of(sampleMessage));

        assertThrows(ChatMessageException.class, () -> chatMessageService.updateMessage("message1", "  "));
    }
    @Test
    void saveMessage_DatabaseError_ThrowsException() {
        when(chatSessionRepository.findByIdAndActiveTrue(anyString())).thenReturn(Optional.of(sampleSession));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenThrow(new RuntimeException("DB error"));

        assertThrows(ChatMessageException.class, () -> chatMessageService.saveMessage(sampleMessage));
    }

    @Test
    void getMessagesBySessionId_DatabaseError_ThrowsException() {
        when(chatSessionRepository.findByIdAndActiveTrue(anyString())).thenReturn(Optional.of(sampleSession));
        when(chatMessageRepository.findByChatSessionIdOrderByTimestampAsc(anyString())).thenThrow(new RuntimeException("DB error"));

        assertThrows(ChatMessageException.class, () -> chatMessageService.getMessagesBySessionId("session1"));
    }

    @Test
    void deleteMessage_DatabaseError_ThrowsException() {
        when(chatMessageRepository.findById(anyString())).thenReturn(Optional.of(sampleMessage));
        doThrow(new RuntimeException("DB error")).when(chatMessageRepository).delete(any(ChatMessage.class));

        assertThrows(ChatMessageException.class, () -> chatMessageService.deleteMessage("message1"));
    }

    @Test
    void deleteAllMessagesBySessionId_DatabaseError_ThrowsException() {
        when(chatSessionRepository.findById(anyString())).thenReturn(Optional.of(sampleSession));
        doThrow(new RuntimeException("DB error")).when(chatMessageRepository).deleteByChatSessionId(anyString());

        assertThrows(ChatMessageException.class, () -> chatMessageService.deleteAllMessagesBySessionId("session1"));
    }
}
