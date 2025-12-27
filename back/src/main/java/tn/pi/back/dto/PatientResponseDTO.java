package tn.pi.back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.pi.back.model.Sexe;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Réponse contenant les informations d'un patient")
public class PatientResponseDTO {
    @Schema(description = "Identifiant unique du patient", example = "1")
    private Long id;
    
    @Schema(description = "Nom", example = "Dupont")
    private String nom;
    
    @Schema(description = "Prénom", example = "Jean")
    private String prenom;
    
    @Schema(description = "Date de naissance", example = "1990-01-15")
    private LocalDate dateNaissance;
    
    @Schema(description = "Sexe", example = "M")
    private Sexe sexe;
    
    @Schema(description = "Numéro de téléphone", example = "+33123456789")
    private String telephone;
    
    @Schema(description = "Adresse", example = "123 Rue de la Paix, 75001 Paris")
    private String adresse;
    
    @Schema(description = "Allergies", example = "Pénicilline, Pollen")
    private String allergies;
    
    @Schema(description = "Antécédents médicaux", example = "Hypertension, Diabète type 2")
    private String antecedents;
    
    @Schema(description = "Nom du contact d'urgence", example = "Marie Dupont")
    private String contactUrgenceNom;
    
    @Schema(description = "Téléphone du contact d'urgence", example = "+33987654321")
    private String contactUrgenceTelephone;
    
    @Schema(description = "Relation avec le contact d'urgence", example = "Épouse")
    private String contactUrgenceRelation;
    
    @Schema(description = "Date de création")
    private LocalDateTime createdAt;
    
    @Schema(description = "Date de dernière mise à jour")
    private LocalDateTime updatedAt;
}

