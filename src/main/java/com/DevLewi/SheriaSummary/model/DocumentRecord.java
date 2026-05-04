package com.DevLewi.SheriaSummary.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentRecord {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false)
    private String filename;

    @Column(name = "upload_time")
    private LocalDateTime uploadTime;

    @Column(name = "chunk_count")
    private int chunkCount;

    @Column(name = "file_size")
    private long fileSize;
}
