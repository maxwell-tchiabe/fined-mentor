package com.fined.mentor.chat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fined.mentor.auth.entity.Role;
import com.fined.mentor.auth.entity.User;
import com.fined.mentor.chat.dto.ChatMessageRequest;
import com.fined.mentor.chat.dto.UpdateSessionTitleRequest;
import com.fined.mentor.chat.entity.ChatMessage;
import com.fined.mentor.chat.entity.ChatSession;
import com.fined.mentor.chat.service.ChatService;
import com.fined.mentor.chat.service.ChatSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.Collections;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

        private MockMvc mockMvc;

        @Mock
        private ChatService chatService;

        @Mock
        private ChatSessionService chatSessionService;

        @InjectMocks
        private ChatController chatController;

        private ObjectMapper objectMapper;
        private User sampleUser;
        private ChatSession sampleSession;
        private ChatMessage sampleMessage;

        @BeforeEach
        void setUp() {
                mockMvc = MockMvcBuilders.standaloneSetup(chatController).build();
                objectMapper = new ObjectMapper();

                Role role = new Role();
                role.setName(Role.RoleName.ROLE_USER);

                sampleUser = User.builder()
                                .id("user1")
                                .username("testuser")
                                .roles(Set.of(role))
                                .build();

                SecurityContextHolder.getContext().setAuthentication(
                                new UsernamePasswordAuthenticationToken(sampleUser, null, sampleUser.getAuthorities()));

                sampleSession = ChatSession.builder()
                                .id("session1")
                                .title("New Chat")
                                .userId("user1")
                                .createdAt(Instant.now())
                                .active(true)
                                .build();

                sampleMessage = ChatMessage.builder()
                                .id("message1")
                                .chatSessionId("session1")
                                .text("Hello AI")
                                .role(ChatMessage.Role.MODEL)
                                .timestamp(Instant.now())
                                .build();
        }

        @Test
        void createChatSession_Success() throws Exception {
                when(chatSessionService.createSession("New Chat", "user1")).thenReturn(sampleSession);

                mockMvc.perform(post("/api/chat/sessions")
                                .param("title", "New Chat"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.id").value("session1"));
        }

        @Test
        void getActiveSessions_Success() throws Exception {
                when(chatSessionService.getActiveSessions("user1"))
                                .thenReturn(Collections.singletonList(sampleSession));

                mockMvc.perform(get("/api/chat/sessions"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data[0].id").value("session1"));
        }

        @Test
        void sendMessage_Success() throws Exception {
                ChatMessageRequest request = new ChatMessageRequest();
                request.setChatSessionId("session1");
                request.setMessage("Hello");

                when(chatService.getChatResponse("session1", "Hello")).thenReturn(sampleMessage);

                mockMvc.perform(post("/api/chat/message")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.id").value("message1"))
                                .andExpect(jsonPath("$.data.text").value("Hello AI"));
        }

        @Test
        void getChatSession_Success() throws Exception {
                when(chatSessionService.getSessionWithDetails("session1")).thenReturn(sampleSession);

                mockMvc.perform(get("/api/chat/sessions/session1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.id").value("session1"));
        }

        @Test
        void getChatHistory_Success() throws Exception {
                when(chatService.getChatHistory("session1")).thenReturn(Collections.singletonList(sampleMessage));

                mockMvc.perform(get("/api/chat/sessions/session1/history"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data[0].id").value("message1"));
        }

        @Test
        void updateSessionTitle_Success() throws Exception {
                UpdateSessionTitleRequest request = new UpdateSessionTitleRequest();
                request.setTitle("Updated Title");

                sampleSession.setTitle("Updated Title");
                when(chatSessionService.updateSessionTitle("session1", "Updated Title")).thenReturn(sampleSession);

                mockMvc.perform(put("/api/chat/sessions/session1/title")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.title").value("Updated Title"));
        }

        @Test
        void deactivateSession_Success() throws Exception {
                doNothing().when(chatService).deactivateChatSession("session1");

                mockMvc.perform(delete("/api/chat/sessions/session1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        void createChatSession_Error() throws Exception {
                when(chatSessionService.createSession(anyString(), anyString()))
                                .thenThrow(new RuntimeException("Service error"));

                mockMvc.perform(post("/api/chat/sessions")
                                .param("title", "New Chat"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.error").value("Service error"));
        }

        @Test
        void getActiveSessions_Error() throws Exception {
                when(chatSessionService.getActiveSessions(anyString()))
                                .thenThrow(new RuntimeException("Retrieve error"));

                mockMvc.perform(get("/api/chat/sessions"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.error").value("Retrieve error"));
        }

        @Test
        void sendMessage_Error() throws Exception {
                ChatMessageRequest request = new ChatMessageRequest();
                request.setChatSessionId("session1");
                request.setMessage("Hello");

                when(chatService.getChatResponse(anyString(), anyString()))
                                .thenThrow(new RuntimeException("Chat error"));

                mockMvc.perform(post("/api/chat/message")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.error").value("Chat error"));
        }

        @Test
        void getChatSession_Error() throws Exception {
                when(chatSessionService.getSessionWithDetails(anyString()))
                                .thenThrow(new RuntimeException("Session error"));

                mockMvc.perform(get("/api/chat/sessions/session1"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.error").value("Session error"));
        }

        @Test
        void getChatHistory_Error() throws Exception {
                when(chatService.getChatHistory(anyString())).thenThrow(new RuntimeException("History error"));

                mockMvc.perform(get("/api/chat/sessions/session1/history"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.error").value("History error"));
        }

        @Test
        void updateSessionTitle_Error() throws Exception {
                UpdateSessionTitleRequest request = new UpdateSessionTitleRequest();
                request.setTitle("Fail");

                when(chatSessionService.updateSessionTitle(anyString(), anyString()))
                                .thenThrow(new RuntimeException("Update error"));

                mockMvc.perform(put("/api/chat/sessions/session1/title")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.error").value("Update error"));
        }

        @Test
        void deactivateSession_Error() throws Exception {
                doThrow(new RuntimeException("Delete error")).when(chatService).deactivateChatSession(anyString());

                mockMvc.perform(delete("/api/chat/sessions/session1"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.error").value("Delete error"));
        }

        @Test
        void streamMessage_Error() throws Exception {
                com.fined.mentor.chat.dto.ChatMessageRequest request = new com.fined.mentor.chat.dto.ChatMessageRequest();
                request.setChatSessionId("session1");
                request.setMessage("Hello");

                when(chatService.streamChatResponse(anyString(), anyString())).thenThrow(new RuntimeException("Stream error"));

                mockMvc.perform(post("/api/chat/stream")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk());
        }
}
