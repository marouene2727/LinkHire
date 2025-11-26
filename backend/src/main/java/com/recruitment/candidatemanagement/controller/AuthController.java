package com.recruitment.candidatemanagement.controller;

import com.recruitment.candidatemanagement.entity.User;
import com.recruitment.candidatemanagement.repository.UserRepository;
import com.recruitment.candidatemanagement.security.JwtService;
import com.recruitment.candidatemanagement.service.EmailVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {
    
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;
    
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.get("username"), 
                    request.get("password")
                )
            );
            
            UserDetails userDetails = userDetailsService.loadUserByUsername(request.get("username"));
            String token = jwtService.generateToken(userDetails);
            User user = userRepository.findByUsername(request.get("username")).orElseThrow();
            
            return ResponseEntity.ok(Map.of(
                "token", token,
                "user", Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "email", user.getEmail(),
                    "firstName", user.getFirstName(),
                    "lastName", user.getLastName(),
                    "role", user.getRole()
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Identifiants invalides"));
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Nom d'utilisateur déjà utilisé"));
        }
        
        if (userRepository.existsByEmail(user.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email déjà utilisé"));
        }
        
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(User.Role.RECRUITER);
        
        User savedUser = userRepository.save(user);
        emailVerificationService.sendVerificationEmail(savedUser);
        
        return ResponseEntity.ok(Map.of(
            "message", "Compte créé avec succès. Un email de vérification a été envoyé."
        ));
    }
    
    @GetMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(@RequestParam String token) {
        String result = emailVerificationService.validateVerificationToken(token);
        
        switch (result) {
            case "valid":
                return ResponseEntity.ok(Map.of(
                    "message", "Email vérifié avec succès. Vous pouvez maintenant vous connecter."
                ));
            case "expired":
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Token de vérification expiré. Veuillez vous réinscrire."
                ));
            default:
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Token de vérification invalide."
                ));
        }
    }
    

}