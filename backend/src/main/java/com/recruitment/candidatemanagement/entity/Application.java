package com.recruitment.candidatemanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "applications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Application {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_offer_id", nullable = false)
    @JsonIgnore
    private JobOffer jobOffer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    @JsonIgnore
    private Candidate candidate;
    
    @Column(name = "email_subject")
    private String emailSubject;
    
    @Column(name = "email_body", columnDefinition = "TEXT")
    private String emailBody;
    
    @Column(name = "received_at")
    private LocalDateTime receivedAt;
    
    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;
    
    @Column(name = "ai_score")
    private Integer aiScore; // Score sur 20
    
    @Column(name = "ai_analysis", columnDefinition = "TEXT")
    private String aiAnalysis;
    
    @Column(name = "recruiter_notes", columnDefinition = "TEXT")
    private String recruiterNotes;
    
    @Column(name = "response_sent")
    private Boolean responseSent = false;
    
    @Column(name = "response_sent_at")
    private LocalDateTime responseSentAt;
    
    @Column(name = "viewed_by_recruiter")
    private Boolean viewedByRecruiter = false;
    
    @Column(name = "viewed_at")
    private LocalDateTime viewedAt;
    
    @Column(name = "archived")
    private Boolean archived = false;
    
    @Column(name = "archived_at")
    private LocalDateTime archivedAt;
    
    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<ApplicationDocument> documents;
    
    @PrePersist
    protected void onCreate() {
        if (receivedAt == null) {
            receivedAt = LocalDateTime.now();
        }
    }
    
    public enum ApplicationStatus {
        PENDING,     // En attente d'analyse
        VALIDATED,   // Score >= 15/20
        AMBIGUOUS,   // Score 10-14/20
        REJECTED     // Score < 10/20
    }
}