package com.DevLewi.SheriaSummary.controller;

import com.DevLewi.SheriaSummary.dto.UploadResponse;
import com.DevLewi.SheriaSummary.model.DocumentRecord;
import com.DevLewi.SheriaSummary.service.DocumentService;
import com.DevLewi.SheriaSummary.service.impl.DocumentServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> upload(
            @RequestParam("file") MultipartFile file) {
        try {
            UploadResponse response = documentService.ingestPdf(file);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.badRequest()
                    .body(UploadResponse.builder()
                            .message("File processing error: " + e.getMessage())
                            .build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(UploadResponse.builder()
                            .message(e.getMessage())
                            .build());
        }
    }

    @GetMapping
    public ResponseEntity<List<DocumentRecord>> listDocuments() {
        return ResponseEntity.ok(documentService.listDocuments());
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<Void> deleteDocument(@PathVariable String documentId) {
        boolean deleted = documentService.deleteDocument(documentId);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
