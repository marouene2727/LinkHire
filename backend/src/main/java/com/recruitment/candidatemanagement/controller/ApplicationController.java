package com.recruitment.candidatemanagement.controller;

import com.recruitment.candidatemanagement.dto.ApplicationDTO;
import com.recruitment.candidatemanagement.dto.ApplicationDetailDTO;
import com.recruitment.candidatemanagement.entity.Application;
import com.recruitment.candidatemanagement.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import com.recruitment.candidatemanagement.entity.ApplicationDocument;
import com.recruitment.candidatemanagement.repository.ApplicationDocumentRepository;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/applications")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ApplicationController {
    
    private final ApplicationService applicationService;
    private final ApplicationDocumentRepository applicationDocumentRepository;
    
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllApplications() {
        List<Application> applications = applicationService.getAllApplications();
        List<Map<String, Object>> result = applications.stream()
            .map(this::mapToListItem)
            .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Map<String, Object>>> getApplicationsByStatus(
            @PathVariable Application.ApplicationStatus status) {
        List<Application> applications = applicationService.getApplicationsByStatus(status);
        List<Map<String, Object>> result = applications.stream()
            .map(this::mapToListItem)
            .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApplicationDetailDTO> getApplicationById(@PathVariable Long id) {
        return applicationService.getApplicationDetailById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/today")
    public ResponseEntity<List<Application>> getTodaysApplications() {
        List<Application> applications = applicationService.getTodaysApplications();
        return ResponseEntity.ok(applications);
    }
    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getApplicationStats() {
        Map<String, Object> stats = Map.of(
            "total", applicationService.getApplicationsCount(),
            "today", applicationService.getTodaysApplicationsCount(),
            "validated", applicationService.getApplicationsByStatus(Application.ApplicationStatus.VALIDATED).size(),
            "ambiguous", applicationService.getApplicationsByStatus(Application.ApplicationStatus.AMBIGUOUS).size(),
            "rejected", applicationService.getApplicationsByStatus(Application.ApplicationStatus.REJECTED).size(),
            "pending", applicationService.getApplicationsByStatus(Application.ApplicationStatus.PENDING).size()
        );
        return ResponseEntity.ok(stats);
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<Application> updateApplicationStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            Application.ApplicationStatus status = Application.ApplicationStatus.valueOf(
                request.get("status").toUpperCase());
            Application updated = applicationService.updateApplicationStatus(id, status);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}/notes")
    public ResponseEntity<Application> addRecruiterNotes(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            String notes = request.get("notes");
            Application updated = applicationService.addRecruiterNotes(id, notes);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/job-offer/{jobOfferId}")
    public ResponseEntity<List<ApplicationDTO>> getApplicationsByJobOffer(@PathVariable Long jobOfferId) {
        List<ApplicationDTO> applications = applicationService.getApplicationsByJobOffer(jobOfferId);
        return ResponseEntity.ok(applications);
    }
    
    @GetMapping("/job-offer/{jobOfferId}/all")
    public ResponseEntity<List<ApplicationDTO>> getAllApplicationsByJobOffer(@PathVariable Long jobOfferId) {
        List<ApplicationDTO> applications = applicationService.getAllApplicationsByJobOffer(jobOfferId);
        return ResponseEntity.ok(applications);
    }
    
    @GetMapping("/archived")
    public ResponseEntity<List<Map<String, Object>>> getArchivedApplications() {
        List<Application> applications = applicationService.getArchivedApplications();
        List<Map<String, Object>> result = applications.stream()
            .map(this::mapToListItem)
            .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/job-offer/{jobOfferId}/validated")
    public ResponseEntity<List<Map<String, Object>>> getValidatedCandidatesByJobOffer(@PathVariable Long jobOfferId) {
        List<Application> applications = applicationService.getValidatedApplicationsByJobOffer(jobOfferId);
        List<Map<String, Object>> result = applications.stream()
            .map(this::mapToValidatedCandidate)
            .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/submit")
    public ResponseEntity<Application> submitApplication(
            @RequestParam("jobOfferId") Long jobOfferId,
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("email") String email,
            @RequestParam("phone") String phone,
            @RequestParam(value = "linkedinUrl", required = false) String linkedinUrl,
            @RequestParam(value = "githubUrl", required = false) String githubUrl,
            @RequestParam(value = "currentPosition", required = false) String currentPosition,
            @RequestParam(value = "currentCompany", required = false) String currentCompany,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam("message") String message,
            @RequestParam("cv") MultipartFile cv,
            @RequestParam(value = "coverLetter", required = false) MultipartFile coverLetter) {
        try {
            Application application = applicationService.createWebApplication(
                jobOfferId, firstName, lastName, email, phone, 
                linkedinUrl, githubUrl, currentPosition, currentCompany, location, 
                message, cv, coverLetter);
            return ResponseEntity.ok(application);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("n'est plus disponible")) {
                return ResponseEntity.status(410).build(); // 410 Gone - Offre expirée
            }
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/documents/{documentId}/download")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable Long documentId,
            @RequestParam(required = false) String token) {
        try {
            ApplicationDocument document = applicationDocumentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document non trouvé"));
            
            Path filePath = Paths.get(document.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                String disposition = "view".equals(token) ? "inline" : "attachment";
                return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, disposition + "; filename=\"" + document.getFileName() + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, document.getContentType())
                    .body(resource);
            } else {
                throw new RuntimeException("Fichier non trouvé ou non lisible");
            }
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/{id}/validate")
    public ResponseEntity<Void> validateApplication(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            String message = request.get("message");
            String interviewDate = request.get("interviewDate");
            applicationService.validateApplication(id, message, interviewDate);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/{id}/reject")
    public ResponseEntity<Void> rejectApplication(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            String message = request.get("message");
            applicationService.rejectApplication(id, message);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/{id}/contact")
    public ResponseEntity<Void> contactCandidate(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            String subject = request.get("subject");
            String message = request.get("message");
            applicationService.contactCandidate(id, subject, message);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    private Map<String, Object> mapToListItem(Application app) {
        Map<String, Object> item = new java.util.HashMap<>();
        item.put("id", app.getId());
        item.put("receivedAt", app.getReceivedAt());
        item.put("status", app.getStatus());
        item.put("aiScore", app.getAiScore());
        item.put("emailSubject", app.getEmailSubject());
        
        Map<String, Object> candidate = new java.util.HashMap<>();
        if (app.getCandidate() != null) {
            candidate.put("firstName", app.getCandidate().getFirstName());
            candidate.put("lastName", app.getCandidate().getLastName());
            candidate.put("email", app.getCandidate().getEmail());
            candidate.put("phone", app.getCandidate().getPhone());
        }
        item.put("candidate", candidate);
        
        return item;
    }
    
    private Map<String, Object> mapToValidatedCandidate(Application app) {
        Map<String, Object> candidate = new java.util.HashMap<>();
        candidate.put("id", app.getId());
        candidate.put("firstName", app.getCandidate().getFirstName());
        candidate.put("lastName", app.getCandidate().getLastName());
        candidate.put("email", app.getCandidate().getEmail());
        candidate.put("phone", app.getCandidate().getPhone());
        candidate.put("aiScore", app.getAiScore());
        candidate.put("validatedAt", app.getReceivedAt()); // Date de candidature
        return candidate;
    }
}