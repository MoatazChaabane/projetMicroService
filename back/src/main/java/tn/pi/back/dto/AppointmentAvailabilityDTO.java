package tn.pi.back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO pour vérifier la disponibilité d'un créneau")
public class AppointmentAvailabilityDTO {
    @Schema(description = "ID du docteur", example = "1")
    private Long doctorId;
    
    @Schema(description = "Date du rendez-vous", example = "2024-01-15")
    private LocalDate date;
    
    @Schema(description = "Heure du rendez-vous", example = "10:00")
    private LocalTime heure;
    
    @Schema(description = "Indique si le créneau est disponible", example = "true")
    private Boolean available;
    
    @Schema(description = "Message d'information", example = "Créneau disponible")
    private String message;
}

