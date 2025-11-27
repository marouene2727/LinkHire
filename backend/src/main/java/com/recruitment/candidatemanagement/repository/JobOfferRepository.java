package com.recruitment.candidatemanagement.repository;

import com.recruitment.candidatemanagement.entity.JobOffer;
import com.recruitment.candidatemanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface JobOfferRepository extends JpaRepository<JobOffer, Long> {
    
    List<JobOffer> findByStatus(JobOffer.JobStatus status);
    
    List<JobOffer> findByStatusOrderByCreatedAtDesc(JobOffer.JobStatus status);
    
    @Query("SELECT j FROM JobOffer j WHERE j.title LIKE %:keyword% OR j.description LIKE %:keyword%")
    List<JobOffer> findByKeyword(String keyword);
    
    java.util.Optional<JobOffer> findByApplicationUrl(String applicationUrl);
    
    long countByStatus(JobOffer.JobStatus status);
    
    List<JobOffer> findByCreatedBy(User createdBy);
}