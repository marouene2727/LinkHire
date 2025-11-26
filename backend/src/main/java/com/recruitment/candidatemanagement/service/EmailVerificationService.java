package com.recruitment.candidatemanagement.service;

import com.recruitment.candidatemanagement.entity.User;
import com.recruitment.candidatemanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {
    
    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    
    public void sendVerificationEmail(User user) {
        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        user.setTokenExpiry(LocalDateTime.now().plusHours(24));
        userRepository.save(user);
        
        String verificationUrl = "http://localhost:4200/verify-email?token=" + token;
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Vérification de votre compte - Système de Recrutement");
        message.setText(buildEmailContent(user.getFirstName(), verificationUrl));
        
        mailSender.send(message);
    }
    
    public String validateVerificationToken(String token) {
        User user = userRepository.findByVerificationToken(token).orElse(null);
        
        if (user == null) {
            return "invalid";
        }
        
        if (user.getTokenExpiry().isBefore(LocalDateTime.now())) {
            return "expired";
        }
        
        user.setEmailVerified(true);
        user.setIsActive(true);
        user.setVerificationToken(null);
        user.setTokenExpiry(null);
        userRepository.save(user);
        
        return "valid";
    }
    
    private String buildEmailContent(String firstName, String verificationUrl) {
        return String.format("""
            Bonjour %s,
            
            Bienvenue sur notre plateforme de gestion de candidatures !
            
            Pour finaliser votre inscription et activer votre compte, veuillez cliquer sur le lien ci-dessous :
            
            %s
            
            ⚠️ Ce lien est valide pendant 24 heures uniquement.
            
            Une fois votre email vérifié, vous pourrez :
            • Accéder à votre tableau de bord
            • Gérer vos offres d'emploi
            • Analyser les candidatures reçues
            
            Si vous n'avez pas créé ce compte, vous pouvez ignorer cet email en toute sécurité.
            
            Cordialement,
            L'équipe de recrutement
            
            ---
            Ceci est un email automatique, merci de ne pas y répondre.
            """, firstName, verificationUrl);
    }
}