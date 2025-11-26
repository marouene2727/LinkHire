package com.recruitment.candidatemanagement.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "application_documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationDocument {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;
    
    @Column(nullable = false)
    private String fileName;
    
    @Column(nullable = false)
    private String originalFileName;
    
    @Column(nullable = false)
    private String filePath;
    
    @Column(nullable = false)
    private String contentType;
    
    private Long fileSize;
    
    @Enumerated(EnumType.STRING)
    private DocumentType documentType;
    
    @Column(name = "extracted_text", columnDefinition = "TEXT")
    private String extractedText;
    
    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;
    
    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
    }
    
    public enum DocumentType {
        CV, COVER_LETTER, PORTFOLIO, OTHER
    }
}