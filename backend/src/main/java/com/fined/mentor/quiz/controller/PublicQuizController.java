package com.fined.mentor.quiz.controller;

import com.fined.mentor.quiz.dto.GuestQuizRequest;
import com.fined.mentor.quiz.service.QuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import static org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE;

@Slf4j
@RestController
@RequestMapping("/api/public/quiz")
@RequiredArgsConstructor
public class PublicQuizController {

    private final QuizService quizService;

    @PostMapping(value = "/stream", produces = TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamQuizGeneration(@Valid @RequestBody GuestQuizRequest request) {
        try {
            log.info("Streaming guest quiz for topic: {}", request.getTopic());
            return quizService.streamQuizGeneration(request.getTopic());
        } catch (Exception e) {
            log.error("Error starting guest quiz stream", e);
            return Flux.error(e);
        }
    }
}
