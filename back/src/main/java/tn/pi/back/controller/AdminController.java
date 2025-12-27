package tn.pi.back.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.pi.back.dto.ChangePasswordDTO;
import tn.pi.back.dto.UpdateProfileDTO;
import tn.pi.back.dto.UserResponseDTO;
import tn.pi.back.service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Administration", description = "API pour la gestion des utilisateurs (ADMIN uniquement)")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {
    
    private final UserService userService;
    
    public AdminController(UserService userService) {
        this.userService = userService;
    }
    
    @Operation(
            summary = "Liste tous les utilisateurs",
            description = "Récupère la liste de tous les utilisateurs. Réservé aux administrateurs."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès"),
            @ApiResponse(responseCode = "403", description = "Accès refusé - Admin uniquement")
    })
    @GetMapping("/users")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<UserResponseDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    
    @Operation(
            summary = "Récupérer un utilisateur par ID",
            description = "Récupère les informations d'un utilisateur spécifique par son ID. Réservé aux administrateurs."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Utilisateur trouvé",
                    content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé"),
            @ApiResponse(responseCode = "403", description = "Accès refusé - Admin uniquement")
    })
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long userId) {
        UserResponseDTO user = userService.getProfile(userId);
        return ResponseEntity.ok(user);
    }
    
    @Operation(
            summary = "Modifier un utilisateur",
            description = "Met à jour le profil d'un utilisateur spécifique. Réservé aux administrateurs."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Utilisateur mis à jour avec succès"),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé"),
            @ApiResponse(responseCode = "403", description = "Accès refusé - Admin uniquement")
    })
    @PutMapping("/users/{userId}")
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateProfileDTO updateDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String adminEmail = authentication.getName();
        
        UserResponseDTO updatedUser = userService.updateProfile(userId, updateDTO, adminEmail);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Utilisateur mis à jour avec succès");
        response.put("user", updatedUser);
        return ResponseEntity.ok(response);
    }
    
    @Operation(
            summary = "Changer le mot de passe d'un utilisateur",
            description = "Change le mot de passe d'un utilisateur spécifique. Réservé aux administrateurs."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mot de passe modifié avec succès"),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé"),
            @ApiResponse(responseCode = "403", description = "Accès refusé - Admin uniquement")
    })
    @PutMapping("/users/{userId}/password")
    public ResponseEntity<Map<String, String>> changeUserPassword(
            @PathVariable Long userId,
            @Valid @RequestBody ChangePasswordDTO changePasswordDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String adminEmail = authentication.getName();
        
        userService.changePassword(userId, changePasswordDTO, adminEmail);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Mot de passe modifié avec succès");
        return ResponseEntity.ok(response);
    }
    
    @Operation(
            summary = "Upload photo pour un utilisateur",
            description = "Upload une photo de profil pour un utilisateur spécifique. Réservé aux administrateurs."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Photo uploadée avec succès"),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé"),
            @ApiResponse(responseCode = "403", description = "Accès refusé - Admin uniquement")
    })
    @PostMapping(value = "/users/{userId}/photo", consumes = "multipart/form-data")
    public ResponseEntity<Map<String, Object>> uploadUserPhoto(
            @PathVariable Long userId,
            @RequestParam("file") MultipartFile file) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String adminEmail = authentication.getName();
        
        UserResponseDTO updatedUser = userService.uploadPhoto(userId, file, adminEmail);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Photo uploadée avec succès");
        response.put("user", updatedUser);
        return ResponseEntity.ok(response);
    }
}

