package com.recruitment.candidatemanagement.service;

import com.recruitment.candidatemanagement.config.AppProperties;
import com.recruitment.candidatemanagement.entity.Application;
import com.recruitment.candidatemanagement.entity.ApplicationDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileUploadService {
    
    private final AppProperties appProperties;
    private final Tika tika = new Tika();
    
    public ApplicationDocument saveFile(MultipartFile file, Application application) {
        try {
            // Créer le dossier s'il n'existe pas
            Path uploadPath = Paths.get(appProperties.getFileStorage().getUploadDir());
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // Générer un nom unique
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            
            // Sauvegarder le fichier
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // Extraire le texte
            String extractedText = extractText(file);
            
            // Créer l'entité document
            ApplicationDocument document = new ApplicationDocument();
            document.setApplication(application);
            document.setFileName(fileName);
            document.setOriginalFileName(file.getOriginalFilename());
            document.setFilePath(filePath.toString());
            document.setContentType(file.getContentType());
            document.setFileSize(file.getSize());
            document.setDocumentType(determineDocumentType(file.getOriginalFilename()));
            document.setExtractedText(extractedText);
            
            log.info("Fichier sauvegardé: {} pour la candidature {}", 
                fileName, application.getId());
            
            return document;
            
        } catch (IOException e) {
            log.error("Erreur lors de la sauvegarde du fichier", e);
            throw new RuntimeException("Erreur lors de la sauvegarde du fichier", e);
        }
    }
    
    private String extractText(MultipartFile file) {
        try {
            return tika.parseToString(file.getInputStream());
        } catch (Exception e) {
            log.warn("Impossible d'extraire le texte du fichier: {}", file.getOriginalFilename());
            return "";
        }
    }
    
    private ApplicationDocument.DocumentType determineDocumentType(String filename) {
        String lowerName = filename.toLowerCase();
        
        if (lowerName.contains("cv") || lowerName.contains("resume")) {
            return ApplicationDocument.DocumentType.CV;
        } else if (lowerName.contains("lettre") || lowerName.contains("motivation") || 
                   lowerName.contains("cover")) {
            return ApplicationDocument.DocumentType.COVER_LETTER;
        } else if (lowerName.contains("portfolio")) {
            return ApplicationDocument.DocumentType.PORTFOLIO;
        }
        
        return ApplicationDocument.DocumentType.OTHER;
    }
    
    public void deleteFile(String filePath) {
        try {
            Files.deleteIfExists(Paths.get(filePath));
            log.info("Fichier supprimé: {}", filePath);
        } catch (IOException e) {
            log.error("Erreur lors de la suppression du fichier: {}", filePath, e);
        }
    }
}