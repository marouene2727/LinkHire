package com.recruitment.candidatemanagement.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "job_offers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobOffer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    private String company;
    

    
    private String location;
    
    @Column(name = "remote_work")
    private Boolean remoteWork = false;
    
    @Column(name = "salary_min")
    private Integer salaryMin;
    
    @Column(name = "salary_max")
    private Integer salaryMax;
    
    @Column(name = "salary_currency")
    private String salaryCurrency = "EUR";
    
    @Column(name = "salary_range")
    private String salaryRange;
    
    @Column(name = "experience_level")
    private String experienceLevel;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Column(name = "preview_visibility", columnDefinition = "TEXT")
    private String previewVisibility;
    
    @Column(name = "experience_min")
    private Integer experienceMin;
    
    @Column(name = "experience_max")
    private Integer experienceMax;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "contract_type")
    private ContractType contractType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "education_level")
    private EducationLevel educationLevel;
    
    @Column(name = "required_skills", columnDefinition = "TEXT")
    private String requiredSkills;
    

    
    @Column(name = "languages", columnDefinition = "TEXT")
    private String languages;
    
    @Column(name = "benefits", columnDefinition = "TEXT")
    private String benefits;
    
    @Column(name = "application_deadline")
    private LocalDateTime applicationDeadline;
    
    @Column(name = "start_date")
    private LocalDateTime startDate;
    
    @Column(name = "contact_email")
    private String contactEmail;
    
    @Column(name = "contact_phone")
    private String contactPhone;
    
    @Column(name = "application_url")
    private String applicationUrl;
    
    @Enumerated(EnumType.STRING)
    private JobStatus status = JobStatus.DRAFT;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    @JsonIgnore
    private User createdBy;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "published_at")
    private LocalDateTime publishedAt;
    
    @OneToMany(mappedBy = "jobOffer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Application> applications;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (applicationUrl == null) {
            applicationUrl = "/apply/" + java.util.UUID.randomUUID().toString();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (status == JobStatus.PUBLISHED && publishedAt == null) {
            publishedAt = LocalDateTime.now();
        }
    }
    
    public enum JobStatus {
        DRAFT, PUBLISHED, CLOSED
    }
    
    public enum ContractType {
        CDI, CDD, FREELANCE, STAGE, APPRENTISSAGE
    }
    
    public enum EducationLevel {
        BAC, BAC_PLUS_2, BAC_PLUS_3, BAC_PLUS_5, DOCTORAT, AUTRE
    }
}