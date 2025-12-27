package tn.pi.back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "DTO pour le changement de mot de passe")
public class ChangePasswordDTO {
    @NotBlank(message = "Le mot de passe actuel est obligatoire")
    @Schema(description = "Mot de passe actuel", example = "oldpassword", required = true)
    private String currentPassword;
    
    @NotBlank(message = "Le nouveau mot de passe est obligatoire")
    @Size(min = 6, message = "Le nouveau mot de passe doit contenir au moins 6 caractères")
    @Schema(description = "Nouveau mot de passe (minimum 6 caractères)", example = "newpassword123", required = true, minLength = 6)
    private String newPassword;
}

