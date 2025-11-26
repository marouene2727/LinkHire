package com.recruitment.candidatemanagement.controller;

import com.recruitment.candidatemanagement.entity.Candidate;
import com.recruitment.candidatemanagement.repository.CandidateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/candidates")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class CandidateController {
    
    private final CandidateRepository candidateRepository;
    
    @GetMapping
    public ResponseEntity<List<Candidate>> getAllCandidates() {
        return ResponseEntity.ok(candidateRepository.findAll());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Candidate> getCandidateById(@PathVariable Long id) {
        return candidateRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<Candidate>> searchCandidates(@RequestParam String name) {
        List<Candidate> candidates = candidateRepository
            .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(name, name);
        return ResponseEntity.ok(candidates);
    }
    
    @GetMapping("/by-skill")
    public ResponseEntity<List<Candidate>> getCandidatesBySkill(@RequestParam String skill) {
        return ResponseEntity.ok(candidateRepository.findBySkill(skill));
    }
    
    @GetMapping("/by-location")
    public ResponseEntity<List<Candidate>> getCandidatesByLocation(@RequestParam String location) {
        return ResponseEntity.ok(candidateRepository.findByLocation(location));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Candidate> updateCandidate(
            @PathVariable Long id, 
            @RequestBody Candidate candidateDetails) {
        return candidateRepository.findById(id)
            .map(candidate -> {
                candidate.setFirstName(candidateDetails.getFirstName());
                candidate.setLastName(candidateDetails.getLastName());
                candidate.setPhone(candidateDetails.getPhone());
                candidate.setLinkedinProfile(candidateDetails.getLinkedinProfile());
                candidate.setCurrentPosition(candidateDetails.getCurrentPosition());
                candidate.setCurrentCompany(candidateDetails.getCurrentCompany());
                candidate.setLocation(candidateDetails.getLocation());
                candidate.setYearsExperience(candidateDetails.getYearsExperience());
                candidate.setSkills(candidateDetails.getSkills());
                
                return ResponseEntity.ok(candidateRepository.save(candidate));
            })
            .orElse(ResponseEntity.notFound().build());
    }
}