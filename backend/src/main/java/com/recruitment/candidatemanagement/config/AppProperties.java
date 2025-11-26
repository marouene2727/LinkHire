package com.recruitment.candidatemanagement.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    
    private FileStorage fileStorage = new FileStorage();
    private Email email = new Email();
    private Ai ai = new Ai();
    private Jwt jwt = new Jwt();
    
    @Data
    public static class FileStorage {
        private String uploadDir = "./uploads";
    }
    
    @Data
    public static class Email {
        private long checkInterval = 300000; // 5 minutes
    }
    
    @Data
    public static class Ai {
        private Scoring scoring = new Scoring();
        
        @Data
        public static class Scoring {
            private boolean enabled = true;
            private boolean mockMode = true;
        }
    }
    
    @Data
    public static class Jwt {
        private String secret = "mySecretKey123456789012345678901234567890";
        private long expiration = 86400000; // 24 heures
    }
}