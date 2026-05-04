package com.DevLewi.SheriaSummary.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UploadResponse {
    private String documentId;
    private String filename;
    private int chunksCreated;
    private String message;
}
