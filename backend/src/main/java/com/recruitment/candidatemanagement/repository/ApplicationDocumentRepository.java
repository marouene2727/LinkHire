package com.recruitment.candidatemanagement.repository;

import com.recruitment.candidatemanagement.entity.ApplicationDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationDocumentRepository extends JpaRepository<ApplicationDocument, Long> {
    List<ApplicationDocument> findByApplicationId(Long applicationId);
    List<ApplicationDocument> findByDocumentType(String documentType);
}