package tn.pi.back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "DTO pour la connexion d'un utilisateur")
public class LoginDTO {
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    @Schema(description = "Adresse email de l'utilisateur", example = "user@example.com", required = true)
    private String email;
    
    @NotBlank(message = "Le mot de passe est obligatoire")
    @Schema(description = "Mot de passe de l'utilisateur", example = "password123", required = true)
    private String password;
}

