package com.fined.mentor.controller;

import com.fined.mentor.dto.ApiResponse;
import com.fined.mentor.dto.QuizAnswerRequest;
import com.fined.mentor.dto.QuizRequest;
import com.fined.mentor.dto.QuizResponse;
import com.fined.mentor.dto.QuizStateResponse;
import com.fined.mentor.entity.Quiz;
import com.fined.mentor.entity.QuizState;
import com.fined.mentor.service.QuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<QuizResponse>> generateQuiz(
            @Valid @RequestBody QuizRequest request) {
        try {
            log.info("Generating quiz for topic: {}", request.getTopic());
            Quiz quiz = quizService.generateQuiz(request.getTopic(), request.getChatSessionId());

            QuizResponse response = QuizResponse.builder()
                    .id(quiz.getId())
                    .topic(quiz.getTopic())
                    .questions(quiz.getQuestions())
                    .createdAt(quiz.getCreatedAt())
                    .build();

            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("Error generating quiz for topic: {}", request.getTopic(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{quizId}/start")
    public ResponseEntity<ApiResponse<QuizStateResponse>> startQuiz(
            @PathVariable String quizId,
            @RequestParam String chatSessionId) {
        try {
            log.info("Starting quiz: {} for session: {}", quizId, chatSessionId);
            QuizState quizState = quizService.startQuiz(quizId, chatSessionId);

            QuizStateResponse response = QuizStateResponse.builder()
                    .id(quizState.getId())
                    .quizId(quizState.getQuizId())
                    .chatSessionId(quizState.getChatSessionId())
                    .currentQuestionIndex(quizState.getCurrentQuestionIndex())
                    .userAnswers(quizState.getUserAnswers())
                    .isSubmitted(quizState.getIsSubmitted())
                    .score(quizState.getScore())
                    .isFinished(quizState.isFinished())
                    .build();

            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("Error starting quiz: {} for session: {}", quizId, chatSessionId, e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/answer")
    public ResponseEntity<ApiResponse<QuizStateResponse>> submitAnswer(
            @Valid @RequestBody QuizAnswerRequest request) {
        try {
            log.debug("Submitting answer for quiz state: {}, question: {}",
                    request.getQuizStateId(), request.getQuestionIndex());

            QuizState quizState = quizService.submitAnswer(
                    request.getQuizStateId(),
                    request.getQuestionIndex(),
                    request.getAnswer());

            QuizStateResponse response = QuizStateResponse.builder()
                    .id(quizState.getId())
                    .quizId(quizState.getQuizId())
                    .chatSessionId(quizState.getChatSessionId())
                    .currentQuestionIndex(quizState.getCurrentQuestionIndex())
                    .userAnswers(quizState.getUserAnswers())
                    .isSubmitted(quizState.getIsSubmitted())
                    .score(quizState.getScore())
                    .isFinished(quizState.isFinished())
                    .build();

            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("Error submitting answer for quiz state: {}", request.getQuizStateId(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<ApiResponse<QuizResponse>> getQuizBySession(
            @PathVariable String sessionId) {
        try {
            Quiz quiz = quizService.getQuizBySessionId(sessionId);
            QuizResponse response = QuizResponse.builder()
                    .id(quiz.getId())
                    .topic(quiz.getTopic())
                    .questions(quiz.getQuestions())
                    .createdAt(quiz.getCreatedAt())
                    .build();

            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("Error retrieving quiz for session: {}", sessionId, e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/state/{quizStateId}")
    public ResponseEntity<ApiResponse<QuizStateResponse>> getQuizState(
            @PathVariable String quizStateId) {
        try {
            QuizState quizState = quizService.getQuizState(quizStateId);

            QuizStateResponse response = QuizStateResponse.builder()
                    .id(quizState.getId())
                    .quizId(quizState.getQuizId())
                    .chatSessionId(quizState.getChatSessionId())
                    .currentQuestionIndex(quizState.getCurrentQuestionIndex())
                    .userAnswers(quizState.getUserAnswers())
                    .isSubmitted(quizState.getIsSubmitted())
                    .score(quizState.getScore())
                    .isFinished(quizState.isFinished())
                    .build();

            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("Error retrieving quiz state: {}", quizStateId, e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/sessions/{sessionId}/state")
    public ResponseEntity<ApiResponse<QuizStateResponse>> getQuizStateBySession(
            @PathVariable String sessionId) {
        try {
            QuizState quizState = quizService.getQuizStateBySessionId(sessionId);

            QuizStateResponse response = QuizStateResponse.builder()
                    .id(quizState.getId())
                    .quizId(quizState.getQuizId())
                    .chatSessionId(quizState.getChatSessionId())
                    .currentQuestionIndex(quizState.getCurrentQuestionIndex())
                    .userAnswers(quizState.getUserAnswers())
                    .isSubmitted(quizState.getIsSubmitted())
                    .score(quizState.getScore())
                    .isFinished(quizState.isFinished())
                    .build();

            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("Error retrieving quiz state for session: {}", sessionId, e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{quizStateId}/finish")
    public ResponseEntity<ApiResponse<QuizStateResponse>> finishQuiz(
            @PathVariable String quizStateId) {
        try {
            log.info("Finishing quiz state: {}", quizStateId);
            QuizState quizState = quizService.getQuizState(quizStateId);
            quizState.setFinished(true);
            // Note: You might want to add a finishQuiz method in QuizService for additional logic

            QuizStateResponse response = QuizStateResponse.builder()
                    .id(quizState.getId())
                    .quizId(quizState.getQuizId())
                    .chatSessionId(quizState.getChatSessionId())
                    .currentQuestionIndex(quizState.getCurrentQuestionIndex())
                    .userAnswers(quizState.getUserAnswers())
                    .isSubmitted(quizState.getIsSubmitted())
                    .score(quizState.getScore())
                    .isFinished(true)
                    .build();

            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("Error finishing quiz state: {}", quizStateId, e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
