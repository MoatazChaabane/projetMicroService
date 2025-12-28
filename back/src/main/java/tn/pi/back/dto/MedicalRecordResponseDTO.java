package tn.pi.back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Réponse contenant les informations d'un dossier médical")
public class MedicalRecordResponseDTO {
    
    @Schema(description = "ID du dossier médical", example = "1")
    private Long id;
    
    @Schema(description = "ID du patient", example = "1")
    private Long patientId;
    
    @Schema(description = "Nom complet du patient", example = "Jean Dupont")
    private String patientName;
    
    @Schema(description = "Notes générales")
    private String notes;
    
    @Schema(description = "Nombre de consultations", example = "5")
    private Long visitsCount;
    
    @Schema(description = "Nombre de pièces jointes", example = "3")
    private Long attachmentsCount;
    
    @Schema(description = "Date de création", example = "2024-01-15T14:30:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "Date de dernière mise à jour", example = "2024-01-15T14:30:00")
    private LocalDateTime updatedAt;
}

