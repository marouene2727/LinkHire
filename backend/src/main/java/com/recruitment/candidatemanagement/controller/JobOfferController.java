package com.recruitment.candidatemanagement.controller;

import com.recruitment.candidatemanagement.entity.JobOffer;
import com.recruitment.candidatemanagement.dto.JobOfferDTO;
import com.recruitment.candidatemanagement.service.JobOfferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/job-offers")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class JobOfferController {
    
    private final JobOfferService jobOfferService;
    
    @GetMapping
    public ResponseEntity<List<JobOfferDTO>> getAllJobOffers() {
        List<JobOfferDTO> jobOffers = jobOfferService.getAllJobOffers()
            .stream()
            .map(jobOffer -> {
                JobOfferDTO dto = JobOfferDTO.fromEntity(jobOffer);
                dto.setApplicationsCount(jobOfferService.getApplicationsCount(jobOffer.getId()));
                return dto;
            })
            .collect(Collectors.toList());
        return ResponseEntity.ok(jobOffers);
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<JobOffer>> getActiveJobOffers() {
        return ResponseEntity.ok(jobOfferService.getActiveJobOffers());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<JobOfferDTO> getJobOfferById(@PathVariable String id) {
        try {
            Long longId = Long.parseLong(id);
            return jobOfferService.getJobOfferById(longId)
                .map(jobOffer -> {
                    JobOfferDTO dto = JobOfferDTO.fromEntity(jobOffer);
                    dto.setApplicationsCount(jobOfferService.getApplicationsCount(jobOffer.getId()));
                    return dto;
                })
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/apply/{applicationUrl}")
    public ResponseEntity<JobOfferDTO> getJobOfferByApplicationUrl(@PathVariable String applicationUrl) {
        var jobOfferOpt = jobOfferService.getJobOfferByApplicationUrl("/apply/" + applicationUrl);
        if (jobOfferOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        JobOffer jobOffer = jobOfferOpt.get();
        if (jobOffer.getStatus() != JobOffer.JobStatus.PUBLISHED) {
            return ResponseEntity.status(410).build();
        }
        
        JobOfferDTO dto = JobOfferDTO.fromEntity(jobOffer);
        dto.setApplicationsCount(jobOfferService.getApplicationsCount(jobOffer.getId()));
        return ResponseEntity.ok(dto);
    }
    
    @PostMapping
    public ResponseEntity<JobOfferDTO> createJobOffer(@RequestBody JobOffer jobOffer) {
        JobOffer created = jobOfferService.createJobOffer(jobOffer);
        return ResponseEntity.ok(JobOfferDTO.fromEntity(created));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<JobOffer> updateJobOffer(
            @PathVariable Long id, 
            @RequestBody JobOffer jobOffer) {
        try {
            JobOffer updated = jobOfferService.updateJobOffer(id, jobOffer);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PatchMapping("/{id}/publish")
    public ResponseEntity<JobOffer> publishJobOffer(@PathVariable Long id) {
        try {
            JobOffer published = jobOfferService.publishJobOffer(id);
            return ResponseEntity.ok(published);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PatchMapping("/{id}/close")
    public ResponseEntity<JobOffer> closeJobOffer(@PathVariable Long id) {
        try {
            JobOffer closed = jobOfferService.closeJobOffer(id);
            return ResponseEntity.ok(closed);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJobOffer(@PathVariable Long id) {
        jobOfferService.deleteJobOffer(id);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<JobOffer>> searchJobOffers(@RequestParam String keyword) {
        return ResponseEntity.ok(jobOfferService.searchJobOffers(keyword));
    }
}