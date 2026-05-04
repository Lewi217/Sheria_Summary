package com.DevLewi.SheriaSummary.service;

import com.DevLewi.SheriaSummary.dto.UploadResponse;
import com.DevLewi.SheriaSummary.model.DocumentRecord;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface DocumentService {
    UploadResponse ingestPdf(MultipartFile file) throws IOException;
    List<DocumentRecord> listDocuments();
    boolean deleteDocument(String documentId);

}
