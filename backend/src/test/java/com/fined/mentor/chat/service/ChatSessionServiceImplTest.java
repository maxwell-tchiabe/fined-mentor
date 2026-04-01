package com.fined.mentor.chat.service;

import com.fined.mentor.chat.entity.ChatMessage;
import com.fined.mentor.chat.entity.ChatSession;
import com.fined.mentor.chat.exception.ChatSessionException;
import com.fined.mentor.chat.exception.ChatSessionNotFoundException;
import com.fined.mentor.chat.repository.ChatMessageRepository;
import com.fined.mentor.chat.repository.ChatSessionRepository;
import com.fined.mentor.quiz.entity.Quiz;
import com.fined.mentor.quiz.entity.QuizState;
import com.fined.mentor.quiz.repository.QuizRepository;
import com.fined.mentor.quiz.repository.QuizStateRepository;
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
class ChatSessionServiceImplTest {

        @Mock
        private ChatSessionRepository chatSessionRepository;

        @Mock
        private ChatMessageRepository chatMessageRepository;

        @Mock
        private QuizRepository quizRepository;

        @Mock
        private QuizStateRepository quizStateRepository;

        @InjectMocks
        private ChatSessionServiceImpl chatSessionService;

        private ChatSession sampleSession;
        private ChatMessage sampleMessage;
        private Quiz sampleQuiz;
        private QuizState sampleQuizState;

        @BeforeEach
        void setUp() {
                sampleSession = ChatSession.builder()
                                .id("session1")
                                .title("Test Session")
                                .userId("user1")
                                .active(true)
                                .createdAt(Instant.now())
                                .build();

                sampleMessage = ChatMessage.builder()
                                .id("message1")
                                .chatSessionId("session1")
                                .text("Hello")
                                .role(ChatMessage.Role.USER)
                                .build();

                sampleQuiz = Quiz.builder()
                                .id("quiz1")
                                .chatSessionId("session1")
                                .build();

                sampleQuizState = QuizState.builder()
                                .id("quizState1")
                                .quizId("quiz1")
                                .currentQuestionIndex(0)
                                .build();
        }

        @Test
        void createSession_Success() {
                when(chatSessionRepository.save(any(ChatSession.class))).thenReturn(sampleSession);

                ChatSession session = chatSessionService.createSession("Test Session", "user1");

                assertNotNull(session);
                assertEquals("Test Session", session.getTitle());
                assertEquals("user1", session.getUserId());
                assertTrue(session.isActive());

                verify(chatSessionRepository).save(any(ChatSession.class));
        }

        @Test
        void createSession_Exception() {
                when(chatSessionRepository.save(any(ChatSession.class))).thenThrow(new RuntimeException("DB Error"));

                assertThrows(ChatSessionException.class,
                                () -> chatSessionService.createSession("Test Session", "user1"));
        }

        @Test
        void getSession_Success() {
                when(chatSessionRepository.findByIdAndActiveTrue("session1")).thenReturn(Optional.of(sampleSession));

                ChatSession session = chatSessionService.getSession("session1");

                assertNotNull(session);
                assertEquals("session1", session.getId());
        }

        @Test
        void getSession_NotFound() {
                when(chatSessionRepository.findByIdAndActiveTrue("unknown")).thenReturn(Optional.empty());

                assertThrows(ChatSessionNotFoundException.class, () -> chatSessionService.getSession("unknown"));
        }

        @Test
        void getSessionWithDetails_Success() {
                when(chatSessionRepository.findByIdAndActiveTrue("session1")).thenReturn(Optional.of(sampleSession));
                when(chatMessageRepository.findByChatSessionIdOrderByTimestampAsc("session1"))
                                .thenReturn(Collections.singletonList(sampleMessage));
                when(quizRepository.findFirstByChatSessionIdOrderByCreatedAtDesc("session1"))
                                .thenReturn(Optional.of(sampleQuiz));
                when(quizStateRepository.findByQuizId("quiz1"))
                                .thenReturn(Optional.of(sampleQuizState));

                ChatSession session = chatSessionService.getSessionWithDetails("session1");

                assertNotNull(session);
                assertEquals(1, session.getMessages().size());
                assertNotNull(session.getQuiz());
                assertNotNull(session.getQuizState());
        }

