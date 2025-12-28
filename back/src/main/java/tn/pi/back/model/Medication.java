package tn.pi.back.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Medication {
    private String name; // Nom du médicament
    private String dosage; // Dosage (ex: "500mg", "2 comprimés")
    private String frequency; // Fréquence (ex: "3 fois par jour", "1 fois le matin")
    private String duration; // Durée (ex: "7 jours", "2 semaines")
    private String instructions; // Instructions spéciales (ex: "Pendant les repas")
}

