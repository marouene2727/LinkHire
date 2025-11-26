package com.recruitment.candidatemanagement.service;

import com.recruitment.candidatemanagement.entity.JobOffer;
import com.recruitment.candidatemanagement.entity.User;
import com.recruitment.candidatemanagement.repository.JobOfferRepository;
import com.recruitment.candidatemanagement.repository.UserRepository;
import com.recruitment.candidatemanagement.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class JobOfferService {
    
    private final JobOfferRepository jobOfferRepository;
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    
    public List<JobOffer> getAllJobOffers() {
        // Récupérer l'utilisateur connecté
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String username = auth.getName();
            Optional<User> user = userRepository.findByUsername(username);
            if (user.isPresent()) {
                return jobOfferRepository.findByCreatedBy(user.get());
            }
        }
        return jobOfferRepository.findAll();
    }
    
    public Optional<JobOffer> getJobOfferById(Long id) {
        return jobOfferRepository.findById(id);
    }
    
    public List<JobOffer> getActiveJobOffers() {
        return jobOfferRepository.findByStatusOrderByCreatedAtDesc(JobOffer.JobStatus.PUBLISHED);
    }
    
    public JobOffer createJobOffer(JobOffer jobOffer) {
        jobOffer.setStatus(JobOffer.JobStatus.DRAFT);
        
        // Récupérer l'utilisateur connecté
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String username = auth.getName();
            Optional<User> user = userRepository.findByUsername(username);
            user.ifPresent(jobOffer::setCreatedBy);
        }
        
        return jobOfferRepository.save(jobOffer);
    }
    
    public JobOffer updateJobOffer(Long id, JobOffer jobOfferDetails) {
        JobOffer jobOffer = jobOfferRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Offre d'emploi non trouvée"));
        
        // Informations générales
        jobOffer.setTitle(jobOfferDetails.getTitle());
        jobOffer.setDescription(jobOfferDetails.getDescription());
        jobOffer.setCompany(jobOfferDetails.getCompany());
        
        // Localisation
        jobOffer.setLocation(jobOfferDetails.getLocation());
        jobOffer.setRemoteWork(jobOfferDetails.getRemoteWork());
        jobOffer.setContractType(jobOfferDetails.getContractType());
        
        // Rémunération
        jobOffer.setSalaryMin(jobOfferDetails.getSalaryMin());
        jobOffer.setSalaryMax(jobOfferDetails.getSalaryMax());
        jobOffer.setSalaryCurrency(jobOfferDetails.getSalaryCurrency());
        jobOffer.setSalaryRange(jobOfferDetails.getSalaryRange());
        
        // Profil
        jobOffer.setExperienceMin(jobOfferDetails.getExperienceMin());
        jobOffer.setExperienceMax(jobOfferDetails.getExperienceMax());
        jobOffer.setExperienceLevel(jobOfferDetails.getExperienceLevel());
        jobOffer.setEducationLevel(jobOfferDetails.getEducationLevel());
        jobOffer.setRequiredSkills(jobOfferDetails.getRequiredSkills());
        jobOffer.setLanguages(jobOfferDetails.getLanguages());
        jobOffer.setBenefits(jobOfferDetails.getBenefits());
        jobOffer.setPreviewVisibility(jobOfferDetails.getPreviewVisibility());
        
        // Dates et contact
        jobOffer.setApplicationDeadline(jobOfferDetails.getApplicationDeadline());
        jobOffer.setStartDate(jobOfferDetails.getStartDate());
        jobOffer.setContactEmail(jobOfferDetails.getContactEmail());
        jobOffer.setContactPhone(jobOfferDetails.getContactPhone());
        
        return jobOfferRepository.save(jobOffer);
    }
    
    public JobOffer publishJobOffer(Long id) {
        JobOffer jobOffer = jobOfferRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Offre d'emploi non trouvée"));
        
        jobOffer.setStatus(JobOffer.JobStatus.PUBLISHED);
        jobOffer.setPublishedAt(LocalDateTime.now());
        
        return jobOfferRepository.save(jobOffer);
    }
    
    public JobOffer closeJobOffer(Long id) {
        JobOffer jobOffer = jobOfferRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Offre d'emploi non trouvée"));
        
        jobOffer.setStatus(JobOffer.JobStatus.CLOSED);
        
        return jobOfferRepository.save(jobOffer);
    }
    
    public void deleteJobOffer(Long id) {
        jobOfferRepository.deleteById(id);
    }
    
    public List<JobOffer> searchJobOffers(String keyword) {
        return jobOfferRepository.findByKeyword(keyword);
    }
    
    public Optional<JobOffer> getJobOfferByApplicationUrl(String applicationUrl) {
        return jobOfferRepository.findByApplicationUrl(applicationUrl);
    }
    
    public long getApplicationsCount(Long jobOfferId) {
        return applicationRepository.findByJobOfferId(jobOfferId).size();
    }
}