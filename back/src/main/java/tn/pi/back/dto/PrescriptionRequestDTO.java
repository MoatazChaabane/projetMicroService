package tn.pi.back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO pour la création d'une ordonnance")
public class PrescriptionRequestDTO {
    
    @NotNull(message = "L'ID du patient est obligatoire")
    @Schema(description = "ID du patient", example = "1", required = true)
    private Long patientId;
    
    @NotNull(message = "L'ID du docteur est obligatoire")
    @Schema(description = "ID du docteur", example = "1", required = true)
    private Long doctorId;
    
    @NotEmpty(message = "Au moins un médicament est requis")
    @Schema(description = "Liste des médicaments", required = true)
    @Valid
    private List<MedicationDTO> medications;
    
    @Schema(description = "Instructions générales", example = "Repos recommandé. Éviter l'alcool.")
    private String instructions;
    
    @Schema(description = "Date de l'ordonnance (par défaut: aujourd'hui)", example = "2024-01-15")
    private LocalDate date;
}

