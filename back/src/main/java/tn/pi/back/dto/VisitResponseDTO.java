package tn.pi.back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Réponse contenant les informations d'une consultation")
public class VisitResponseDTO {
    
    @Schema(description = "ID de la consultation", example = "1")
    private Long id;
    
    @Schema(description = "ID du dossier médical", example = "1")
    private Long medicalRecordId;
    
    @Schema(description = "ID du docteur", example = "1")
    private Long doctorId;
    
    @Schema(description = "Nom complet du docteur", example = "Dr. Marie Martin")
    private String doctorName;
    
    @Schema(description = "Date de la consultation", example = "2024-01-15")
    private LocalDate visitDate;
    
    @Schema(description = "Heure de la consultation", example = "14:30")
    private LocalTime visitTime;
    
    @Schema(description = "Motif de la consultation")
    private String reason;
    
    @Schema(description = "Symptômes observés")
    private String symptoms;
    
    @Schema(description = "Diagnostic")
    private String diagnosis;
    
    @Schema(description = "Traitement prescrit")
    private String treatment;
    
    @Schema(description = "Notes additionnelles")
    private String notes;
    
    @Schema(description = "Date de création", example = "2024-01-15T14:30:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "Date de dernière mise à jour", example = "2024-01-15T14:30:00")
    private LocalDateTime updatedAt;
}

