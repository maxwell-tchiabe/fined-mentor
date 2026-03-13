package com.fined.mentor.chat.controller;

import com.fined.mentor.chat.dto.GuestChatMessageRequest;
import com.fined.mentor.chat.service.ChatService;
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
@RequestMapping("/api/public/chat")
@RequiredArgsConstructor
public class PublicChatController {

    private final ChatService chatService;

    @PostMapping(value = "/stream", produces = TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamMessage(@Valid @RequestBody GuestChatMessageRequest request) {
        try {
            log.debug("Streaming guest chat message");
            return chatService.streamGuestChatResponse(request.getHistory(), request.getMessage());
        } catch (Exception e) {
            log.error("Error starting guest chat stream", e);
            return Flux.error(e);
        }
    }
}
