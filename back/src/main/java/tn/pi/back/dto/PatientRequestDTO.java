package tn.pi.back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import tn.pi.back.model.Sexe;

import java.time.LocalDate;

@Data
@Schema(description = "DTO pour la création/mise à jour d'un patient")
public class PatientRequestDTO {
    @NotBlank(message = "Le nom est obligatoire")
    @Schema(description = "Nom du patient", example = "Dupont", required = true)
    private String nom;
    
    @NotBlank(message = "Le prénom est obligatoire")
    @Schema(description = "Prénom du patient", example = "Jean", required = true)
    private String prenom;
    
    @NotNull(message = "La date de naissance est obligatoire")
    @Past(message = "La date de naissance doit être dans le passé")
    @Schema(description = "Date de naissance", example = "1990-01-15", required = true)
    private LocalDate dateNaissance;
    
    @NotNull(message = "Le sexe est obligatoire")
    @Schema(description = "Sexe du patient", example = "M", required = true, allowableValues = {"M", "F"})
    private Sexe sexe;
    
    @Pattern(regexp = "^[+]?[0-9]{8,15}$", message = "Le numéro de téléphone doit être valide")
    @Schema(description = "Numéro de téléphone", example = "+33123456789")
    private String telephone;
    
    @Schema(description = "Adresse complète", example = "123 Rue de la Paix, 75001 Paris")
    private String adresse;
    
    @Schema(description = "Allergies connues", example = "Pénicilline, Pollen")
    private String allergies;
    
    @Schema(description = "Antécédents médicaux", example = "Hypertension, Diabète type 2")
    private String antecedents;
    
    @Schema(description = "Nom du contact d'urgence", example = "Marie Dupont")
    private String contactUrgenceNom;
    
    @Pattern(regexp = "^[+]?[0-9]{8,15}$", message = "Le numéro de téléphone du contact d'urgence doit être valide")
    @Schema(description = "Téléphone du contact d'urgence", example = "+33987654321")
    private String contactUrgenceTelephone;
    
    @Schema(description = "Relation avec le contact d'urgence", example = "Épouse")
    private String contactUrgenceRelation;
}

