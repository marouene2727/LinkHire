package com.recruitment.candidatemanagement.service;

import com.recruitment.candidatemanagement.entity.Application;
import com.recruitment.candidatemanagement.entity.ApplicationDocument;
import com.recruitment.candidatemanagement.repository.ApplicationRepository;
import com.recruitment.candidatemanagement.repository.ApplicationDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.recruitment.candidatemanagement.config.AppProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIAnalysisService {
    
    private final ApplicationRepository applicationRepository;
    private final ApplicationDocumentRepository documentRepository;
    private final AppProperties appProperties;
    private final PdfAnalysisService pdfAnalysisService;
    private final ProfileScrapingService profileScrapingService;
    
    @Value("${app.file-storage.upload-dir}")
    private String uploadDir;
    
    @Async
    @Transactional
    public CompletableFuture<Void> analyzeApplicationAsync(Long applicationId) {
        log.info("Début de l'analyse IA pour la candidature ID: {}", applicationId);
        
        try {
            Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée"));
            
            // Simulation de l'analyse IA (à remplacer par une vraie IA)
            AnalysisResult result = appProperties.getAi().getScoring().isMockMode() ? 
                performMockAnalysis(application) : 
                performRealAnalysis(application);
            
            // Mise à jour de la candidature avec les résultats
            application.setAiScore(result.score);
            application.setAiAnalysis(result.analysis);
            application.setStatus(determineStatus(result.score));
            
            applicationRepository.save(application);
            
            log.info("Analyse IA terminée pour la candidature ID: {} - Score: {}/20", 
                    applicationId, result.score);
            
        } catch (Exception e) {
            log.error("Erreur lors de l'analyse IA pour la candidature ID: {}", applicationId, e);
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    private AnalysisResult performMockAnalysis(Application application) {
        // Simulation d'un délai d'analyse
        try {
            Thread.sleep(2000); // 2 secondes
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        Random random = new Random();
        int score = random.nextInt(21); // Score entre 0 et 20
        
        String analysis = generateMockAnalysis(score, application);
        
        return new AnalysisResult(score, analysis);
    }
    
    private AnalysisResult performRealAnalysis(Application application) {
        log.info("Début de l'analyse IA réelle pour la candidature ID: {}", application.getId());
        
        int totalScore = 0;
        StringBuilder analysis = new StringBuilder();
        analysis.append("Analyse IA complète de la candidature:\n\n");
        
        // 1. Analyse des documents PDF (max 8 points)
        int pdfScore = analyzePdfDocuments(application, analysis);
        totalScore += pdfScore;
        
        // 2. Analyse du profil LinkedIn (max 6 points)
        int linkedinScore = analyzeLinkedInProfile(application, analysis);
        totalScore += linkedinScore;
        
        // 3. Analyse du profil GitHub (max 4 points)
        int githubScore = analyzeGitHubProfile(application, analysis);
        totalScore += githubScore;
        
        // 4. Analyse des informations candidat (max 2 points)
        int candidateScore = analyzeCandidateInfo(application, analysis);
        totalScore += candidateScore;
        
        // Normaliser le score sur 20
        int finalScore = Math.min(totalScore, 20);
        
        analysis.append("\n📊 SCORE FINAL: ").append(finalScore).append("/20\n");
        analysis.append("\nDétail des scores:\n");
        analysis.append("• Documents PDF: ").append(pdfScore).append("/8\n");
        analysis.append("• Profil LinkedIn: ").append(linkedinScore).append("/6\n");
        analysis.append("• Profil GitHub: ").append(githubScore).append("/4\n");
        analysis.append("• Informations candidat: ").append(candidateScore).append("/2\n");
        
        return new AnalysisResult(finalScore, analysis.toString());
    }
    
    private int analyzePdfDocuments(Application application, StringBuilder analysis) {
        List<ApplicationDocument> documents = documentRepository.findByApplicationId(application.getId());
        int totalPdfScore = 0;
        String jobRequirements = application.getJobOffer().getRequiredSkills(); // Exigences du poste
        
        analysis.append("📄 ANALYSE DES DOCUMENTS:\n");
        
        for (ApplicationDocument doc : documents) {
            if (doc.getFileName().toLowerCase().endsWith(".pdf")) {
                String filePath = uploadDir + "/" + doc.getFileName();
                PdfAnalysisService.PdfAnalysisResult result = pdfAnalysisService.analyzePdf(filePath, jobRequirements);
                
                totalPdfScore += result.skillsScore;
                totalPdfScore += result.relevanceScore;
                
                analysis.append("• ").append(doc.getDocumentType()).append(": ");
                analysis.append(result.foundSkills.size()).append(" compétences, ");
                analysis.append(result.experienceYears).append(" ans expérience");
                
                if (result.isAiGenerated) {
                    analysis.append(" ⚠️ Contenu suspect (IA détectée)");
                }
                
                if (result.relevanceScore > 0) {
                    analysis.append(" ✅ Expérience pertinente");
                }
                
                analysis.append("\n");
            }
        }
        
        return Math.min(totalPdfScore, 8); // Max 8 points pour les PDF
    }
    
    private int analyzeLinkedInProfile(Application application, StringBuilder analysis) {
        String linkedinUrl = application.getCandidate().getLinkedinProfile();
        
        analysis.append("\n💼 ANALYSE PROFIL LINKEDIN:\n");
        
        if (linkedinUrl == null || linkedinUrl.isEmpty()) {
            analysis.append("• Aucun profil LinkedIn fourni\n");
            return 0;
        }
        
        ProfileScrapingService.ProfileAnalysisResult result = 
            profileScrapingService.analyzeLinkedInProfile(linkedinUrl);
        
        analysis.append("• Profil trouvé: ").append(result.title).append("\n");
        analysis.append("• Compétences LinkedIn: ").append(result.skills.size()).append("\n");
        
        return Math.min(result.score, 6); // Max 6 points pour LinkedIn
    }
    
    private int analyzeGitHubProfile(Application application, StringBuilder analysis) {
        // Récupérer l'URL GitHub depuis les informations du candidat
        String githubUrl = extractGitHubUrl(application.getCandidate().getSkills());
        
        analysis.append("\n💻 ANALYSE PROFIL GITHUB:\n");
        
        if (githubUrl == null || githubUrl.isEmpty()) {
            analysis.append("• Aucun profil GitHub fourni\n");
            return 0;
        }
        
        ProfileScrapingService.GitHubAnalysisResult result = 
            profileScrapingService.analyzeGitHubProfile(githubUrl);
        
        analysis.append("• Repositories publics: ").append(result.publicRepos).append("\n");
        analysis.append("• Langages utilisés: ").append(String.join(", ", result.languages)).append("\n");
        analysis.append("• Total stars: ").append(result.totalStars).append("\n");
        
        return Math.min(result.score, 4); // Max 4 points pour GitHub
    }
    
    private int analyzeCandidateInfo(Application application, StringBuilder analysis) {
        analysis.append("\n👤 ANALYSE MESSAGE CANDIDAT:\n");
        
        int score = 0;
        String message = application.getEmailBody(); // Message du candidat
        
        if (message != null && !message.trim().isEmpty()) {
            // Vérifier la sincérité et détecter l'IA
            boolean isAiGenerated = detectAiInMessage(message);
            boolean isPersonalized = checkPersonalization(message, application.getJobOffer().getTitle());
            
            if (!isAiGenerated) {
                score += 1;
                analysis.append("• Message authentique détecté\n");
            } else {
                analysis.append("• ⚠️ Message suspect (possiblement généré par IA)\n");
            }
            
            if (isPersonalized) {
                score += 1;
                analysis.append("• Message personnalisé pour le poste\n");
            } else {
                analysis.append("• Message générique détecté\n");
            }
        } else {
            analysis.append("• Aucun message fourni\n");
        }
        
        return score;
    }
    
    private boolean detectAiInMessage(String message) {
        String[] aiIndicators = {
            "en tant qu'intelligence artificielle", "je suis ravi de postuler",
            "grâce à mon expertise approfondie", "mes compétences me permettent",
            "je serais honoré de contribuer", "n'hésitez pas à me contacter",
            "je reste à votre disposition", "dans l'attente de votre retour"
        };
        
        String lower = message.toLowerCase();
        int suspiciousCount = 0;
        
        for (String indicator : aiIndicators) {
            if (lower.contains(indicator)) {
                suspiciousCount++;
            }
        }
        
        // Vérifier la longueur et la structure (IA tend à être verbose)
        if (message.length() > 500 && suspiciousCount >= 2) {
            return true;
        }
        
        return suspiciousCount >= 3;
    }
    
    private boolean checkPersonalization(String message, String jobTitle) {
        if (jobTitle == null) return false;
        
        String lower = message.toLowerCase();
        String jobLower = jobTitle.toLowerCase();
        
        // Vérifier si le titre du poste est mentionné
        return lower.contains(jobLower) || 
               lower.contains("ce poste") || 
               lower.contains("cette offre") ||
               lower.contains("votre entreprise");
    }
    
    private String extractGitHubUrl(String skills) {
        if (skills == null) return null;
        
        // Rechercher une URL GitHub dans les compétences
        if (skills.toLowerCase().contains("github.com")) {
            String[] parts = skills.split("\\s+");
            for (String part : parts) {
                if (part.contains("github.com")) {
                    return part;
                }
            }
        }
        
        return null;
    }
    
    private String generateMockAnalysis(int score, Application application) {
        StringBuilder analysis = new StringBuilder();
        analysis.append("Analyse automatique de la candidature (mode simulation):\\n\\n");
        
        if (score >= 15) {
            analysis.append("✅ CANDIDATURE VALIDÉE\\n");
            analysis.append("- Profil correspondant aux critères recherchés\\n");
            analysis.append("- Expérience pertinente détectée\\n");
            analysis.append("- Compétences techniques alignées\\n");
        } else if (score >= 10) {
            analysis.append("⚠️ CANDIDATURE AMBIGUË\\n");
            analysis.append("- Profil partiellement correspondant\\n");
            analysis.append("- Certaines compétences manquantes\\n");
            analysis.append("- Nécessite une évaluation manuelle\\n");
        } else {
            analysis.append("❌ CANDIDATURE NON RETENUE\\n");
            analysis.append("- Profil ne correspondant pas aux critères\\n");
            analysis.append("- Expérience insuffisante\\n");
            analysis.append("- Compétences non alignées\\n");
        }
        
        analysis.append("\\n⚠️ Mode simulation activé - Activez le mode réel dans la configuration\\n");
        analysis.append("\\nScore détaillé (simulé):\\n");
        analysis.append("- Expérience: ").append(Math.min(score + 2, 20)).append("/20\\n");
        analysis.append("- Compétences: ").append(Math.max(score - 2, 0)).append("/20\\n");
        analysis.append("- Motivation: ").append(score).append("/20\\n");
        
        return analysis.toString();
    }
    
    private Application.ApplicationStatus determineStatus(int score) {
        if (score >= 15) {
            return Application.ApplicationStatus.VALIDATED;
        } else if (score >= 10) {
            return Application.ApplicationStatus.AMBIGUOUS;
        } else {
            return Application.ApplicationStatus.REJECTED;
        }
    }
    
    private static class AnalysisResult {
        final int score;
        final String analysis;
        
        AnalysisResult(int score, String analysis) {
            this.score = score;
            this.analysis = analysis;
        }
    }
}