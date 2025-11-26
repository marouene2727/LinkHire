package com.recruitment.candidatemanagement.repository;

import com.recruitment.candidatemanagement.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    
    List<Application> findByStatus(Application.ApplicationStatus status);
    
    List<Application> findByJobOfferId(Long jobOfferId);
    
    @Query(value = "SELECT a.id, a.received_at, a.status, a.ai_score, a.ai_analysis, " +
           "c.first_name, c.last_name, c.email, c.phone, c.linkedin_profile, " +
           "a.viewed_by_recruiter, a.viewed_at " +
           "FROM applications a JOIN candidates c ON a.candidate_id = c.id " +
           "WHERE a.job_offer_id = :jobOfferId AND (a.archived IS NULL OR a.archived = false)", nativeQuery = true)
    List<Object[]> findApplicationsWithCandidateByJobOfferId(@Param("jobOfferId") Long jobOfferId);
    
    @Query(value = "SELECT a.id, a.received_at, a.status, a.ai_score, a.ai_analysis, " +
           "c.first_name, c.last_name, c.email, c.phone, c.linkedin_profile, " +
           "a.viewed_by_recruiter, a.viewed_at " +
           "FROM applications a JOIN candidates c ON a.candidate_id = c.id " +
           "WHERE a.job_offer_id = :jobOfferId", nativeQuery = true)
    List<Object[]> findAllApplicationsWithCandidateByJobOfferId(@Param("jobOfferId") Long jobOfferId);
    
    List<Application> findByCandidateId(Long candidateId);
    
    @Query("SELECT a FROM Application a WHERE a.receivedAt BETWEEN :startDate AND :endDate")
    List<Application> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                    @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT a FROM Application a WHERE a.aiScore >= :minScore AND a.aiScore <= :maxScore")
    List<Application> findByScoreRange(@Param("minScore") Integer minScore, 
                                     @Param("maxScore") Integer maxScore);
    
    @Query("SELECT a FROM Application a WHERE a.responseSent = false AND a.status != 'PENDING'")
    List<Application> findPendingResponses();
    
    long countByStatus(Application.ApplicationStatus status);
    
    @Query("SELECT COUNT(a) FROM Application a WHERE a.receivedAt >= :date")
    long countTodaysApplications(@Param("date") LocalDateTime date);
    
    @Query("SELECT a FROM Application a " +
           "LEFT JOIN FETCH a.candidate " +
           "LEFT JOIN FETCH a.jobOffer " +
           "LEFT JOIN FETCH a.documents " +
           "WHERE a.id = :id")
    Optional<Application> findByIdWithDetails(@Param("id") Long id);
    
    @Query("SELECT a FROM Application a " +
           "LEFT JOIN FETCH a.candidate " +
           "WHERE a.archived IS NULL OR a.archived = false " +
           "ORDER BY a.receivedAt DESC")
    List<Application> findAllWithCandidates();
    
    @Query("SELECT a FROM Application a " +
           "LEFT JOIN FETCH a.candidate " +
           "WHERE a.status = :status AND (a.archived IS NULL OR a.archived = false) " +
           "ORDER BY a.receivedAt DESC")
    List<Application> findByStatusWithCandidates(@Param("status") Application.ApplicationStatus status);
    
    @Query("SELECT a FROM Application a " +
           "LEFT JOIN FETCH a.candidate " +
           "WHERE a.archived = true " +
           "ORDER BY a.archivedAt DESC")
    List<Application> findArchivedWithCandidates();
    
    @Query("SELECT a FROM Application a " +
           "LEFT JOIN FETCH a.candidate " +
           "WHERE a.jobOffer.id = :jobOfferId AND a.status = :status " +
           "ORDER BY a.receivedAt DESC")
    List<Application> findByJobOfferIdAndStatus(@Param("jobOfferId") Long jobOfferId, @Param("status") Application.ApplicationStatus status);
    
    @Query(value = "SELECT jo.id, jo.title, COUNT(a.id) as unread_count, MAX(a.received_at) as latest_date, " +
           "CASE WHEN COUNT(a.id) = 1 THEN MIN(a.id) ELSE NULL END as single_application_id " +
           "FROM applications a " +
           "JOIN job_offers jo ON a.job_offer_id = jo.id " +
           "WHERE (a.viewed_by_recruiter IS NULL OR a.viewed_by_recruiter = false) " +
           "AND (a.archived IS NULL OR a.archived = false) " +
           "GROUP BY jo.id, jo.title " +
           "HAVING COUNT(a.id) > 0 " +
           "ORDER BY latest_date DESC", nativeQuery = true)
    List<Object[]> findUnreadNotificationsByJobOffer();
    
    @Modifying
    @Query("UPDATE Application a SET a.viewedByRecruiter = true, a.viewedAt = CURRENT_TIMESTAMP " +
           "WHERE a.jobOffer.id = :jobOfferId AND (a.viewedByRecruiter IS NULL OR a.viewedByRecruiter = false)")
    void markJobOfferApplicationsAsViewed(@Param("jobOfferId") Long jobOfferId);
    
    @Modifying
    @Query("UPDATE Application a SET a.viewedByRecruiter = true, a.viewedAt = CURRENT_TIMESTAMP " +
           "WHERE a.viewedByRecruiter IS NULL OR a.viewedByRecruiter = false")
    void markAllApplicationsAsViewed();
}