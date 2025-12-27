package tn.pi.back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.pi.back.model.Specialite;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Critères de recherche pour les docteurs")
public class DoctorSearchDTO {
    @Schema(description = "Spécialité médicale", example = "CARDIOLOGIE")
    private Specialite specialite;
    
    @Schema(description = "Latitude du point de recherche", example = "48.8566")
    private Double latitude;
    
    @Schema(description = "Longitude du point de recherche", example = "2.3522")
    private Double longitude;
    
    @Schema(description = "Rayon de recherche en kilomètres", example = "10.0")
    private Double rayonKm;
    
    @Schema(description = "Date pour vérifier la disponibilité (AAAA-MM-JJ)", example = "2023-12-28")
    private LocalDate date;
    
    @Schema(description = "Heure pour vérifier la disponibilité (HH:mm)", example = "10:00")
    private LocalTime heure;
    
    @Schema(description = "Autorise la téléconsultation", example = "true")
    private Boolean teleconsultation;
    
    @Schema(description = "Note minimum requise", example = "4.0")
    private Double ratingMin;
}

