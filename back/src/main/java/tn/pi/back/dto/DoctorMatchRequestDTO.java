package tn.pi.back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.pi.back.model.Specialite;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO pour la recherche de matching de docteurs basée sur les symptômes")
public class DoctorMatchRequestDTO {
    
    @NotBlank(message = "Les symptômes sont obligatoires")
    @Schema(description = "Symptômes sous forme de texte ou liste de tags", 
            example = "douleur thoracique, essoufflement", required = true)
    private String symptomes;
    
    @Schema(description = "Liste optionnelle de tags de symptômes", 
            example = "[\"douleur thoracique\", \"essoufflement\"]")
    private List<String> tags;
    
    @Schema(description = "Spécialité optionnelle pour filtrer les résultats", 
            example = "CARDIOLOGIE")
    private Specialite specialite;
    
    @NotNull(message = "La latitude est obligatoire")
    @Schema(description = "Latitude de la position du patient", example = "36.8065", required = true)
    private Double latitude;
    
    @NotNull(message = "La longitude est obligatoire")
    @Schema(description = "Longitude de la position du patient", example = "10.1815", required = true)
    private Double longitude;
    
    @NotNull(message = "Le rayon de recherche est obligatoire")
    @Schema(description = "Rayon de recherche en kilomètres", example = "10.0", required = true)
    private Double rayonKm;
    
    @Schema(description = "Date souhaitée pour le rendez-vous (optionnelle)", 
            example = "2024-01-15")
    private LocalDate dateSouhaitee;
    
    @Schema(description = "Nombre maximum de résultats à retourner", example = "10", defaultValue = "10")
    @Builder.Default
    private Integer limit = 10;
}

