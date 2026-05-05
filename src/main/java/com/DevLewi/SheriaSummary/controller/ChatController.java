package com.DevLewi.SheriaSummary.controller;

import com.DevLewi.SheriaSummary.dto.ChatRequest;
import com.DevLewi.SheriaSummary.dto.ChatResponse;
import com.DevLewi.SheriaSummary.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        return ResponseEntity.ok(chatService.chat(request));
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamChat(@RequestBody ChatRequest request) {
        Flux<ServerSentEvent<String>> tokens = chatService.streamChat(request)
                .map(token -> ServerSentEvent.<String>builder()
                        .event("token")
                        .data(token)
                        .build());

        ServerSentEvent<String> done = ServerSentEvent.<String>builder()
                .event("done")
                .data("[DONE]")
                .build();

        return tokens.concatWith(Flux.just(done));
    }
}
