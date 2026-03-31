package com.fined.mentor.chat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fined.mentor.chat.dto.GuestChatMessageRequest;
import com.fined.mentor.chat.service.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import reactor.core.publisher.Flux;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PublicChatControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ChatService chatService;

    @InjectMocks
    private PublicChatController publicChatController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(publicChatController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void streamMessage_Success() throws Exception {
        GuestChatMessageRequest request = new GuestChatMessageRequest();
        request.setMessage("Hello");
        request.setHistory(new ArrayList<>());

        when(chatService.streamGuestChatResponse(any(), anyString()))
                .thenReturn(Flux.just("Hi", " there!"));

        mockMvc.perform(post("/api/public/chat/stream")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
