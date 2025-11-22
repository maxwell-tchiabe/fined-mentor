package com.fined.mentor.chat.service;

import com.fined.mentor.chat.entity.ChatSession;

import java.util.List;

public interface ChatSessionService {
    ChatSession createSession(String title);

    ChatSession getSession(String sessionId);

    List<ChatSession> getActiveSessions();

    ChatSession updateSessionTitle(String sessionId, String newTitle);

    void deactivateSession(String sessionId);

    ChatSession getSessionWithMessages(String sessionId);

    ChatSession getSessionWithDetails(String sessionId);

    void findById(String chatSessionId);
}