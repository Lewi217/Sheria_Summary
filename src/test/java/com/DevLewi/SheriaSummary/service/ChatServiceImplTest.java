package com.DevLewi.SheriaSummary.service;

import com.DevLewi.SheriaSummary.dto.ChatRequest;
import com.DevLewi.SheriaSummary.service.impl.ChatServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceImplTest {

    @Mock
    private ChatClient chatClient;

    @Mock
    private VectorStore vectorStore;

    @InjectMocks
    private ChatServiceImpl service;

    // --- buildContext ---

    @Test
    void buildContextReturnsFallbackWhenEmpty() {
        assertEquals("No relevant document sections found.", service.buildContext(List.of()));
    }

    @Test
    void buildContextIncludesFilenameAndPage() {
        Document doc = mockDoc("The right to privacy is protected.", "constitution.pdf", 12);
        String ctx = service.buildContext(List.of(doc));
        assertTrue(ctx.contains("constitution.pdf"));
        assertTrue(ctx.contains("Page 12"));
        assertTrue(ctx.contains("The right to privacy is protected."));
    }

    @Test
    void buildContextOmitsPageRefWhenAbsent() {
        Document doc = mockDocNoPage("Some text.", "law.pdf");
        String ctx = service.buildContext(List.of(doc));
        assertTrue(ctx.contains("law.pdf"));
        assertFalse(ctx.contains("Page"));
    }

    @Test
    void buildContextSeparatesMultipleDocsWithDivider() {
        Document doc1 = mockDoc("Text one.", "a.pdf", 1);
        Document doc2 = mockDoc("Text two.", "b.pdf", 2);
        String ctx = service.buildContext(List.of(doc1, doc2));
        assertTrue(ctx.contains("---"));
    }

    // --- buildSources ---

    @Test
    void buildSourcesIncludesPageWhenPresent() {
        Document doc = mockDoc("text", "act.pdf", 5);
        List<String> sources = service.buildSources(List.of(doc));
        assertEquals(List.of("act.pdf - Page 5"), sources);
    }

    @Test
    void buildSourcesOmitsPageWhenAbsent() {
        Document doc = mockDocNoPage("text", "act.pdf");
        List<String> sources = service.buildSources(List.of(doc));
        assertEquals(List.of("act.pdf"), sources);
    }

    @Test
    void buildSourcesDeduplicates() {
        Document doc1 = mockDoc("text1", "act.pdf", 3);
        Document doc2 = mockDoc("text2", "act.pdf", 3);
        List<String> sources = service.buildSources(List.of(doc1, doc2));
        assertEquals(1, sources.size());
    }

    // --- input validation ---

    @Test
    void chatThrowsForNullQuestion() {
        assertThrows(IllegalArgumentException.class,
                () -> service.chat(new ChatRequest(null, null)));
    }

    @Test
    void chatThrowsForBlankQuestion() {
        assertThrows(IllegalArgumentException.class,
                () -> service.chat(new ChatRequest("   ", null)));
    }

    @Test
    void streamChatReturnsErrorFluxForEmptyQuestion() {
        StepVerifier.create(service.streamChat(new ChatRequest("", null)))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    // --- helpers ---

    private Document mockDoc(String text, String filename, int page) {
        Document doc = mock(Document.class);
        when(doc.getText()).thenReturn(text);
        when(doc.getMetadata()).thenReturn(Map.of("filename", filename, "page_number", page));
        return doc;
    }

    private Document mockDocNoPage(String text, String filename) {
        Document doc = mock(Document.class);
        when(doc.getText()).thenReturn(text);
        when(doc.getMetadata()).thenReturn(Map.of("filename", filename));
        return doc;
    }
}
