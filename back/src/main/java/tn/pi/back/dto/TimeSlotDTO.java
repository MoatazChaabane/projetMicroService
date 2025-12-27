package tn.pi.back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.pi.back.model.JourSemaine;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO pour un créneau horaire")
public class TimeSlotDTO {
    @Schema(description = "ID du créneau (null pour création)", example = "1")
    private Long id;
    
    @NotNull(message = "Le jour est obligatoire")
    @Schema(description = "Jour de la semaine", example = "LUNDI", required = true)
    private JourSemaine jour;
    
    @NotNull(message = "L'heure de début est obligatoire")
    @Schema(description = "Heure de début (HH:mm)", example = "09:00", required = true)
    private LocalTime heureDebut;
    
    @NotNull(message = "L'heure de fin est obligatoire")
    @Schema(description = "Heure de fin (HH:mm)", example = "12:00", required = true)
    private LocalTime heureFin;
    
    @Schema(description = "Disponibilité du créneau", example = "true")
    private Boolean disponible;
}

