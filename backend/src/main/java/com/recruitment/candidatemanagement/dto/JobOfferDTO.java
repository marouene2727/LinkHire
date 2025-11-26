package com.recruitment.candidatemanagement.dto;

import com.recruitment.candidatemanagement.entity.JobOffer;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobOfferDTO {
    
    private Long id;
    private String title;
    private String description;
    private String company;

    private String location;
    private Boolean remoteWork;
    private Integer salaryMin;
    private Integer salaryMax;
    private String salaryCurrency;
    private String salaryRange;
    private String experienceLevel;
    private LocalDateTime expiresAt;
    private String previewVisibility;
    private Integer experienceMin;
    private Integer experienceMax;
    private JobOffer.ContractType contractType;
    private JobOffer.EducationLevel educationLevel;
    private String requiredSkills;
    private String languages;
    private String benefits;
    private LocalDateTime applicationDeadline;
    private LocalDateTime startDate;
    private String contactEmail;
    private String contactPhone;
    private String applicationUrl;
    private JobOffer.JobStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;
    private Long applicationsCount;
    
    public static JobOfferDTO fromEntity(JobOffer jobOffer) {
        JobOfferDTO dto = new JobOfferDTO();
        dto.setId(jobOffer.getId());
        dto.setTitle(jobOffer.getTitle());
        dto.setDescription(jobOffer.getDescription());
        dto.setCompany(jobOffer.getCompany());

        dto.setLocation(jobOffer.getLocation());
        dto.setRemoteWork(jobOffer.getRemoteWork());
        dto.setSalaryMin(jobOffer.getSalaryMin());
        dto.setSalaryMax(jobOffer.getSalaryMax());
        dto.setSalaryCurrency(jobOffer.getSalaryCurrency());
        dto.setSalaryRange(jobOffer.getSalaryRange());
        dto.setExperienceLevel(jobOffer.getExperienceLevel());
        dto.setExpiresAt(jobOffer.getExpiresAt());
        dto.setPreviewVisibility(jobOffer.getPreviewVisibility());
        dto.setExperienceMin(jobOffer.getExperienceMin());
        dto.setExperienceMax(jobOffer.getExperienceMax());
        dto.setContractType(jobOffer.getContractType());
        dto.setEducationLevel(jobOffer.getEducationLevel());
        dto.setRequiredSkills(jobOffer.getRequiredSkills());
        dto.setLanguages(jobOffer.getLanguages());
        dto.setBenefits(jobOffer.getBenefits());
        dto.setApplicationDeadline(jobOffer.getApplicationDeadline());
        dto.setStartDate(jobOffer.getStartDate());
        dto.setContactEmail(jobOffer.getContactEmail());
        dto.setContactPhone(jobOffer.getContactPhone());
        dto.setApplicationUrl(jobOffer.getApplicationUrl());
        dto.setStatus(jobOffer.getStatus());
        dto.setCreatedAt(jobOffer.getCreatedAt());
        dto.setUpdatedAt(jobOffer.getUpdatedAt());
        dto.setPublishedAt(jobOffer.getPublishedAt());
        // Le compteur sera défini dans le contrôleur
        dto.setApplicationsCount(0L);
        return dto;
    }
}