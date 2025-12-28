package tn.pi.back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Réponse contenant les informations d'une ordonnance")
public class PrescriptionResponseDTO {
    
    @Schema(description = "ID de l'ordonnance", example = "1")
    private Long id;
    
    @Schema(description = "ID du patient", example = "1")
    private Long patientId;
    
    @Schema(description = "Nom complet du patient", example = "Jean Dupont")
    private String patientName;
    
    @Schema(description = "ID du docteur", example = "1")
    private Long doctorId;
    
    @Schema(description = "Nom complet du docteur", example = "Dr. Marie Martin")
    private String doctorName;
    
    @Schema(description = "Spécialité du docteur", example = "CARDIOLOGIE")
    private String doctorSpeciality;
    
    @Schema(description = "Liste des médicaments")
    private List<MedicationDTO> medications;
    
    @Schema(description = "Instructions générales", example = "Repos recommandé")
    private String instructions;
    
    @Schema(description = "Date de l'ordonnance", example = "2024-01-15")
    private LocalDate date;
    
    @Schema(description = "URL du PDF", example = "/prescriptions/pdfs/prescription-1.pdf")
    private String pdfUrl;
    
    @Schema(description = "Hash de la signature numérique (SHA-256)", example = "a1b2c3d4...")
    private String signatureHash;
    
    @Schema(description = "Date de création", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "Date de dernière mise à jour", example = "2024-01-15T10:30:00")
    private LocalDateTime updatedAt;
}

