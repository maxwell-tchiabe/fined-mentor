package com.fined.mentor.chat.service;

import com.fined.mentor.chat.entity.ChatSession;
import com.fined.mentor.chat.entity.ChatMessage;
import com.fined.mentor.quiz.entity.Quiz;
import com.fined.mentor.quiz.entity.QuizState;
import com.fined.mentor.chat.exception.ChatSessionException;
import com.fined.mentor.chat.exception.ChatSessionNotFoundException;
import com.fined.mentor.chat.repository.ChatMessageRepository;
import com.fined.mentor.chat.repository.ChatSessionRepository;
import com.fined.mentor.quiz.repository.QuizRepository;
import com.fined.mentor.quiz.repository.QuizStateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatSessionServiceImpl implements ChatSessionService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final QuizRepository quizRepository;
    private final QuizStateRepository quizStateRepository;

    @Override
    @Transactional
    public ChatSession createSession(String title, String userId) {
        try {
            log.info("Creating new chat session with title: {} for user: {}", title, userId);

            ChatSession session = ChatSession.builder()
                    .title(title != null ? title : "New Chat Session")
                    .createdAt(LocalDateTime.now())
                    .active(true)
                    .userId(userId)
                    .build();

            ChatSession savedSession = chatSessionRepository.save(session);
            log.debug("Successfully created chat session with id: {}", savedSession.getId());

            return savedSession;

        } catch (Exception e) {
            log.error("Failed to create chat session with title: {}", title, e);
            throw new ChatSessionException("Failed to create chat session. Please try again.", e);
        }
    }

    @Override
    public ChatSession getSession(String sessionId) {
        log.debug("Retrieving chat session: {}", sessionId);
        return chatSessionRepository.findByIdAndActiveTrue(sessionId)
                .orElseThrow(() -> {
                    log.warn("Chat session not found or inactive: {}", sessionId);
                    return new ChatSessionNotFoundException("Chat session not found: " + sessionId);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public ChatSession getSessionWithDetails(String sessionId) {
        log.debug("Retrieving full chat session details for id: {}", sessionId);
        ChatSession session = getSession(sessionId);

        // Fetch and set messages
        List<ChatMessage> messages = chatMessageRepository.findByChatSessionIdOrderByTimestampAsc(sessionId);
        session.setMessages(messages);

        // Fetch and set quiz and quiz state
        Optional<Quiz> quizOpt = quizRepository.findByChatSessionId(sessionId);
        if (quizOpt.isPresent()) {
            Quiz quiz = quizOpt.get();
            session.setQuiz(quiz);

            Optional<QuizState> quizStateOpt = quizStateRepository.findByQuizId(quiz.getId());
            quizStateOpt.ifPresent(session::setQuizState);
        }

        return session;
    }

    @Override
    public List<ChatSession> getActiveSessions(String userId) {
        log.debug("Retrieving all active chat sessions for user: {}", userId);
        return chatSessionRepository.findByUserIdAndActiveTrueOrderByCreatedAtDesc(userId);
    }

    @Override
    @Transactional
    public ChatSession updateSessionTitle(String sessionId, String newTitle) {
        try {
            log.info("Updating title for session: {} to: {}", sessionId, newTitle);

            ChatSession session = getSession(sessionId);
            session.setTitle(newTitle);

            ChatSession updatedSession = chatSessionRepository.save(session);
            log.debug("Successfully updated session title for: {}", sessionId);

            return updatedSession;

        } catch (Exception e) {
            log.error("Failed to update title for session: {}", sessionId, e);
            throw new ChatSessionException("Failed to update chat session title.", e);
        }
    }

    @Override
    @Transactional
    public void deactivateSession(String sessionId) {
        try {
            log.info("Deactivating chat session: {}", sessionId);

            ChatSession session = getSession(sessionId);
            session.setActive(false);

            chatSessionRepository.save(session);
            log.debug("Successfully deactivated chat session: {}", sessionId);

        } catch (Exception e) {
            log.error("Failed to deactivate chat session: {}", sessionId, e);
            throw new ChatSessionException("Failed to deactivate chat session.", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ChatSession getSessionWithMessages(String sessionId) {
        try {
            log.debug("Retrieving chat session with messages: {}", sessionId);

            ChatSession session = getSession(sessionId);

            // Load messages for this session
            List<ChatMessage> messages = chatMessageRepository.findByChatSessionIdOrderByTimestampAsc(sessionId);

            return session;

        } catch (Exception e) {
            log.error("Failed to retrieve chat session with messages: {}", sessionId, e);
            throw new ChatSessionException("Failed to retrieve chat session details.", e);
        }
    }

    @Override
    public void findById(String chatSessionId) {

    }
}