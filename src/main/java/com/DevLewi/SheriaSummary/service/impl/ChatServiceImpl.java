package com.DevLewi.SheriaSummary.service.impl;

import com.DevLewi.SheriaSummary.dto.ChatRequest;
import com.DevLewi.SheriaSummary.dto.ChatResponse;
import com.DevLewi.SheriaSummary.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    private static final String SYSTEM_PROMPT = """
            You are a legal expert specializing in Kenyan law, helping students and founders understand
            new AI and Data Protection legislation.
            
            Rules:
            - Answer ONLY based on the provided document context.
            - Always cite the specific Section, Clause, or Article number when referencing the law.
            - Use simple, plain language that a non-lawyer can understand.
            - If the context does not contain an answer, respond: "This specific information was not found in the provided document. Please consult the full text or a legal professional."
            - Format your response with: a brief summary, then numbered key points, then the specific legal citation.
            """;

    @Override
    public ChatResponse chat(ChatRequest request) {
        if (request.getQuestion() == null || request.getQuestion().isBlank()) {
            throw new IllegalArgumentException("Question cannot be empty");
        }

        List<Document> docs = retrieveDocuments(request);
        log.info("Retrieved {} document chunks for query", docs.size());

        String context = buildContext(docs);
        List<String> sources = buildSources(docs);

        String userPrompt = """
                Context from Kenyan legal documents:
                %s
                
                Question: %s
                """.formatted(context, request.getQuestion());

        String answer = chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(userPrompt)
                .call()
                .content();

        return ChatResponse.builder()
                .answer(answer)
                .sources(sources)
                .chunksRetrieved(docs.size())
                .build();
    }

    @Override
    public Flux<String> streamChat(ChatRequest request) {
        if (request.getQuestion() == null || request.getQuestion().isBlank()) {
            return Flux.error(new IllegalArgumentException("Question cannot be empty"));
        }

        List<Document> docs = retrieveDocuments(request);
        String context = buildContext(docs);

        String userPrompt = """
                Context from Kenyan legal documents:
                %s
                
                Question: %s
                """.formatted(context, request.getQuestion());

        return chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(userPrompt)
                .stream()
                .content();
    }

    @Override
    public List<Document> retrieveDocuments(ChatRequest request) {
        SearchRequest.Builder searchBuilder = SearchRequest.builder()
                .query(request.getQuestion())
                .topK(6);

        if (request.getDocumentId() != null && !request.getDocumentId().isBlank()) {
            searchBuilder.filterExpression("documentId == '" + request.getDocumentId() + "'");
        }

        return vectorStore.similaritySearch(searchBuilder.build());
    }

    @Override
    public String buildContext(List<Document> docs) {
        if (docs.isEmpty()) {
            return "No relevant document sections found.";
        }
        return docs.stream()
                .map(doc -> {
                    String filename = (String) doc.getMetadata().getOrDefault("filename", "Unknown document");
                    Object page = doc.getMetadata().get("page_number");
                    String pageRef = page != null ? " (Page " + page + ")" : "";
                    return "[Source: " + filename + pageRef + "]\n" + doc.getText();
                })
                .collect(Collectors.joining("\n\n---\n\n"));
    }

    @Override
    public List<String> buildSources(List<Document> docs) {
        return docs.stream()
                .map(doc -> {
                    String filename = (String) doc.getMetadata().getOrDefault("filename", "Unknown");
                    Object page = doc.getMetadata().get("page_number");
                    return page != null ? filename + " - Page " + page : filename;
                })
                .distinct()
                .collect(Collectors.toList());
    }
}
