package tn.pi.back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.pi.back.model.MedicalAttachment.AttachmentType;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Réponse contenant les informations d'une pièce jointe")
public class AttachmentResponseDTO {
    
    @Schema(description = "ID de la pièce jointe", example = "1")
    private Long id;
    
    @Schema(description = "ID du dossier médical", example = "1")
    private Long medicalRecordId;
    
    @Schema(description = "ID du docteur", example = "1")
    private Long doctorId;
    
    @Schema(description = "Nom du docteur", example = "Dr. Marie Martin")
    private String doctorName;
    
    @Schema(description = "Nom du fichier", example = "analyse_sang.pdf")
    private String fileName;
    
    @Schema(description = "Type MIME du fichier", example = "application/pdf")
    private String fileType;
    
    @Schema(description = "Type de pièce jointe", example = "ANALYSE")
    private AttachmentType attachmentType;
    
    @Schema(description = "Description du fichier")
    private String description;
    
    @Schema(description = "Taille du fichier en bytes", example = "1024000")
    private Long fileSize;
    
    @Schema(description = "URL de téléchargement", example = "/api/medical-records/attachments/1/download")
    private String downloadUrl;
    
    @Schema(description = "Date de création", example = "2024-01-15T14:30:00")
    private LocalDateTime createdAt;
}

