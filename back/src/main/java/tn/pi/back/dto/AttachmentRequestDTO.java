package tn.pi.back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import tn.pi.back.model.MedicalAttachment.AttachmentType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO pour l'ajout d'une pièce jointe au dossier médical")
public class AttachmentRequestDTO {
    
    @NotNull(message = "L'ID du dossier médical est obligatoire")
    @Schema(description = "ID du dossier médical", example = "1", required = true)
    private Long medicalRecordId;
    
    @Schema(description = "ID du docteur (optionnel)", example = "1")
    private Long doctorId;
    
    @NotNull(message = "Le type de pièce jointe est obligatoire")
    @Schema(description = "Type de pièce jointe", example = "ANALYSE", required = true)
    private AttachmentType attachmentType;
    
    @Schema(description = "Description du fichier", example = "Analyse de sang - NFS")
    private String description;
    
    @NotNull(message = "Le fichier est obligatoire")
    @Schema(description = "Fichier à uploader", required = true)
    private MultipartFile file;
}

