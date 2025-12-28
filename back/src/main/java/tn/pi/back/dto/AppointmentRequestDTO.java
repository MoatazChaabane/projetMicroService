package tn.pi.back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Schema(description = "DTO pour la création/mise à jour d'un rendez-vous")
public class AppointmentRequestDTO {
    @NotNull(message = "L'ID du docteur est obligatoire")
    @Schema(description = "ID du docteur", example = "1", required = true)
    private Long doctorId;
    
    @NotNull(message = "L'ID du patient est obligatoire")
    @Schema(description = "ID du patient", example = "1", required = true)
    private Long patientId;
    
    @NotNull(message = "La date est obligatoire")
    @Schema(description = "Date du rendez-vous (AAAA-MM-JJ)", example = "2024-01-15", required = true)
    private LocalDate date;
    
    @NotNull(message = "L'heure est obligatoire")
    @Schema(description = "Heure du rendez-vous (HH:mm)", example = "10:00", required = true)
    private LocalTime heure;
    
    @Schema(description = "Motif de la consultation", example = "Consultation de routine")
    private String motif;
    
    @Schema(description = "Notes additionnelles", example = "Première consultation")
    private String notes;
}

