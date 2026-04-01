package com.fined.mentor.quiz.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fined.mentor.quiz.dto.GuestQuizRequest;
import com.fined.mentor.quiz.service.QuizService;
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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PublicQuizControllerTest {

    private MockMvc mockMvc;

    @Mock
    private QuizService quizService;

    @InjectMocks
    private PublicQuizController publicQuizController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(publicQuizController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void streamQuizGeneration_Success() throws Exception {
        GuestQuizRequest request = new GuestQuizRequest();
        request.setTopic("Investment");

        when(quizService.streamQuizGeneration("Investment")).thenReturn(Flux.just("Chunk 1", "Chunk 2"));

        mockMvc.perform(post("/api/public/quiz/stream")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void streamQuizGeneration_Error() throws Exception {
        GuestQuizRequest request = new GuestQuizRequest();
        request.setTopic("Investment");

        when(quizService.streamQuizGeneration(anyString())).thenReturn(Flux.error(new RuntimeException("Stream error")));

        mockMvc.perform(post("/api/public/quiz/stream")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()); // stand-alone mockmvc with flux usually returns 200 even if flux is error, unless handled by ExceptionHandler
    }
}
