package com.DevLewi.SheriaSummary.controller;

import com.DevLewi.SheriaSummary.dto.ChatResponse;
import com.DevLewi.SheriaSummary.exception.GlobalExceptionHandler;
import com.DevLewi.SheriaSummary.service.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    @Mock
    private ChatService chatService;

    @InjectMocks
    private ChatController chatController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(chatController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void chatReturnsResponse() throws Exception {
        ChatResponse response = ChatResponse.builder()
                .answer(List.of("Data Protection Act requires consent."))
                .sources(List.of("data-protection.pdf - Page 3"))
                .chunksRetrieved(2)
                .build();
        when(chatService.chat(any())).thenReturn(response);

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"What does the Data Protection Act say?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chunksRetrieved").value(2))
                .andExpect(jsonPath("$.answer[0]").value("Data Protection Act requires consent."));
    }

    @Test
    void chatReturnsBadRequestForEmptyQuestion() throws Exception {
        when(chatService.chat(any())).thenThrow(new IllegalArgumentException("Question cannot be empty"));

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Question cannot be empty"));
    }
}
