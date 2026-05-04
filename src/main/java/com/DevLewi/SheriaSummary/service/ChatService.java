package com.DevLewi.SheriaSummary.service;

import com.DevLewi.SheriaSummary.dto.ChatRequest;
import com.DevLewi.SheriaSummary.dto.ChatResponse;
import org.springframework.ai.document.Document;
import reactor.core.publisher.Flux;

import java.util.List;

public interface ChatService {
    ChatResponse chat(ChatRequest request);
    Flux<String> streamChat(ChatRequest request);
    List<Document> retrieveDocuments(ChatRequest request);
    String buildContext(List<Document> docs);
    List<String> buildSources(List<Document> docs);
}
