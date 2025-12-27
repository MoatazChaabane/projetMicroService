package tn.pi.back.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import tn.pi.back.dto.LoginDTO;
import tn.pi.back.dto.RegisterDTO;
import tn.pi.back.dto.UserResponseDTO;
import tn.pi.back.service.UserService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentification", description = "API pour l'inscription, la connexion et la déconnexion")
public class AuthController {
    
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    
    @Operation(
            summary = "Inscription d'un nouvel utilisateur",
            description = "Crée un nouveau compte utilisateur avec les informations fournies. Les rôles disponibles sont PATIENT, DOCTOR et ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Inscription réussie",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400", description = "Erreur de validation"),
            @ApiResponse(responseCode = "409", description = "Email déjà utilisé")
    })
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterDTO registerDTO) {
        UserResponseDTO user = userService.register(registerDTO);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Inscription réussie");
        response.put("user", user);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @Operation(
            summary = "Connexion d'un utilisateur",
            description = "Authentifie un utilisateur avec son email et mot de passe. Crée une session et retourne un cookie JSESSIONID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Connexion réussie",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "401", description = "Email ou mot de passe incorrect")
    })
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginDTO loginDTO) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginDTO.getEmail(),
                    loginDTO.getPassword()
                )
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            UserResponseDTO user = userService.getProfileByEmail(authentication.getName());
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Connexion réussie");
            response.put("user", user);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Email ou mot de passe incorrect");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
    
    @Operation(
            summary = "Déconnexion",
            description = "Déconnecte l'utilisateur actuel et invalide la session."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Déconnexion réussie")
    })
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        // La logique de déconnexion est gérée par Spring Security
        Map<String, String> response = new HashMap<>();
        response.put("message", "Déconnexion réussie");
        return ResponseEntity.ok(response);
    }
    
    @Operation(
            summary = "Récupérer l'utilisateur actuel",
            description = "Retourne les informations de l'utilisateur actuellement connecté."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Utilisateur trouvé",
                    content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            UserResponseDTO user = userService.getProfileByEmail(authentication.getName());
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}

