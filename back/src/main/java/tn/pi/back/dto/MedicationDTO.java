package tn.pi.back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO pour un médicament dans une ordonnance")
public class MedicationDTO {
    
    @NotBlank(message = "Le nom du médicament est obligatoire")
    @Schema(description = "Nom du médicament", example = "Paracétamol", required = true)
    private String name;
    
    @NotBlank(message = "Le dosage est obligatoire")
    @Schema(description = "Dosage", example = "500mg", required = true)
    private String dosage;
    
    @NotBlank(message = "La fréquence est obligatoire")
    @Schema(description = "Fréquence", example = "3 fois par jour", required = true)
    private String frequency;
    
    @NotBlank(message = "La durée est obligatoire")
    @Schema(description = "Durée", example = "7 jours", required = true)
    private String duration;
    
    @Schema(description = "Instructions spéciales", example = "Pendant les repas")
    private String instructions;
}

