package com.fined.mentor.service;

import com.fined.mentor.entity.ChatMessage;
import com.fined.mentor.entity.ChatSession;

import java.util.List;

public interface ChatService {
    ChatMessage getChatResponse(String chatSessionId, String userMessage);
    List<ChatMessage> getChatHistory(String chatSessionId);
    ChatSession createChatSession(String title);
    void deactivateChatSession(String chatSessionId);
}
