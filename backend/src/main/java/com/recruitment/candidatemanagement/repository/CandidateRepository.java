package com.recruitment.candidatemanagement.repository;

import com.recruitment.candidatemanagement.entity.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {
    
    Optional<Candidate> findByEmail(String email);
    
    List<Candidate> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
        String firstName, String lastName);
    
    @Query("SELECT c FROM Candidate c WHERE c.skills LIKE %:skill%")
    List<Candidate> findBySkill(String skill);
    
    List<Candidate> findByLocation(String location);
    
    @Query("SELECT c FROM Candidate c WHERE c.yearsExperience >= :minYears")
    List<Candidate> findByMinimumExperience(Integer minYears);
    
    boolean existsByEmail(String email);
}