        @Test
        void getSessionWithDetails_NoQuiz() {
                when(chatSessionRepository.findByIdAndActiveTrue("session1")).thenReturn(Optional.of(sampleSession));
                when(chatMessageRepository.findByChatSessionIdOrderByTimestampAsc("session1"))
                                .thenReturn(Collections.singletonList(sampleMessage));
                when(quizRepository.findFirstByChatSessionIdOrderByCreatedAtDesc("session1"))
                                .thenReturn(Optional.empty());

                ChatSession session = chatSessionService.getSessionWithDetails("session1");

                assertNotNull(session);
                assertEquals(1, session.getMessages().size());
                assertNull(session.getQuiz());
                assertNull(session.getQuizState());
        }

        @Test
        void getActiveSessions_Success() {
                when(chatSessionRepository.findByUserIdAndActiveTrueOrderByCreatedAtDesc("user1"))
                                .thenReturn(Collections.singletonList(sampleSession));
                when(quizRepository.findFirstByChatSessionIdOrderByCreatedAtDesc("session1"))
                                .thenReturn(Optional.of(sampleQuiz));
                when(quizStateRepository.findByQuizId("quiz1"))
                                .thenReturn(Optional.of(sampleQuizState));

                List<ChatSession> sessions = chatSessionService.getActiveSessions("user1");

                assertFalse(sessions.isEmpty());
                assertEquals(1, sessions.size());
                assertNotNull(sessions.get(0).getQuiz());
                assertNotNull(sessions.get(0).getQuizState());
        }

        @Test
        void updateSessionTitle_Success() {
                when(chatSessionRepository.findByIdAndActiveTrue("session1")).thenReturn(Optional.of(sampleSession));
                when(chatSessionRepository.save(any(ChatSession.class))).thenReturn(sampleSession);

                ChatSession updatedSession = chatSessionService.updateSessionTitle("session1", "New Title");

                assertNotNull(updatedSession);
                assertEquals("New Title", sampleSession.getTitle());
        }

        @Test
        void deactivateSession_Success() {
                when(chatSessionRepository.findByIdAndActiveTrue("session1")).thenReturn(Optional.of(sampleSession));
                when(chatSessionRepository.save(any(ChatSession.class))).thenReturn(sampleSession);

                chatSessionService.deactivateSession("session1");

                assertFalse(sampleSession.isActive());
                verify(chatSessionRepository).save(sampleSession);
        }

        @Test
        void getSessionWithMessages_Success() {
                when(chatSessionRepository.findByIdAndActiveTrue("session1")).thenReturn(Optional.of(sampleSession));
                when(chatMessageRepository.findByChatSessionIdOrderByTimestampAsc("session1"))
                                .thenReturn(Collections.singletonList(sampleMessage));

                ChatSession session = chatSessionService.getSessionWithMessages("session1");

                assertNotNull(session);
        }

        @Test
        void getSessionWithDetails_Exception() {
                when(chatSessionRepository.findByIdAndActiveTrue("session1")).thenReturn(Optional.of(sampleSession));
                when(chatMessageRepository.findByChatSessionIdOrderByTimestampAsc("session1"))
                                .thenThrow(new RuntimeException("DB Error"));

                assertThrows(ChatSessionException.class, () -> chatSessionService.getSessionWithDetails("session1"));
        }

        @Test
        void updateSessionTitle_Exception() {
                when(chatSessionRepository.findByIdAndActiveTrue("session1")).thenReturn(Optional.of(sampleSession));
                when(chatSessionRepository.save(any(ChatSession.class))).thenThrow(new RuntimeException("DB Error"));

                assertThrows(ChatSessionException.class, () -> chatSessionService.updateSessionTitle("session1", "New Title"));
        }

        @Test
        void deactivateSession_Exception() {
                when(chatSessionRepository.findByIdAndActiveTrue("session1")).thenReturn(Optional.of(sampleSession));
                when(chatSessionRepository.save(any(ChatSession.class))).thenThrow(new RuntimeException("DB Error"));

                assertThrows(ChatSessionException.class, () -> chatSessionService.deactivateSession("session1"));
        }

        @Test
        void getSessionWithMessages_Exception() {
                when(chatSessionRepository.findByIdAndActiveTrue("session1")).thenReturn(Optional.of(sampleSession));
                when(chatMessageRepository.findByChatSessionIdOrderByTimestampAsc("session1"))
                                .thenThrow(new RuntimeException("DB Error"));

                assertThrows(ChatSessionException.class, () -> chatSessionService.getSessionWithMessages("session1"));
        }
}
