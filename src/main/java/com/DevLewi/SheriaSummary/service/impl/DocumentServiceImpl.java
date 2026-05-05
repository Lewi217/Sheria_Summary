package com.DevLewi.SheriaSummary.service.impl;

import com.DevLewi.SheriaSummary.dto.UploadResponse;
import com.DevLewi.SheriaSummary.model.DocumentRecord;
import com.DevLewi.SheriaSummary.repository.DocumentRepository;
import com.DevLewi.SheriaSummary.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final VectorStore vectorStore;
    private final DocumentRepository documentRepository;

    @Override
    public UploadResponse ingestPdf(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Only PDF files are supported");
        }

        byte[] pdfBytes = file.getBytes();
        ByteArrayResource pdfResource = new ByteArrayResource(pdfBytes) {
            @Override
            public String getFilename() {
                return filename;
            }
        };

        var config = PdfDocumentReaderConfig.builder()
                .withPagesPerDocument(1)
                .build();

        var pdfReader = new PagePdfDocumentReader(pdfResource, config);

        List<Document> rawDocs = pdfReader.read();
        log.info("Read {} pages from {}", rawDocs.size(), filename);

        String docId = UUID.randomUUID().toString();

        rawDocs.forEach(doc -> {
            doc.getMetadata().put("documentId", docId);
            doc.getMetadata().put("filename", filename);
            doc.getMetadata().put("source", filename);
        });

        var splitter = TokenTextSplitter.builder()
                .withChunkSize(512)
                .withMinChunkSizeChars(128)
                .withMinChunkLengthToEmbed(5)
                .withMaxNumChunks(10000)
                .withKeepSeparator(true)
                .build();
        List<Document> chunks = splitter.apply(rawDocs);
        log.info("Split into {} chunks for {}", chunks.size(), filename);

        vectorStore.add(chunks);

        DocumentRecord record = DocumentRecord.builder()
                .id(docId)
                .filename(filename)
                .uploadTime(LocalDateTime.now())
                .chunkCount(chunks.size())
                .fileSize(file.getSize())
                .build();

        documentRepository.save(record);
        log.info("Saved document metadata for {}", filename);

        return UploadResponse.builder()
                .documentId(docId)
                .filename(filename)
                .chunksCreated(chunks.size())
                .message("Document ingested successfully. Ready for querying.")
                .build();
    }

    @Override
    public List<DocumentRecord> listDocuments() {
        return documentRepository.findAll();
    }

    @Override
    public boolean deleteDocument(String documentId) {
        if (!documentRepository.existsById(documentId)) {
            return false;
        }
        documentRepository.deleteById(documentId);
        return true;
    }
}
