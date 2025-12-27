package tn.pi.back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.pi.back.model.Role;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Réponse contenant les informations d'un utilisateur")
public class UserResponseDTO {
    @Schema(description = "Identifiant unique de l'utilisateur", example = "1")
    private Long id;
    
    @Schema(description = "Adresse email", example = "user@example.com")
    private String email;
    
    @Schema(description = "Prénom", example = "John")
    private String firstName;
    
    @Schema(description = "Nom", example = "Doe")
    private String lastName;
    
    @Schema(description = "Numéro de téléphone", example = "+1234567890")
    private String phoneNumber;
    
    @Schema(description = "Rôle de l'utilisateur", example = "PATIENT")
    private Role role;
    
    @Schema(description = "URL de la photo de profil", example = "/uploads/user_photo.jpg")
    private String photoUrl;
    
    @Schema(description = "Date de création du compte")
    private LocalDateTime createdAt;
    
    @Schema(description = "Date de dernière mise à jour")
    private LocalDateTime updatedAt;
}

