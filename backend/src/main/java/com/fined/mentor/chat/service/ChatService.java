package com.fined.mentor.chat.service;

import com.fined.mentor.chat.entity.ChatMessage;
import com.fined.mentor.chat.entity.ChatSession;

import java.util.List;

import reactor.core.publisher.Flux;

public interface ChatService {
    ChatMessage getChatResponse(String chatSessionId, String userMessage);

    Flux<String> streamChatResponse(String chatSessionId, String userMessage);

    List<ChatMessage> getChatHistory(String chatSessionId);

    ChatSession createChatSession(String title, String userId);

    void deactivateChatSession(String chatSessionId);
}
