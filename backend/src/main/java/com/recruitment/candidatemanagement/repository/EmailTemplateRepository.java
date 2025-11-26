package com.recruitment.candidatemanagement.repository;

import com.recruitment.candidatemanagement.entity.EmailTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Long> {
    
    List<EmailTemplate> findByTemplateType(EmailTemplate.TemplateType templateType);
    
    List<EmailTemplate> findByIsActiveTrue();
    
    Optional<EmailTemplate> findByTemplateTypeAndIsActiveTrue(EmailTemplate.TemplateType templateType);
    
    List<EmailTemplate> findByNameContainingIgnoreCase(String name);
}