package tn.pi.back.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HomeController {
    
    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "API Gestion Profil - Application Médicale");
        response.put("version", "1.0.0");
        response.put("endpoints", Map.of(
            "swagger-ui", "/swagger-ui.html",
            "api-docs", "/v3/api-docs",
            "auth", "/api/auth",
            "profile", "/api/profile",
            "admin", "/api/admin"
        ));
        return ResponseEntity.ok(response);
    }
}

