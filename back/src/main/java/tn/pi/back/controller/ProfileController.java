package tn.pi.back.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@Tag(name = "Gestion du Profil", description = "API pour la gestion du profil utilisateur")
@SecurityRequirement(name = "bearerAuth")
public class ProfileController {
    
    private final UserService userService;
    
    @Operation(
            summary = "Récupérer le profil",
            description = "Récupère le profil de l'utilisateur actuellement connecté."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profil récupéré avec succès",
                    content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    @GetMapping
    public ResponseEntity<UserResponseDTO> getProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        UserResponseDTO user = userService.getProfileByEmail(email);
        return ResponseEntity.ok(user);
    }
    
    @Operation(
            summary = "Modifier le profil",
            description = "Met à jour les informations du profil de l'utilisateur connecté."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profil mis à jour avec succès"),
            @ApiResponse(responseCode = "400", description = "Erreur de validation"),
            @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    @PutMapping
    public ResponseEntity<Map<String, Object>> updateProfile(@Valid @RequestBody UpdateProfileDTO updateDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        UserResponseDTO currentUser = userService.getProfileByEmail(email);
        
        UserResponseDTO updatedUser = userService.updateProfile(currentUser.getId(), updateDTO, email);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Profil mis à jour avec succès");
        response.put("user", updatedUser);
        return ResponseEntity.ok(response);
    }
    
    @Operation(
            summary = "Changer le mot de passe",
            description = "Change le mot de passe de l'utilisateur connecté. Nécessite le mot de passe actuel."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mot de passe modifié avec succès"),
            @ApiResponse(responseCode = "400", description = "Mot de passe actuel incorrect ou erreur de validation"),
            @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    @PutMapping("/password")
    public ResponseEntity<Map<String, String>> changePassword(@Valid @RequestBody ChangePasswordDTO changePasswordDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        UserResponseDTO currentUser = userService.getProfileByEmail(email);
        
        userService.changePassword(currentUser.getId(), changePasswordDTO, email);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Mot de passe modifié avec succès");
        return ResponseEntity.ok(response);
    }
    
    @Operation(
            summary = "Upload photo de profil",
            description = "Upload une photo de profil pour l'utilisateur connecté. Formats acceptés: JPG, PNG, etc. Taille max: 5MB."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Photo uploadée avec succès"),
            @ApiResponse(responseCode = "400", description = "Fichier invalide ou trop volumineux"),
            @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    @PostMapping(value = "/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadPhoto(@RequestParam("file") MultipartFile file) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        UserResponseDTO currentUser = userService.getProfileByEmail(email);
        
        UserResponseDTO updatedUser = userService.uploadPhoto(currentUser.getId(), file, email);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Photo uploadée avec succès");
        response.put("user", updatedUser);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    @Operation(
            summary = "Supprimer la photo de profil",
            description = "Supprime la photo de profil de l'utilisateur connecté."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Photo supprimée avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    @DeleteMapping("/photo")
    public ResponseEntity<Map<String, Object>> deletePhoto() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        UserResponseDTO currentUser = userService.getProfileByEmail(email);
        
        UserResponseDTO updatedUser = userService.deletePhoto(currentUser.getId(), email);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Photo supprimée avec succès");
        response.put("user", updatedUser);
        return ResponseEntity.ok(response);
    }
}

