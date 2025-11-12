package com.fined.mentor.service;

import com.fined.mentor.entity.ChatSession;
import com.fined.mentor.exception.ChatSessionNotFoundException;

import java.util.List;

public interface ChatSessionService {
    ChatSession createSession(String title);
    ChatSession getSession(String sessionId);
    List<ChatSession> getActiveSessions();
    ChatSession updateSessionTitle(String sessionId, String newTitle);
    void deactivateSession(String sessionId);
    ChatSession getSessionWithMessages(String sessionId);

    void findById(String chatSessionId);
}