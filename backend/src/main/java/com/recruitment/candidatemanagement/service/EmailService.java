package com.recruitment.candidatemanagement.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    public void sendValidationEmail(String to, String candidateName, String jobTitle, 
                                  String company, String message, String interviewDate) {
        try {
            SimpleMailMessage email = new SimpleMailMessage();
            email.setTo(to);
            email.setSubject("Candidature acceptée - " + jobTitle);
            email.setText(message);
            
            mailSender.send(email);
            log.info("Email de validation envoyé à: {}", to);
        } catch (Exception e) {
            log.error("Erreur envoi email validation: {}", e.getMessage());
        }
    }
    
    public void sendRejectionEmail(String to, String candidateName, String jobTitle, String message) {
        try {
            SimpleMailMessage email = new SimpleMailMessage();
            email.setTo(to);
            email.setSubject("Candidature - " + jobTitle);
            email.setText(message);
            
            mailSender.send(email);
            log.info("Email de rejet envoyé à: {}", to);
        } catch (Exception e) {
            log.error("Erreur envoi email rejet: {}", e.getMessage());
        }
    }
    
    public void sendContactEmail(String to, String candidateName, String subject, String message) {
        try {
            SimpleMailMessage email = new SimpleMailMessage();
            email.setTo(to);
            email.setSubject(subject);
            email.setText(message);
            
            mailSender.send(email);
            log.info("Email de contact envoyé à: {}", to);
        } catch (Exception e) {
            log.error("Erreur envoi email contact: {}", e.getMessage());
        }
    }
}