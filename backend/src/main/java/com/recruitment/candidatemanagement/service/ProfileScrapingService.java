package com.recruitment.candidatemanagement.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@Slf4j
public class ProfileScrapingService {
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    public ProfileAnalysisResult analyzeLinkedInProfile(String linkedinUrl) {
        try {
            if (linkedinUrl == null || linkedinUrl.isEmpty()) {
                return new ProfileAnalysisResult(0, "", new ArrayList<>());
            }
            
            // Note: LinkedIn bloque le scraping direct, on simule l'analyse
            return simulateLinkedInAnalysis(linkedinUrl);
            
        } catch (Exception e) {
            log.error("Erreur lors de l'analyse du profil LinkedIn: {}", linkedinUrl, e);
            return new ProfileAnalysisResult(0, "", new ArrayList<>());
        }
    }
    
    public GitHubAnalysisResult analyzeGitHubProfile(String githubUrl) {
        try {
            if (githubUrl == null || githubUrl.isEmpty()) {
                return new GitHubAnalysisResult(0, new ArrayList<>(), 0, 0);
            }
            
            String username = extractGitHubUsername(githubUrl);
            if (username != null) {
                return analyzeGitHubAPI(username);
            }
            
            return new GitHubAnalysisResult(0, new ArrayList<>(), 0, 0);
            
        } catch (Exception e) {
            log.error("Erreur lors de l'analyse du profil GitHub: {}", githubUrl, e);
            return new GitHubAnalysisResult(0, new ArrayList<>(), 0, 0);
        }
    }
    
    private ProfileAnalysisResult simulateLinkedInAnalysis(String linkedinUrl) {
        // Simulation basée sur l'URL et des patterns communs
        List<String> skills = Arrays.asList("Communication", "Leadership", "Project Management");
        String title = "Software Developer"; // Titre simulé
        int score = 5; // Score de base pour avoir un profil LinkedIn
        
        return new ProfileAnalysisResult(score, title, skills);
    }
    
    private String extractGitHubUsername(String githubUrl) {
        try {
            // Extraire le nom d'utilisateur de l'URL GitHub
            String[] parts = githubUrl.replace("https://", "").replace("http://", "")
                                    .replace("github.com/", "").split("/");
            return parts.length > 0 ? parts[0] : null;
        } catch (Exception e) {
            return null;
        }
    }
    
    @SuppressWarnings("unchecked")
    private GitHubAnalysisResult analyzeGitHubAPI(String username) {
        try {
            // Utiliser l'API publique GitHub
            String userUrl = "https://api.github.com/users/" + username;
            String reposUrl = "https://api.github.com/users/" + username + "/repos";
            
            // Récupérer les informations utilisateur
            Map<String, Object> userInfo = restTemplate.getForObject(userUrl, Map.class);
            List<Map<String, Object>> repos = restTemplate.getForObject(reposUrl, List.class);
            
            if (userInfo == null || repos == null) {
                return new GitHubAnalysisResult(0, new ArrayList<>(), 0, 0);
            }
            
            // Analyser les repositories
            Set<String> languages = new HashSet<>();
            int totalStars = 0;
            int publicRepos = (Integer) userInfo.getOrDefault("public_repos", 0);
            
            for (Map<String, Object> repo : repos) {
                String language = (String) repo.get("language");
                if (language != null) {
                    languages.add(language);
                }
                
                Integer stars = (Integer) repo.getOrDefault("stargazers_count", 0);
                totalStars += stars;
            }
            
            // Calculer le score GitHub
            int score = calculateGitHubScore(publicRepos, totalStars, languages.size());
            
            return new GitHubAnalysisResult(score, new ArrayList<>(languages), publicRepos, totalStars);
            
        } catch (Exception e) {
            log.error("Erreur lors de l'appel à l'API GitHub pour: {}", username, e);
            return new GitHubAnalysisResult(0, new ArrayList<>(), 0, 0);
        }
    }
    
    private int calculateGitHubScore(int publicRepos, int totalStars, int languageCount) {
        int score = 0;
        
        // Points pour les repositories publics
        score += Math.min(publicRepos, 10); // Max 10 points
        
        // Points pour les stars
        score += Math.min(totalStars / 5, 5); // Max 5 points (1 point par 5 stars)
        
        // Points pour la diversité des langages
        score += Math.min(languageCount, 5); // Max 5 points
        
        return score;
    }
    
    public static class ProfileAnalysisResult {
        public final int score;
        public final String title;
        public final List<String> skills;
        
        public ProfileAnalysisResult(int score, String title, List<String> skills) {
            this.score = score;
            this.title = title;
            this.skills = skills;
        }
    }
    
    public static class GitHubAnalysisResult {
        public final int score;
        public final List<String> languages;
        public final int publicRepos;
        public final int totalStars;
        
        public GitHubAnalysisResult(int score, List<String> languages, int publicRepos, int totalStars) {
            this.score = score;
            this.languages = languages;
            this.publicRepos = publicRepos;
            this.totalStars = totalStars;
        }
    }
}