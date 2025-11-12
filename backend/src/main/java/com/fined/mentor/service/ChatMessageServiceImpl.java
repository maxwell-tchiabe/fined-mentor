package com.fined.mentor.service;

import com.fined.mentor.entity.ChatMessage;
import com.fined.mentor.exception.ChatMessageException;
import com.fined.mentor.exception.ChatMessageNotFoundException;
import com.fined.mentor.exception.ChatSessionNotFoundException;
import com.fined.mentor.repository.ChatMessageRepository;
import com.fined.mentor.repository.ChatSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatSessionRepository chatSessionRepository;

    @Override
    @Transactional
    public ChatMessage saveMessage(ChatMessage message) {
        try {
            log.debug("Saving chat message for session: {}", message.getChatSessionId());

            // Validate that the session exists and is active
            chatSessionRepository.findByIdAndActiveTrue(message.getChatSessionId())
                    .orElseThrow(() -> new ChatSessionNotFoundException(
                            "Chat session not found or inactive: " + message.getChatSessionId()));

            // Set timestamp if not already set
            if (message.getTimestamp() == null) {
                message.setTimestamp(LocalDateTime.now());
            }

            ChatMessage savedMessage = chatMessageRepository.save(message);
            log.debug("Successfully saved chat message with id: {}", savedMessage.getId());

            return savedMessage;

        } catch (Exception e) {
            log.error("Failed to save chat message for session: {}", message.getChatSessionId(), e);
            throw new ChatMessageException("Failed to save chat message.", e);
        }
    }

    @Override
    public ChatMessage getMessage(String messageId) {
        log.debug("Retrieving chat message: {}", messageId);
        return chatMessageRepository.findById(messageId)
                .orElseThrow(() -> {
                    log.warn("Chat message not found: {}", messageId);
                    return new ChatMessageNotFoundException("Chat message not found: " + messageId);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessage> getMessagesBySessionId(String sessionId) {
        try {
            log.debug("Retrieving messages for session: {}", sessionId);

            // Validate session exists and is active
            chatSessionRepository.findByIdAndActiveTrue(sessionId)
                    .orElseThrow(() -> new ChatSessionNotFoundException(
                            "Chat session not found or inactive: " + sessionId));

            List<ChatMessage> messages = chatMessageRepository.findByChatSessionIdOrderByTimestampAsc(sessionId);
            log.debug("Retrieved {} messages for session: {}", messages.size(), sessionId);

            return messages;

        } catch (Exception e) {
            log.error("Failed to retrieve messages for session: {}", sessionId, e);
            throw new ChatMessageException("Failed to retrieve chat messages.", e);
        }
    }

    @Override
    @Transactional
    public void deleteMessage(String messageId) {
        try {
            log.info("Deleting chat message: {}", messageId);

            ChatMessage message = getMessage(messageId);
            chatMessageRepository.delete(message);

            log.debug("Successfully deleted chat message: {}", messageId);

        } catch (Exception e) {
            log.error("Failed to delete chat message: {}", messageId, e);
            throw new ChatMessageException("Failed to delete chat message.", e);
        }
    }

    @Override
    @Transactional
    public void deleteAllMessagesBySessionId(String sessionId) {
        try {
            log.info("Deleting all messages for session: {}", sessionId);

            // Validate session exists
            chatSessionRepository.findById(sessionId)
                    .orElseThrow(() -> new ChatSessionNotFoundException(
                            "Chat session not found: " + sessionId));

            chatMessageRepository.deleteByChatSessionId(sessionId);
            log.debug("Successfully deleted all messages for session: {}", sessionId);

        } catch (Exception e) {
            log.error("Failed to delete messages for session: {}", sessionId, e);
            throw new ChatMessageException("Failed to delete chat messages.", e);
        }
    }

    @Override
    @Transactional
    public ChatMessage updateMessage(String messageId, String newText) {
        try {
            log.info("Updating chat message: {}", messageId);

            ChatMessage message = getMessage(messageId);

            // Only allow updating user messages (not AI responses)
            if (message.getRole() != ChatMessage.Role.USER) {
                throw new ChatMessageException("Only user messages can be updated.");
            }

            if (newText == null || newText.trim().isEmpty()) {
                throw new ChatMessageException("Message text cannot be empty.");
            }

            message.setText(newText.trim());

            ChatMessage updatedMessage = chatMessageRepository.save(message);
            log.debug("Successfully updated chat message: {}", messageId);

            return updatedMessage;

        } catch (Exception e) {
            log.error("Failed to update chat message: {}", messageId, e);
            throw new ChatMessageException("Failed to update chat message.", e);
        }
    }
}