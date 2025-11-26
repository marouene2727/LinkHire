package com.recruitment.candidatemanagement.dto;

import com.recruitment.candidatemanagement.entity.Application;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationDTO {
    
    private Long id;
    private CandidateDTO candidate;
    private LocalDateTime receivedAt;
    private Application.ApplicationStatus status;
    private Integer aiScore;
    private String aiAnalysis;
    private Boolean viewedByRecruiter;
    private LocalDateTime viewedAt;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CandidateDTO {
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String linkedinProfile;
    }
    
    public static ApplicationDTO fromEntity(Application application) {
        ApplicationDTO dto = new ApplicationDTO();
        dto.setId(application.getId());
        dto.setReceivedAt(application.getReceivedAt());
        dto.setStatus(application.getStatus());
        dto.setAiScore(application.getAiScore());
        dto.setAiAnalysis(application.getAiAnalysis());
        dto.setViewedByRecruiter(application.getViewedByRecruiter());
        dto.setViewedAt(application.getViewedAt());
        
        if (application.getCandidate() != null) {
            CandidateDTO candidateDTO = new CandidateDTO();
            candidateDTO.setFirstName(application.getCandidate().getFirstName());
            candidateDTO.setLastName(application.getCandidate().getLastName());
            candidateDTO.setEmail(application.getCandidate().getEmail());
            candidateDTO.setPhone(application.getCandidate().getPhone());
            candidateDTO.setLinkedinProfile(application.getCandidate().getLinkedinProfile());
            dto.setCandidate(candidateDTO);
        }
        
        return dto;
    }
}