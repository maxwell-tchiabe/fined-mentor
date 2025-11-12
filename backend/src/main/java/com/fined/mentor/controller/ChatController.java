package com.fined.mentor.controller;

import com.fined.mentor.dto.ApiResponse;
import com.fined.mentor.dto.ChatMessageRequest;
import com.fined.mentor.dto.ChatMessageResponse;
import com.fined.mentor.entity.ChatMessage;
import com.fined.mentor.entity.ChatSession;
import com.fined.mentor.service.ChatService;
import com.fined.mentor.service.ChatSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final ChatSessionService chatSessionService;

    @PostMapping("/sessions")
    public ResponseEntity<ApiResponse<ChatSession>> createChatSession(@RequestParam String title) {
        try {
            log.info("Creating new chat session with title: {}", title);
            ChatSession session = chatService.createChatSession(title);
            return ResponseEntity.ok(ApiResponse.success(session));
        } catch (Exception e) {
            log.error("Error creating chat session", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse<List<ChatSession>>> getActiveSessions() {
        try {
            List<ChatSession> sessions = chatSessionService.getActiveSessions();
            return ResponseEntity.ok(ApiResponse.success(sessions));
        } catch (Exception e) {
            log.error("Error retrieving active chat sessions", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/message")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> sendMessage(
            @Valid @RequestBody ChatMessageRequest request) {
        try {
            log.debug("Processing chat message for session: {}", request.getChatSessionId());
            ChatMessage response = chatService.getChatResponse(
                    request.getChatSessionId(), request.getMessage());

            ChatMessageResponse responseDto = ChatMessageResponse.builder()
                    .id(response.getId())
                    .role(response.getRole())
                    .text(response.getText())
                    .timestamp(response.getTimestamp())
                    .chatSessionId(response.getChatSessionId())
                    .build();

            return ResponseEntity.ok(ApiResponse.success(responseDto));
        } catch (Exception e) {
            log.error("Error processing chat message", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/sessions/{sessionId}/history")
    public ResponseEntity<ApiResponse<List<ChatMessage>>> getChatHistory(
            @PathVariable String sessionId) {
        try {
            List<ChatMessage> history = chatService.getChatHistory(sessionId);
            return ResponseEntity.ok(ApiResponse.success(history));
        } catch (Exception e) {
            log.error("Error retrieving chat history for session: {}", sessionId, e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<ApiResponse<Void>> deactivateSession(@PathVariable String sessionId) {
        try {
            chatService.deactivateChatSession(sessionId);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (Exception e) {
            log.error("Error deactivating session: {}", sessionId, e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}