package tn.pi.back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Réponse de matching de docteur avec score de pertinence")
public class DoctorMatchResponseDTO {
    
    @Schema(description = "Informations du docteur")
    private DoctorResponseDTO doctor;
    
    @Schema(description = "Score total de pertinence (0.0 à 1.0)", example = "0.85")
    private Double scoreTotal;
    
    @Schema(description = "Score de pertinence des symptômes/spécialité (0.0 à 1.0)", example = "1.0")
    private Double scoreSymptomes;
    
    @Schema(description = "Score de distance normalisé (0.0 à 1.0, 1.0 = plus proche)", example = "0.9")
    private Double scoreDistance;
    
    @Schema(description = "Score de disponibilité (0.0 ou 1.0)", example = "1.0")
    private Double scoreDisponibilite;
    
    @Schema(description = "Distance en kilomètres", example = "2.5")
    private Double distanceKm;
    
    @Schema(description = "Indique si le docteur est disponible à la date souhaitée", example = "true")
    private Boolean disponible;
    
    @Schema(description = "Message expliquant le score ou la disponibilité", 
            example = "Docteur disponible et proche de votre position")
    private String message;
}

