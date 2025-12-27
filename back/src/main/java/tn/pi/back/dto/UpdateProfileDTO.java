package tn.pi.back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
@Schema(description = "DTO pour la mise à jour du profil utilisateur (tous les champs sont optionnels)")
public class UpdateProfileDTO {
    @Email(message = "L'email doit être valide")
    @Schema(description = "Nouvelle adresse email", example = "newemail@example.com", required = false)
    private String email;
    
    @Schema(description = "Nouveau prénom", example = "Jane", required = false)
    private String firstName;
    
    @Schema(description = "Nouveau nom", example = "Smith", required = false)
    private String lastName;
    
    @Schema(description = "Nouveau numéro de téléphone", example = "+9876543210", required = false)
    private String phoneNumber;
}

