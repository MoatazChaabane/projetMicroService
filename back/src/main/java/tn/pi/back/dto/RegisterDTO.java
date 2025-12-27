package tn.pi.back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import tn.pi.back.model.Role;

@Data
@Schema(description = "DTO pour l'inscription d'un nouvel utilisateur")
public class RegisterDTO {
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    @Schema(description = "Adresse email de l'utilisateur", example = "user@example.com", required = true)
    private String email;
    
    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    @Schema(description = "Mot de passe (minimum 6 caractères)", example = "password123", required = true, minLength = 6)
    private String password;
    
    @NotBlank(message = "Le prénom est obligatoire")
    @Schema(description = "Prénom de l'utilisateur", example = "John", required = true)
    private String firstName;
    
    @NotBlank(message = "Le nom est obligatoire")
    @Schema(description = "Nom de l'utilisateur", example = "Doe", required = true)
    private String lastName;
    
    @Schema(description = "Numéro de téléphone", example = "+1234567890", required = false)
    private String phoneNumber;
    
    @NotNull(message = "Le rôle est obligatoire")
    @Schema(description = "Rôle de l'utilisateur", example = "PATIENT", required = true, allowableValues = {"PATIENT", "DOCTOR", "ADMIN"})
    private Role role;
}

