package com.recruitment.candidatemanagement.service;

import com.recruitment.candidatemanagement.dto.ApplicationDTO;
import com.recruitment.candidatemanagement.dto.ApplicationDetailDTO;
import com.recruitment.candidatemanagement.entity.Application;
import com.recruitment.candidatemanagement.entity.ApplicationDocument;
import com.recruitment.candidatemanagement.entity.Candidate;
import com.recruitment.candidatemanagement.entity.JobOffer;
import com.recruitment.candidatemanagement.repository.ApplicationRepository;
import com.recruitment.candidatemanagement.repository.CandidateRepository;
import com.recruitment.candidatemanagement.repository.JobOfferRepository;
import com.recruitment.candidatemanagement.repository.ApplicationDocumentRepository;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ApplicationService {
    
    private final ApplicationRepository applicationRepository;
    private final CandidateRepository candidateRepository;
    private final JobOfferRepository jobOfferRepository;
    private final ApplicationDocumentRepository applicationDocumentRepository;
    private final AIAnalysisService aiAnalysisService;
    private final FileUploadService fileUploadService;
    private final EmailService emailService;
    
    public List<Application> getAllApplications() {
        return applicationRepository.findAllWithCandidates();
    }
    
    public List<Application> getApplicationsByStatus(Application.ApplicationStatus status) {
        return applicationRepository.findByStatusWithCandidates(status);
    }
    
    public Optional<Application> getApplicationById(Long id) {
        return applicationRepository.findById(id);
    }
    
    public Optional<ApplicationDetailDTO> getApplicationDetailById(Long id) {
        return applicationRepository.findByIdWithDetails(id)
                .map(application -> {
                    // Marquer comme vue
                    if (!Boolean.TRUE.equals(application.getViewedByRecruiter())) {
                        application.setViewedByRecruiter(true);
                        application.setViewedAt(LocalDateTime.now());
                        applicationRepository.save(application);
                    }
                    return mapToApplicationDetailDTO(application);
                });
    }
    
    private ApplicationDetailDTO mapToApplicationDetailDTO(Application application) {
        ApplicationDetailDTO dto = new ApplicationDetailDTO();
        dto.setId(application.getId());
        dto.setReceivedAt(application.getReceivedAt());
        dto.setStatus(application.getStatus());
        dto.setAiScore(application.getAiScore());
        dto.setAiAnalysis(application.getAiAnalysis());
        dto.setEmailSubject(application.getEmailSubject());
        dto.setEmailBody(application.getEmailBody());
        dto.setRecruiterNotes(application.getRecruiterNotes());
        dto.setViewedByRecruiter(application.getViewedByRecruiter());
        dto.setViewedAt(application.getViewedAt());
        dto.setArchived(application.getArchived());
        dto.setArchivedAt(application.getArchivedAt());
        
        // Candidat
        if (application.getCandidate() != null) {
            ApplicationDetailDTO.CandidateDetailDTO candidateDTO = new ApplicationDetailDTO.CandidateDetailDTO();
            candidateDTO.setId(application.getCandidate().getId());
            candidateDTO.setFirstName(application.getCandidate().getFirstName());
            candidateDTO.setLastName(application.getCandidate().getLastName());
            candidateDTO.setEmail(application.getCandidate().getEmail());
            candidateDTO.setPhone(application.getCandidate().getPhone());
            candidateDTO.setLinkedinProfile(application.getCandidate().getLinkedinProfile());
            candidateDTO.setGithubProfile(application.getCandidate().getGithubProfile());
            candidateDTO.setCurrentPosition(application.getCandidate().getCurrentPosition());
            candidateDTO.setCurrentCompany(application.getCandidate().getCurrentCompany());
            candidateDTO.setLocation(application.getCandidate().getLocation());
            candidateDTO.setYearsExperience(application.getCandidate().getYearsExperience());
            candidateDTO.setSkills(application.getCandidate().getSkills());
            dto.setCandidate(candidateDTO);
        }
        
        // Offre d'emploi
        if (application.getJobOffer() != null) {
            ApplicationDetailDTO.JobOfferSummaryDTO jobOfferDTO = new ApplicationDetailDTO.JobOfferSummaryDTO();
            jobOfferDTO.setId(application.getJobOffer().getId());
            jobOfferDTO.setTitle(application.getJobOffer().getTitle());
            jobOfferDTO.setCompany(application.getJobOffer().getCompany());
            jobOfferDTO.setLocation(application.getJobOffer().getLocation());
            dto.setJobOffer(jobOfferDTO);
        }
        
        // Documents
        if (application.getDocuments() != null) {
            List<ApplicationDetailDTO.DocumentDTO> documentDTOs = application.getDocuments().stream()
                    .map(doc -> {
                        ApplicationDetailDTO.DocumentDTO docDTO = new ApplicationDetailDTO.DocumentDTO();
                        docDTO.setId(doc.getId());
                        docDTO.setFileName(doc.getFileName());
                        docDTO.setDocumentType(doc.getDocumentType().toString());
                        docDTO.setFileSize(doc.getFileSize());
                        return docDTO;
                    })
                    .collect(java.util.stream.Collectors.toList());
            dto.setDocuments(documentDTOs);
        }
        
        return dto;
    }
    
    public Application processNewApplication(String candidateEmail, String emailSubject, 
                                           String emailBody, JobOffer jobOffer) {
        log.info("Traitement d'une nouvelle candidature de: {}", candidateEmail);
        
        // Rechercher ou créer le candidat
        Candidate candidate = candidateRepository.findByEmail(candidateEmail)
            .orElseGet(() -> createCandidateFromEmail(candidateEmail, emailBody));
        
        // Créer la candidature
        Application application = new Application();
        application.setCandidate(candidate);
        application.setJobOffer(jobOffer);
        application.setEmailSubject(emailSubject);
        application.setEmailBody(emailBody);
        application.setReceivedAt(LocalDateTime.now());
        application.setStatus(Application.ApplicationStatus.PENDING);
        
        // Sauvegarder la candidature
        application = applicationRepository.save(application);
        
        // Lancer l'analyse IA en arrière-plan
        aiAnalysisService.analyzeApplicationAsync(application.getId());
        
        log.info("Candidature créée avec l'ID: {}", application.getId());
        return application;
    }
    
    public Application updateApplicationStatus(Long applicationId, Application.ApplicationStatus status) {
        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new RuntimeException("Candidature non trouvée"));
        
        application.setStatus(status);
        return applicationRepository.save(application);
    }
    
    public Application addRecruiterNotes(Long applicationId, String notes) {
        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new RuntimeException("Candidature non trouvée"));
        
        application.setRecruiterNotes(notes);
        return applicationRepository.save(application);
    }
    
    public List<Application> getTodaysApplications() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        return applicationRepository.findByDateRange(startOfDay, endOfDay);
    }
    
    public long getApplicationsCount() {
        return applicationRepository.count();
    }
    
    public long getTodaysApplicationsCount() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        return applicationRepository.countTodaysApplications(startOfDay);
    }
    
    public List<ApplicationDTO> getApplicationsByJobOffer(Long jobOfferId) {
        List<Object[]> results = applicationRepository.findApplicationsWithCandidateByJobOfferId(jobOfferId);
        return results.stream()
                .map(this::mapToApplicationDTO)
                .collect(java.util.stream.Collectors.toList());
    }
    
    public List<ApplicationDTO> getAllApplicationsByJobOffer(Long jobOfferId) {
        List<Object[]> results = applicationRepository.findAllApplicationsWithCandidateByJobOfferId(jobOfferId);
        return results.stream()
                .map(this::mapToApplicationDTO)
                .collect(java.util.stream.Collectors.toList());
    }
    
    public List<Application> getArchivedApplications() {
        return applicationRepository.findArchivedWithCandidates();
    }
    
    public List<Application> getValidatedApplicationsByJobOffer(Long jobOfferId) {
        return applicationRepository.findByJobOfferIdAndStatus(jobOfferId, Application.ApplicationStatus.VALIDATED);
    }
    
    private ApplicationDTO mapToApplicationDTO(Object[] row) {
        ApplicationDTO dto = new ApplicationDTO();
        dto.setId(((Number) row[0]).longValue());
        
        // Conversion Timestamp vers LocalDateTime
        if (row[1] instanceof java.sql.Timestamp) {
            dto.setReceivedAt(((java.sql.Timestamp) row[1]).toLocalDateTime());
        }
        
        dto.setStatus(Application.ApplicationStatus.valueOf((String) row[2]));
        dto.setAiScore(row[3] != null ? ((Number) row[3]).intValue() : null);
        dto.setAiAnalysis((String) row[4]);
        
        ApplicationDTO.CandidateDTO candidateDTO = new ApplicationDTO.CandidateDTO();
        candidateDTO.setFirstName((String) row[5]);
        candidateDTO.setLastName((String) row[6]);
        candidateDTO.setEmail((String) row[7]);
        candidateDTO.setPhone((String) row[8]);
        candidateDTO.setLinkedinProfile((String) row[9]);
        dto.setCandidate(candidateDTO);
        
        // Ajouter les champs viewed depuis la requête SQL
        dto.setViewedByRecruiter(row[10] != null ? (Boolean) row[10] : false);
        if (row[11] instanceof java.sql.Timestamp) {
            dto.setViewedAt(((java.sql.Timestamp) row[11]).toLocalDateTime());
        }
        
        return dto;
    }
    
    private Candidate createCandidateFromEmail(String email, String emailBody) {
        log.info("Création d'un nouveau candidat pour l'email: {}", email);
        
        Candidate candidate = new Candidate();
        candidate.setEmail(email);
        
        // Extraction basique du nom depuis l'email (à améliorer avec l'IA)
        String[] emailParts = email.split("@")[0].split("\\.");
        if (emailParts.length >= 2) {
            candidate.setFirstName(capitalize(emailParts[0]));
            candidate.setLastName(capitalize(emailParts[1]));
        } else {
            candidate.setFirstName("Prénom");
            candidate.setLastName(emailParts[0]);
        }
        
        return candidateRepository.save(candidate);
    }
    
    public Application createWebApplication(Long jobOfferId, String firstName, String lastName, 
                                          String email, String phone, String linkedinUrl, 
                                          String githubUrl, String currentPosition, String currentCompany, 
                                          String location, String message, 
                                          MultipartFile cv, MultipartFile coverLetter) {
        log.info("Création d'une candidature web pour: {} {}", firstName, lastName);
        
        // Récupérer l'offre d'emploi
        JobOffer jobOffer = jobOfferRepository.findById(jobOfferId)
            .orElseThrow(() -> new RuntimeException("Offre d'emploi non trouvée"));
        
        // Vérifier que l'offre est encore ouverte
        if (jobOffer.getStatus() != JobOffer.JobStatus.PUBLISHED) {
            throw new RuntimeException("Cette offre d'emploi n'est plus disponible pour les candidatures");
        }
        
        // Rechercher ou créer le candidat
        Candidate candidate = candidateRepository.findByEmail(email)
            .orElseGet(() -> createWebCandidate(firstName, lastName, email, phone, linkedinUrl, githubUrl, currentPosition, currentCompany, location));
        
        // Créer la candidature
        Application application = new Application();
        application.setCandidate(candidate);
        application.setJobOffer(jobOffer);
        application.setEmailSubject("Candidature pour " + jobOffer.getTitle());
        application.setEmailBody(message);
        application.setReceivedAt(LocalDateTime.now());
        application.setStatus(Application.ApplicationStatus.PENDING);
        
        // Sauvegarder la candidature
        application = applicationRepository.save(application);
        
        // Gérer les fichiers
        if (cv != null && !cv.isEmpty()) {
            ApplicationDocument cvDoc = fileUploadService.saveFile(cv, application);
            cvDoc.setDocumentType(ApplicationDocument.DocumentType.CV);
            applicationDocumentRepository.save(cvDoc);
        }
        if (coverLetter != null && !coverLetter.isEmpty()) {
            ApplicationDocument coverDoc = fileUploadService.saveFile(coverLetter, application);
            coverDoc.setDocumentType(ApplicationDocument.DocumentType.COVER_LETTER);
            applicationDocumentRepository.save(coverDoc);
        }
        
        // Lancer l'analyse IA en arrière-plan
        aiAnalysisService.analyzeApplicationAsync(application.getId());
        
        log.info("Candidature web créée avec l'ID: {}", application.getId());
        return application;
    }
    
    private Candidate createWebCandidate(String firstName, String lastName, String email, 
                                        String phone, String linkedinUrl, String githubUrl,
                                        String currentPosition, String currentCompany, String location) {
        log.info("Création d'un nouveau candidat web: {} {}", firstName, lastName);
        
        Candidate candidate = new Candidate();
        candidate.setFirstName(firstName);
        candidate.setLastName(lastName);
        candidate.setEmail(email);
        candidate.setPhone(phone);
        candidate.setLinkedinProfile(linkedinUrl);
        candidate.setGithubProfile(githubUrl);
        candidate.setCurrentPosition(currentPosition);
        candidate.setCurrentCompany(currentCompany);
        candidate.setLocation(location);
        
        return candidateRepository.save(candidate);
    }
    
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
    
    public void validateApplication(Long id, String message, String interviewDate) {
        Application application = applicationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Application non trouvée"));
        
        application.setStatus(Application.ApplicationStatus.VALIDATED);
        application.setResponseSent(true);
        applicationRepository.save(application);
        
        // Envoyer email de validation
        emailService.sendValidationEmail(
            application.getCandidate().getEmail(),
            application.getCandidate().getFirstName(),
            application.getJobOffer().getTitle(),
            application.getJobOffer().getCompany(),
            message,
            interviewDate
        );
        
        log.info("Candidature {} validée avec message d'acceptation", id);
    }
    
    public void rejectApplication(Long id, String message) {
        Application application = applicationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Application non trouvée"));
        
        application.setStatus(Application.ApplicationStatus.REJECTED);
        application.setResponseSent(true);
        application.setArchived(true);
        application.setArchivedAt(LocalDateTime.now());
        applicationRepository.save(application);
        
        // Envoyer email de rejet
        emailService.sendRejectionEmail(
            application.getCandidate().getEmail(),
            application.getCandidate().getFirstName(),
            application.getJobOffer().getTitle(),
            message
        );
        
        log.info("Candidature {} rejetée et archivée", id);
    }
    
    public void contactCandidate(Long id, String subject, String message) {
        Application application = applicationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Application non trouvée"));
        
        // Envoyer email de contact
        emailService.sendContactEmail(
            application.getCandidate().getEmail(),
            application.getCandidate().getFirstName(),
            subject,
            message
        );
        
        log.info("Contact candidat pour candidature {} avec sujet: {}", id, subject);
    }
}