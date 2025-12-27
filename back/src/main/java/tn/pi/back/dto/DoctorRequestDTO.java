package tn.pi.back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import tn.pi.back.model.Specialite;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "DTO pour la création/mise à jour d'un docteur")
public class DoctorRequestDTO {
    @NotNull(message = "L'ID utilisateur est obligatoire")
    @Schema(description = "ID de l'utilisateur (docteur)", example = "1", required = true)
    private Long userId;
    
    @NotNull(message = "La spécialité est obligatoire")
    @Schema(description = "Spécialité médicale", example = "CARDIOLOGIE", required = true)
    private Specialite specialite;
    
    @Schema(description = "Nom de la clinique", example = "Clinique du Cœur")
    private String nomClinique;
    
    @Schema(description = "Adresse complète", example = "123 Rue de la Santé, 75014 Paris")
    private String adresse;
    
    @DecimalMin(value = "-90.0", message = "La latitude doit être entre -90 et 90")
    @DecimalMax(value = "90.0", message = "La latitude doit être entre -90 et 90")
    @Schema(description = "Latitude GPS", example = "48.8566")
    private Double latitude;
    
    @DecimalMin(value = "-180.0", message = "La longitude doit être entre -180 et 180")
    @DecimalMax(value = "180.0", message = "La longitude doit être entre -180 et 180")
    @Schema(description = "Longitude GPS", example = "2.3522")
    private Double longitude;
    
    @DecimalMin(value = "0.0", message = "Le tarif doit être positif")
    @Schema(description = "Tarif de consultation en euros", example = "50.00")
    private BigDecimal tarifConsultation;
    
    @Schema(description = "Liste des langues parlées", example = "[\"Français\", \"Anglais\", \"Arabe\"]")
    private List<String> langues;
    
    @Schema(description = "Autorise la téléconsultation", example = "true")
    private Boolean teleconsultation;
    
    @Valid
    @Schema(description = "Liste des créneaux horaires disponibles")
    private List<TimeSlotDTO> horaires;
}

