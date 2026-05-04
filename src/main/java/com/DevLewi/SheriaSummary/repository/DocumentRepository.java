package com.DevLewi.SheriaSummary.repository;

import com.DevLewi.SheriaSummary.model.DocumentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends JpaRepository<DocumentRecord, String> {
}
