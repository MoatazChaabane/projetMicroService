package tn.pi.back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.pi.back.model.Specialite;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Réponse contenant les informations d'un docteur")
public class DoctorResponseDTO {
    @Schema(description = "ID du docteur", example = "1")
    private Long id;
    
    @Schema(description = "ID de l'utilisateur", example = "1")
    private Long userId;
    
    @Schema(description = "Nom complet du docteur", example = "Dr. Jean Dupont")
    private String nomComplet;
    
    @Schema(description = "Email du docteur", example = "jean.dupont@example.com")
    private String email;
    
    @Schema(description = "Téléphone du docteur", example = "+33123456789")
    private String telephone;
    
    @Schema(description = "Spécialité médicale", example = "CARDIOLOGIE")
    private Specialite specialite;
    
    @Schema(description = "Nom de la clinique", example = "Clinique du Cœur")
    private String nomClinique;
    
    @Schema(description = "Adresse complète", example = "123 Rue de la Santé, 75014 Paris")
    private String adresse;
    
    @Schema(description = "Latitude GPS", example = "48.8566")
    private Double latitude;
    
    @Schema(description = "Longitude GPS", example = "2.3522")
    private Double longitude;
    
    @Schema(description = "Tarif de consultation en euros", example = "50.00")
    private BigDecimal tarifConsultation;
    
    @Schema(description = "Liste des langues parlées", example = "[\"Français\", \"Anglais\"]")
    private List<String> langues;
    
    @Schema(description = "Note moyenne (sur 5)", example = "4.5")
    private BigDecimal rating;
    
    @Schema(description = "Nombre d'avis", example = "25")
    private Integer nombreAvis;
    
    @Schema(description = "Autorise la téléconsultation", example = "true")
    private Boolean teleconsultation;
    
    @Schema(description = "Liste des créneaux horaires disponibles")
    private List<TimeSlotDTO> horaires;
    
    @Schema(description = "Date de création", example = "2023-10-26T10:00:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "Date de dernière mise à jour", example = "2023-10-26T11:30:00")
    private LocalDateTime updatedAt;
}

