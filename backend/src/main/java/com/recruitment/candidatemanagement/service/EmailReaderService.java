package com.recruitment.candidatemanagement.service;

import com.recruitment.candidatemanagement.entity.JobOffer;
import com.recruitment.candidatemanagement.repository.JobOfferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.mail.*;
import java.util.List;
import java.util.Properties;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailReaderService {
    
    private final ApplicationService applicationService;
    private final JobOfferRepository jobOfferRepository;
    
    @Scheduled(fixedDelayString = "${app.email.check-interval}")
    public void checkEmails() {
        log.info("Vérification des nouveaux emails...");
        
        try {
            // Configuration IMAP (à adapter selon le fournisseur)
            Properties props = new Properties();
            props.setProperty("mail.store.protocol", "imaps");
            props.setProperty("mail.imaps.host", "imap.gmail.com");
            props.setProperty("mail.imaps.port", "993");
            props.setProperty("mail.imaps.ssl.enable", "true");
            
            Session session = Session.getInstance(props);
            Store store = session.getStore("imaps");
            
            // Connexion (à configurer avec les vraies credentials)
            // store.connect("your-email@gmail.com", "your-password");
            
            // Pour l'instant, simulation
            simulateEmailProcessing();
            
        } catch (Exception e) {
            log.error("Erreur lors de la lecture des emails", e);
        }
    }
    
    private void simulateEmailProcessing() {
        // Simulation de traitement d'emails pour les tests
        List<JobOffer> activeOffers = jobOfferRepository.findByStatusOrderByCreatedAtDesc(
            JobOffer.JobStatus.PUBLISHED);
        
        if (!activeOffers.isEmpty()) {
            log.info("Simulation: {} offres actives trouvées", activeOffers.size());
            
            // Ici on pourrait simuler la réception d'un email
            // et créer une candidature automatiquement
        }
    }
    
    private void processEmail(Message message, JobOffer jobOffer) {
        try {
            String subject = message.getSubject();
            String from = message.getFrom()[0].toString();
            String content = getTextContent(message);
            
            // Extraire l'email de l'expéditeur
            String candidateEmail = extractEmail(from);
            
            // Créer la candidature
            applicationService.processNewApplication(candidateEmail, subject, content, jobOffer);
            
            log.info("Email traité: {} -> candidature créée", candidateEmail);
            
        } catch (Exception e) {
            log.error("Erreur lors du traitement de l'email", e);
        }
    }
    
    private String getTextContent(Message message) throws Exception {
        if (message.isMimeType("text/plain")) {
            return (String) message.getContent();
        } else if (message.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) message.getContent();
            return getTextFromMultipart(multipart);
        }
        return "";
    }
    
    private String getTextFromMultipart(Multipart multipart) throws Exception {
        StringBuilder result = new StringBuilder();
        int count = multipart.getCount();
        
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = multipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result.append(bodyPart.getContent().toString());
            }
        }
        
        return result.toString();
    }
    
    private String extractEmail(String from) {
        // Extraire l'email de "Name <email@domain.com>"
        if (from.contains("<") && from.contains(">")) {
            int start = from.indexOf("<") + 1;
            int end = from.indexOf(">");
            return from.substring(start, end);
        }
        return from;
    }
}