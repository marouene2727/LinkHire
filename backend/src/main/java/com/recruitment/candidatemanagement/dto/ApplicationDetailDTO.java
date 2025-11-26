package com.recruitment.candidatemanagement.dto;

import com.recruitment.candidatemanagement.entity.Application;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationDetailDTO {
    
    private Long id;
    private CandidateDetailDTO candidate;
    private JobOfferSummaryDTO jobOffer;
    private LocalDateTime receivedAt;
    private Application.ApplicationStatus status;
    private Integer aiScore;
    private String aiAnalysis;
    private String emailSubject;
    private String emailBody;
    private String recruiterNotes;
    private Boolean viewedByRecruiter;
    private LocalDateTime viewedAt;
    private Boolean archived;
    private LocalDateTime archivedAt;
    private List<DocumentDTO> documents;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CandidateDetailDTO {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String linkedinProfile;
        private String githubProfile;
        private String currentPosition;
        private String currentCompany;
        private String location;
        private Integer yearsExperience;
        private String skills;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JobOfferSummaryDTO {
        private Long id;
        private String title;
        private String company;
        private String location;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DocumentDTO {
        private Long id;
        private String fileName;
        private String documentType;
        private Long fileSize;
    }
}