package tn.pi.back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.pi.back.model.MedicalAttachment.AttachmentType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Item de timeline pour l'historique chronologique")
public class TimelineItemDTO {
    
    @Schema(description = "Type d'item (VISIT ou ATTACHMENT)", example = "VISIT")
    private TimelineItemType type;
    
    @Schema(description = "ID de l'item", example = "1")
    private Long id;
    
    @Schema(description = "Date/heure de l'item", example = "2024-01-15T14:30:00")
    private LocalDateTime dateTime;
    
    @Schema(description = "Date de la consultation (si type VISIT)", example = "2024-01-15")
    private LocalDate visitDate;
    
    @Schema(description = "Heure de la consultation (si type VISIT)", example = "14:30")
    private LocalTime visitTime;
    
    @Schema(description = "Titre/Description de l'item")
    private String title;
    
    @Schema(description = "Docteur associé")
    private String doctorName;
    
    @Schema(description = "ID du docteur")
    private Long doctorId;
    
    // Pour les consultations
    @Schema(description = "Motif de consultation (si type VISIT)")
    private String reason;
    
    @Schema(description = "Diagnostic (si type VISIT)")
    private String diagnosis;
    
    // Pour les pièces jointes
    @Schema(description = "Nom du fichier (si type ATTACHMENT)")
    private String fileName;
    
    @Schema(description = "Type de pièce jointe (si type ATTACHMENT)")
    private AttachmentType attachmentType;
    
    @Schema(description = "Description de la pièce jointe (si type ATTACHMENT)")
    private String description;
    
    public enum TimelineItemType {
        VISIT,      // Consultation
        ATTACHMENT  // Pièce jointe
    }
}

