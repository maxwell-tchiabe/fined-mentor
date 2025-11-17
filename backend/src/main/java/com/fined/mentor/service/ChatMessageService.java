package com.fined.mentor.service;

import com.fined.mentor.entity.ChatMessage;

import java.util.List;

public interface ChatMessageService {
    ChatMessage saveMessage(ChatMessage message);
    ChatMessage getMessage(String messageId);
    List<ChatMessage> getMessagesBySessionId(String sessionId);
    void deleteMessage(String messageId);
    void deleteAllMessagesBySessionId(String sessionId);
    ChatMessage updateMessage(String messageId, String newText);
}