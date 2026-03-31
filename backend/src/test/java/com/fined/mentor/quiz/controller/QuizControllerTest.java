package com.fined.mentor.quiz.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fined.mentor.quiz.dto.QuizAnswerRequest;
import com.fined.mentor.quiz.dto.QuizRequest;
import com.fined.mentor.quiz.entity.Quiz;
import com.fined.mentor.quiz.entity.QuizState;
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

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class QuizControllerTest {

    private MockMvc mockMvc;

    @Mock
    private QuizService quizService;

    @InjectMocks
    private QuizController quizController;

    private ObjectMapper objectMapper;
    private Quiz sampleQuiz;
    private QuizState sampleQuizState;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(quizController).build();
        objectMapper = new ObjectMapper();

        sampleQuiz = Quiz.builder()
                .id("quiz1")
                .topic("Investment")
                .questions(Collections.emptyList())
                .build();

        sampleQuizState = new QuizState();
        sampleQuizState.setId("state1");
        sampleQuizState.setQuizId("quiz1");
        sampleQuizState.setChatSessionId("session1");
        sampleQuizState.setScore(1);
    }

    @Test
    void generateQuiz_Success() throws Exception {
        QuizRequest request = new QuizRequest();
        request.setTopic("Investment");
        request.setChatSessionId("session1");

        when(quizService.generateQuiz("Investment", "session1")).thenReturn(sampleQuiz);

        mockMvc.perform(post("/api/quiz/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("quiz1"));
    }

    @Test
    void startQuiz_Success() throws Exception {
        when(quizService.startQuiz("quiz1", "session1")).thenReturn(sampleQuizState);

        mockMvc.perform(post("/api/quiz/quiz1/start")
                        .param("chatSessionId", "session1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("state1"));
    }

    @Test
    void submitAnswer_Success() throws Exception {
        QuizAnswerRequest request = new QuizAnswerRequest();
        request.setQuizStateId("state1");
        request.setQuestionIndex(0);
        request.setAnswer("Answer");

        when(quizService.submitAnswer(eq("state1"), eq(0), eq("Answer"))).thenReturn(sampleQuizState);

        mockMvc.perform(post("/api/quiz/answer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.score").value(1));
    }

    @Test
    void getQuizBySession_Success() throws Exception {
        when(quizService.getQuizBySessionId("session1")).thenReturn(sampleQuiz);

        mockMvc.perform(get("/api/quiz/sessions/session1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("quiz1"));
    }

    @Test
    void getQuizStateBySession_Success() throws Exception {
        when(quizService.getQuizStateBySessionId("session1")).thenReturn(sampleQuizState);

        mockMvc.perform(get("/api/quiz/sessions/session1/state"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("state1"));
    }

    @Test
    void finishQuiz_Success() throws Exception {
        sampleQuizState.setFinished(true);
        when(quizService.finishQuiz("state1")).thenReturn(sampleQuizState);

        mockMvc.perform(post("/api/quiz/state1/finish"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.finished").value(true));
    }
}
