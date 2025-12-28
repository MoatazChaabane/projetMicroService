package tn.pi.back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.pi.back.model.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Réponse contenant les informations d'un rendez-vous")
public class AppointmentResponseDTO {
    @Schema(description = "ID du rendez-vous", example = "1")
    private Long id;
    
    @Schema(description = "ID du docteur", example = "1")
    private Long doctorId;
    
    @Schema(description = "Nom complet du docteur", example = "Dr. Jean Dupont")
    private String doctorNomComplet;
    
    @Schema(description = "Spécialité du docteur", example = "CARDIOLOGIE")
    private String doctorSpecialite;
    
    @Schema(description = "ID du patient", example = "1")
    private Long patientId;
    
    @Schema(description = "Nom complet du patient", example = "Marie Martin")
    private String patientNomComplet;
    
    @Schema(description = "Date du rendez-vous", example = "2024-01-15")
    private LocalDate date;
    
    @Schema(description = "Heure du rendez-vous", example = "10:00")
    private LocalTime heure;
    
    @Schema(description = "Statut du rendez-vous", example = "PENDING")
    private AppointmentStatus status;
    
    @Schema(description = "Motif de la consultation", example = "Consultation de routine")
    private String motif;
    
    @Schema(description = "Notes additionnelles", example = "Première consultation")
    private String notes;
    
    @Schema(description = "Date de création", example = "2024-01-10T10:00:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "Date de dernière mise à jour", example = "2024-01-10T10:00:00")
    private LocalDateTime updatedAt;
}

