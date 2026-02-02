package com.fined.mentor.chat.controller;

import com.fined.mentor.core.dto.ApiResponse;
import com.fined.mentor.chat.dto.ChatMessageRequest;
import com.fined.mentor.chat.dto.ChatMessageResponse;
import com.fined.mentor.chat.dto.UpdateSessionTitleRequest;
import com.fined.mentor.chat.entity.ChatMessage;
import com.fined.mentor.chat.entity.ChatSession;
import com.fined.mentor.chat.service.ChatService;
import com.fined.mentor.chat.service.ChatSessionService;
import com.fined.mentor.auth.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

            // Get current user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            ChatSession session = chatSessionService.createSession(title, user.getId());
            return ResponseEntity.ok(ApiResponse.success(session));
        } catch (Exception e) {
            log.error("Error creating chat session", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse<List<ChatSession>>> getActiveSessions() {
        try {
            // Get current user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            List<ChatSession> sessions = chatSessionService.getActiveSessions(user.getId());
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

    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<ApiResponse<ChatSession>> getChatSession(@PathVariable String sessionId) {
        try {
            log.info("Retrieving chat session with details for id: {}", sessionId);
            ChatSession session = chatSessionService.getSessionWithDetails(sessionId);
            return ResponseEntity.ok(ApiResponse.success(session));
        } catch (Exception e) {
            log.error("Error retrieving chat session: {}", sessionId, e);
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

    @PutMapping("/sessions/{sessionId}/title")
    public ResponseEntity<ApiResponse<ChatSession>> updateSessionTitle(
            @PathVariable String sessionId,
            @Valid @RequestBody UpdateSessionTitleRequest request) {
        try {
            log.info("Updating title for session: {} to: {}", sessionId, request.getTitle());
            ChatSession updatedSession = chatSessionService.updateSessionTitle(sessionId, request.getTitle());
            return ResponseEntity.ok(ApiResponse.success(updatedSession));
        } catch (Exception e) {
            log.error("Error updating session title for session: {}", sessionId, e);
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