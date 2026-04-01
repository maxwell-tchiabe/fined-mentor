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

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

    @Test
    void streamQuizGeneration_Error() throws Exception {
        QuizRequest request = new QuizRequest();
        request.setTopic("Investment");
        request.setChatSessionId("session1");

        when(quizService.streamQuizGeneration(anyString())).thenThrow(new RuntimeException("Startup error"));

        mockMvc.perform(post("/api/quiz/stream")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void generateQuiz_ValidationError() throws Exception {
        QuizRequest request = new QuizRequest();
        request.setTopic("Forbidden");
        request.setChatSessionId("session1");

        when(quizService.generateQuiz(anyString(), anyString()))
                .thenThrow(new com.fined.mentor.quiz.exception.QuizValidationException("Invalid topic"));

        mockMvc.perform(post("/api/quiz/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid topic"));
    }

    @Test
    void generateQuiz_InternalError() throws Exception {
        QuizRequest request = new QuizRequest();
        request.setTopic("Investment");
        request.setChatSessionId("session1");

        when(quizService.generateQuiz(anyString(), anyString()))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(post("/api/quiz/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Failed to generate quiz. Please try again later."));
    }

    @Test
    void saveStreamedQuiz_Error() throws Exception {
        com.fined.mentor.quiz.dto.SaveQuizRequest request = new com.fined.mentor.quiz.dto.SaveQuizRequest();
        request.setTopic("Investment");
        request.setChatSessionId("session1");
        request.setQuizJson("{}");

        when(quizService.saveStreamedQuiz(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Save failed"));

        mockMvc.perform(post("/api/quiz/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Failed to save quiz. Please try again later."));
    }

    @Test
    void getQuizState_Error() throws Exception {
        when(quizService.getQuizState("state1")).thenThrow(new RuntimeException("Not found"));

        mockMvc.perform(get("/api/quiz/state/state1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Not found"));
    }

    @Test
    void updateCurrentQuestionIndex_Success() throws Exception {
        java.util.Map<String, Integer> request = java.util.Map.of("index", 1);
        sampleQuizState.setCurrentQuestionIndex(1);
        when(quizService.updateCurrentQuestionIndex("state1", 1)).thenReturn(sampleQuizState);

        mockMvc.perform(put("/api/quiz/state/state1/index")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentQuestionIndex").value(1));
    }

    @Test
    void updateCurrentQuestionIndex_Error() throws Exception {
        java.util.Map<String, Integer> request = java.util.Map.of("index", 1);
        when(quizService.updateCurrentQuestionIndex(anyString(), anyInt())).thenThrow(new RuntimeException("Update failed"));

        mockMvc.perform(put("/api/quiz/state/state1/index")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Update failed"));
    }

    @Test
    void saveStreamedQuiz_Success() throws Exception {
        com.fined.mentor.quiz.dto.SaveQuizRequest request = new com.fined.mentor.quiz.dto.SaveQuizRequest();
        request.setTopic("Investment");
        request.setChatSessionId("session1");
        request.setQuizJson("{}");

        when(quizService.saveStreamedQuiz(anyString(), anyString(), anyString())).thenReturn(sampleQuiz);

        mockMvc.perform(post("/api/quiz/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getQuizState_Success() throws Exception {
        when(quizService.getQuizState("state1")).thenReturn(sampleQuizState);

        mockMvc.perform(get("/api/quiz/state/state1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("state1"));
    }

    @Test
    void startQuiz_Error() throws Exception {
        when(quizService.startQuiz(anyString(), anyString())).thenThrow(new RuntimeException("Start failed"));

        mockMvc.perform(post("/api/quiz/quiz1/start")
                        .param("chatSessionId", "session1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void submitAnswer_Error() throws Exception {
        QuizAnswerRequest request = new QuizAnswerRequest();
        request.setQuizStateId("state1");
        request.setQuestionIndex(0);
        request.setAnswer("Answer");

        when(quizService.submitAnswer(anyString(), anyInt(), anyString())).thenThrow(new RuntimeException("Submit failed"));

        mockMvc.perform(post("/api/quiz/answer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getQuizBySession_Error() throws Exception {
        when(quizService.getQuizBySessionId("session1")).thenThrow(new RuntimeException("Not found"));

        mockMvc.perform(get("/api/quiz/sessions/session1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getQuizStateBySession_Error() throws Exception {
        when(quizService.getQuizStateBySessionId("session1")).thenThrow(new RuntimeException("Not found"));

        mockMvc.perform(get("/api/quiz/sessions/session1/state"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void finishQuiz_Error() throws Exception {
        when(quizService.finishQuiz("state1")).thenThrow(new RuntimeException("Finish failed"));

        mockMvc.perform(post("/api/quiz/state1/finish"))
                .andExpect(status().isBadRequest());
    }
}
