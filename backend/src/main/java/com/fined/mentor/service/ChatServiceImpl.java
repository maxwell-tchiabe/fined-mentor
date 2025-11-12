package com.fined.mentor.service;


import com.fined.mentor.entity.ChatMessage;
import com.fined.mentor.entity.ChatSession;
import com.fined.mentor.exception.ChatException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ChatServiceImpl implements ChatService {

    private final ChatClient chatClient;
    private final ChatSessionService chatSessionService;
    private final ChatMessageService chatMessageService;


    public ChatServiceImpl(ChatClient.Builder chatClientBuilder,
                           ChatSessionService chatSessionService,
                           ChatMessageService chatMessageService) {
        this.chatClient = chatClientBuilder.build();
        this.chatSessionService = chatSessionService;
        this.chatMessageService = chatMessageService;
    }

    private static final String SYSTEM_PROMPT = """
            You are FinEd Mentor, a friendly and knowledgeable AI agent specializing in
            Finance, Real Estate, and Investment. Provide clear, concise, and helpful answers.
            Format your responses with markdown for readability
            (e.g., use **bold** for emphasis and bullet points for lists).
            """;

    @Override
    public ChatMessage getChatResponse(String chatSessionId, String userMessage) {
        try {
            log.debug("Getting chat response for session: {}", chatSessionId);

            chatSessionService.findById(chatSessionId);

            chatMessageService.saveMessage(
                    ChatMessage.builder()
                            .chatSessionId(chatSessionId)
                            .role(ChatMessage.Role.USER)
                            .text(userMessage)
                            .build());

            List<Message> history = chatMessageService.getMessagesBySessionId(chatSessionId)
                    .stream()
                    .map(msg -> (Message) new UserMessage(msg.getText()))
                    .collect(Collectors.toList());

            SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(SYSTEM_PROMPT);
            Message systemMessage = systemPromptTemplate.createMessage();
            history.add(0, systemMessage);

            Prompt prompt = new Prompt(history);

            String responseContent = chatClient.prompt(prompt).call().content();

            ChatMessage aiResponse = chatMessageService.saveMessage(
                    ChatMessage.builder()
                            .chatSessionId(chatSessionId)
                            .role(ChatMessage.Role.MODEL)
                            .text(responseContent)
                            .build());

            log.debug("Successfully generated and saved chat response");
            return aiResponse;

        } catch (Exception e) {
            log.error("Failed to get chat response for session: {}", chatSessionId, e);
            throw new ChatException("Failed to get chat response. Please try again.", e);
        }
    }

    @Override
    public List<ChatMessage> getChatHistory(String chatSessionId) {
        return chatMessageService.getMessagesBySessionId(chatSessionId);
    }

    @Override
    public ChatSession createChatSession(String title) {
        return chatSessionService.createSession(title);
    }

    @Override
    public void deactivateChatSession(String chatSessionId) {
        chatSessionService.deactivateSession(chatSessionId);
    }

}
