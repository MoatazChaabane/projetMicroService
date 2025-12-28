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
@Schema(description = "Requête pour l'analyse de symptômes")
public class SymptomAnalysisRequestDTO {
    
    @NotBlank(message = "La description des symptômes est obligatoire")
    @Schema(description = "Description textuelle des symptômes par le patient", 
            example = "J'ai des douleurs thoraciques depuis 3 jours, surtout en position allongée, avec essoufflement", 
            required = true)
    private String description;
    
    @Schema(description = "ID du rendez-vous si l'analyse est liée à un RDV existant", 
            example = "1")
    private Long appointmentId;
    
    @Schema(description = "ID du patient", example = "1")
    private Long patientId;
}

