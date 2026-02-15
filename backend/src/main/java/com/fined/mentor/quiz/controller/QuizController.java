package com.fined.mentor.quiz.controller;

import com.fined.mentor.core.dto.ApiResponse;
import com.fined.mentor.quiz.dto.QuizAnswerRequest;
import com.fined.mentor.quiz.dto.QuizRequest;
import com.fined.mentor.quiz.dto.QuizResponse;
import com.fined.mentor.quiz.dto.QuizStateResponse;
import com.fined.mentor.quiz.dto.SaveQuizRequest;
import com.fined.mentor.quiz.entity.Quiz;
import com.fined.mentor.quiz.entity.QuizState;
import com.fined.mentor.quiz.exception.QuizValidationException;
import com.fined.mentor.quiz.service.QuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

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
        } catch (QuizValidationException e) {
            log.warn("Quiz generation failed - invalid topic: {}", request.getTopic());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error generating quiz for topic: {}", request.getTopic(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to generate quiz. Please try again later."));
        }
    }

    @PostMapping(value = "/stream", produces = org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamQuizGeneration(@Valid @RequestBody QuizRequest request) {
        try {
            log.info("Streaming quiz for topic: {}", request.getTopic());
            return quizService.streamQuizGeneration(request.getTopic());
        } catch (Exception e) {
            log.error("Error starting quiz stream", e);
            return Flux.error(e);
        }
    }

    @PostMapping("/save")
    public ResponseEntity<ApiResponse<QuizResponse>> saveStreamedQuiz(
            @Valid @RequestBody SaveQuizRequest request) {
        try {
            log.info("Saving streamed quiz for topic: {}", request.getTopic());
            Quiz quiz = quizService.saveStreamedQuiz(request.getTopic(), request.getChatSessionId(),
                    request.getQuizJson());

            QuizResponse response = QuizResponse.builder()
                    .id(quiz.getId())
                    .topic(quiz.getTopic())
                    .questions(quiz.getQuestions())
                    .createdAt(quiz.getCreatedAt())
                    .build();

            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("Error saving streamed quiz for topic: {}", request.getTopic(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to save quiz. Please try again later."));
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
            QuizState quizState = quizService.finishQuiz(quizStateId);

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
            log.error("Error finishing quiz state: {}", quizStateId, e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/state/{quizStateId}/index")
    public ResponseEntity<ApiResponse<QuizStateResponse>> updateCurrentQuestionIndex(
            @PathVariable String quizStateId,
            @RequestBody java.util.Map<String, Integer> request) {
        try {
            int index = request.get("index");
            log.debug("Updating question index to {} for quiz state: {}", index, quizStateId);

            QuizState quizState = quizService.updateCurrentQuestionIndex(quizStateId, index);

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
            log.error("Error updating question index for quiz state: {}", quizStateId, e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
