package com.recruitment.candidatemanagement.controller;

import com.recruitment.candidatemanagement.entity.Application;
import com.recruitment.candidatemanagement.entity.JobOffer;
import com.recruitment.candidatemanagement.repository.ApplicationRepository;
import com.recruitment.candidatemanagement.repository.CandidateRepository;
import com.recruitment.candidatemanagement.repository.JobOfferRepository;
import com.recruitment.candidatemanagement.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class DashboardController {
    
    private final ApplicationService applicationService;
    private final ApplicationRepository applicationRepository;
    private final JobOfferRepository jobOfferRepository;
    private final CandidateRepository candidateRepository;
    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Statistiques générales
        stats.put("totalApplications", applicationRepository.count());
        stats.put("totalCandidates", candidateRepository.count());
        stats.put("totalJobOffers", jobOfferRepository.count());
        stats.put("activeJobOffers", jobOfferRepository.countByStatus(JobOffer.JobStatus.PUBLISHED));
        
        // Candidatures par statut
        stats.put("validatedApplications", 
            applicationRepository.countByStatus(Application.ApplicationStatus.VALIDATED));
        stats.put("ambiguousApplications", 
            applicationRepository.countByStatus(Application.ApplicationStatus.AMBIGUOUS));
        stats.put("rejectedApplications", 
            applicationRepository.countByStatus(Application.ApplicationStatus.REJECTED));
        stats.put("pendingApplications", 
            applicationRepository.countByStatus(Application.ApplicationStatus.PENDING));
        
        // Candidatures du jour
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        stats.put("todayApplications", applicationRepository.countTodaysApplications(startOfDay));
        
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/recent-applications")
    public ResponseEntity<List<Application>> getRecentApplications(@RequestParam(defaultValue = "10") int limit) {
        List<Application> applications = applicationService.getAllApplications();
        
        // Limiter les résultats
        List<Application> recent = applications.stream()
            .sorted((a, b) -> b.getReceivedAt().compareTo(a.getReceivedAt()))
            .limit(limit)
            .toList();
        
        return ResponseEntity.ok(recent);
    }
    
    @GetMapping("/applications-by-status")
    public ResponseEntity<Map<String, List<Application>>> getApplicationsByStatus() {
        Map<String, List<Application>> result = new HashMap<>();
        
        result.put("validated", applicationRepository.findByStatus(Application.ApplicationStatus.VALIDATED));
        result.put("ambiguous", applicationRepository.findByStatus(Application.ApplicationStatus.AMBIGUOUS));
        result.put("rejected", applicationRepository.findByStatus(Application.ApplicationStatus.REJECTED));
        result.put("pending", applicationRepository.findByStatus(Application.ApplicationStatus.PENDING));
        
        return ResponseEntity.ok(result);
    }
}