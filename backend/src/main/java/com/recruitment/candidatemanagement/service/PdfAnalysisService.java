package com.recruitment.candidatemanagement.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class PdfAnalysisService {
    
    private static final Set<String> TECHNICAL_SKILLS = Set.of(
        "java", "python", "javascript", "typescript", "react", "angular", "vue", "spring", "nodejs",
        "docker", "kubernetes", "aws", "azure", "gcp", "mysql", "postgresql", "mongodb", "redis",
        "git", "jenkins", "maven", "gradle", "junit", "selenium", "html", "css", "sass", "bootstrap",
        "microservices", "rest", "api", "json", "xml", "agile", "scrum", "devops", "ci/cd"
    );
    
    private static final Set<String> SOFT_SKILLS = Set.of(
        "leadership", "communication", "teamwork", "problem solving", "analytical", "creative",
        "adaptable", "organized", "detail-oriented", "time management", "project management"
    );
    
    private static final Set<String> EDUCATION_KEYWORDS = Set.of(
        "master", "bachelor", "licence", "ingénieur", "doctorat", "phd", "bts", "dut", "iut",
        "université", "école", "formation", "diplôme", "certification"
    );

    public PdfAnalysisResult analyzePdf(String filePath, String jobRequirements) {
        try {
            String text = extractTextFromPdf(filePath);
            return analyzeText(text, jobRequirements);
        } catch (Exception e) {
            log.error("Erreur lors de l'analyse du PDF: {}", filePath, e);
            return new PdfAnalysisResult(0, new ArrayList<>(), 0, "", 0, false, 0);
        }
    }
    
    private String extractTextFromPdf(String filePath) throws IOException {
        try (PDDocument document = PDDocument.load(new File(filePath))) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document).toLowerCase();
        }
    }
    
    private PdfAnalysisResult analyzeText(String text, String jobRequirements) {
        // Analyse des compétences par rapport aux exigences du poste
        List<String> foundSkills = new ArrayList<>();
        int skillsScore = analyzeSkillsMatch(text, jobRequirements, foundSkills);
        
        // Analyse de l'expérience professionnelle pertinente
        int experienceYears = extractExperienceYears(text);
        boolean hasRelevantExperience = checkRelevantExperience(text, jobRequirements);
        
        // Analyse de la formation
        String education = extractEducation(text);
        int educationScore = calculateEducationScore(education);
        
        // Détection de contenu généré par IA
        boolean isAiGenerated = detectAiContent(text);
        int aiPenalty = isAiGenerated ? -2 : 0;
        
        int totalScore = skillsScore + (hasRelevantExperience ? 3 : 0) + educationScore + aiPenalty;
        
        return new PdfAnalysisResult(Math.max(totalScore, 0), foundSkills, experienceYears, 
                                   education, educationScore, isAiGenerated, 
                                   hasRelevantExperience ? 1 : 0);
    }
    
    private int analyzeSkillsMatch(String text, String jobRequirements, List<String> foundSkills) {
        int score = 0;
        String[] requiredSkills = extractRequiredSkills(jobRequirements);
        
        // Vérifier les compétences requises (priorité haute)
        for (String skill : requiredSkills) {
            if (text.toLowerCase().contains(skill.toLowerCase())) {
                foundSkills.add(skill);
                score += 3; // 3 points par compétence requise trouvée
            }
        }
        
        // Vérifier les compétences techniques générales
        for (String skill : TECHNICAL_SKILLS) {
            if (text.contains(skill.toLowerCase()) && !foundSkills.contains(skill)) {
                foundSkills.add(skill);
                score += 1; // 1 point par compétence technique supplémentaire
            }
        }
        
        return Math.min(score, 8); // Max 8 points pour les compétences
    }
    
    private String[] extractRequiredSkills(String jobRequirements) {
        if (jobRequirements == null) return new String[0];
        
        // Extraire les compétences mentionnées dans les exigences du poste
        List<String> required = new ArrayList<>();
        String lower = jobRequirements.toLowerCase();
        
        for (String skill : TECHNICAL_SKILLS) {
            if (lower.contains(skill)) {
                required.add(skill);
            }
        }
        
        return required.toArray(new String[0]);
    }
    
    private boolean checkRelevantExperience(String text, String jobRequirements) {
        if (jobRequirements == null) return false;
        
        // Vérifier si le candidat a une expérience dans le domaine requis
        String[] jobKeywords = {"développeur", "developer", "ingénieur", "engineer", 
                               "programmeur", "programmer", "analyste", "analyst"};
        
        for (String keyword : jobKeywords) {
            if (text.toLowerCase().contains(keyword) && 
                jobRequirements.toLowerCase().contains(keyword)) {
                return true;
            }
        }
        
        return false;
    }
    
    private boolean detectAiContent(String text) {
        // Indicateurs de contenu généré par IA
        String[] aiIndicators = {
            "en tant qu'intelligence artificielle", "je suis un modèle de langage",
            "generated by ai", "créé par ia", "assistant virtuel",
            "je ne peux pas", "je ne suis pas capable", "en tant qu'ia"
        };
        
        String lower = text.toLowerCase();
        for (String indicator : aiIndicators) {
            if (lower.contains(indicator)) {
                return true;
            }
        }
        
        // Vérifier les patterns suspects (phrases trop parfaites/répétitives)
        String[] suspiciousPatterns = {
            "fort de mes compétences", "grâce à mon expertise", "mes compétences me permettent",
            "je serais ravi de contribuer", "n'hésitez pas à me contacter"
        };
        
        int suspiciousCount = 0;
        for (String pattern : suspiciousPatterns) {
            if (lower.contains(pattern)) {
                suspiciousCount++;
            }
        }
        
        return suspiciousCount >= 2; // Si 2+ phrases suspectes
    }
    
    private int extractExperienceYears(String text) {
        // Recherche de patterns d'expérience
        Pattern pattern = Pattern.compile("(\\d+)\\s*(ans?|years?)\\s*(d'expérience|experience|exp)");
        Matcher matcher = pattern.matcher(text);
        
        int maxYears = 0;
        while (matcher.find()) {
            try {
                int years = Integer.parseInt(matcher.group(1));
                maxYears = Math.max(maxYears, years);
            } catch (NumberFormatException e) {
                // Ignorer les erreurs de parsing
            }
        }
        
        // Si pas trouvé, estimer par le nombre d'entreprises/postes
        if (maxYears == 0) {
            long jobCount = Arrays.stream(text.split("\\n"))
                .filter(line -> line.contains("2020") || line.contains("2021") || 
                               line.contains("2022") || line.contains("2023") || line.contains("2024"))
                .count();
            maxYears = (int) Math.min(jobCount * 2, 15); // Estimation
        }
        
        return maxYears;
    }
    
    private String extractEducation(String text) {
        StringBuilder education = new StringBuilder();
        
        for (String keyword : EDUCATION_KEYWORDS) {
            if (text.contains(keyword)) {
                education.append(keyword).append(" ");
            }
        }
        
        return education.toString().trim();
    }
    
    private int calculateEducationScore(String education) {
        if (education.contains("doctorat") || education.contains("phd")) return 8;
        if (education.contains("master") || education.contains("ingénieur")) return 6;
        if (education.contains("bachelor") || education.contains("licence")) return 4;
        if (education.contains("bts") || education.contains("dut")) return 3;
        return 1; // Formation de base
    }
    
    public static class PdfAnalysisResult {
        public final int skillsScore;
        public final List<String> foundSkills;
        public final int experienceYears;
        public final String education;
        public final int educationScore;
        public final boolean isAiGenerated;
        public final int relevanceScore;
        
        public PdfAnalysisResult(int skillsScore, List<String> foundSkills, int experienceYears, 
                               String education, int educationScore, boolean isAiGenerated, int relevanceScore) {
            this.skillsScore = skillsScore;
            this.foundSkills = foundSkills;
            this.experienceYears = experienceYears;
            this.education = education;
            this.educationScore = educationScore;
            this.isAiGenerated = isAiGenerated;
            this.relevanceScore = relevanceScore;
        }
    }
}