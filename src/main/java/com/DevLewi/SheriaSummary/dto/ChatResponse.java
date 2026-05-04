package com.DevLewi.SheriaSummary.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ChatResponse {
    private String answer;
    private List<String> sources;
    private int chunksRetrieved;
}
