package tn.pi.back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO pour la création/modification d'une consultation")
public class VisitRequestDTO {
    
    @NotNull(message = "L'ID du dossier médical est obligatoire")
    @Schema(description = "ID du dossier médical", example = "1", required = true)
    private Long medicalRecordId;
    
    @NotNull(message = "L'ID du docteur est obligatoire")
    @Schema(description = "ID du docteur", example = "1", required = true)
    private Long doctorId;
    
    @Schema(description = "Date de la consultation (par défaut: aujourd'hui)", example = "2024-01-15")
    private LocalDate visitDate;
    
    @Schema(description = "Heure de la consultation", example = "14:30")
    private LocalTime visitTime;
    
    @Schema(description = "Motif de la consultation", example = "Consultation de routine")
    private String reason;
    
    @Schema(description = "Symptômes observés", example = "Toux persistante depuis 3 jours")
    private String symptoms;
    
    @Schema(description = "Diagnostic", example = "Rhume")
    private String diagnosis;
    
    @Schema(description = "Traitement prescrit", example = "Repos et hydratation")
    private String treatment;
    
    @Schema(description = "Notes additionnelles", example = "À revoir dans 1 semaine si pas d'amélioration")
    private String notes;
}

