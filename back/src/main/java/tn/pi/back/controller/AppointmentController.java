package tn.pi.back.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.pi.back.dto.*;
import tn.pi.back.model.AppointmentStatus;
import tn.pi.back.service.AppointmentService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@Tag(name = "Gestion des Rendez-vous", description = "API pour la gestion complète des rendez-vous (création, annulation, confirmation, reprogrammation)")
@SecurityRequirement(name = "bearerAuth")
public class AppointmentController {
    
    private final AppointmentService appointmentService;
    
    @Operation(
            summary = "Créer un nouveau rendez-vous",
            description = "Crée un rendez-vous avec vérification de disponibilité et prévention des conflits"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Rendez-vous créé avec succès",
                    content = @Content(schema = @Schema(implementation = AppointmentResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Erreur de validation ou créneau non disponible"),
            @ApiResponse(responseCode = "404", description = "Docteur ou patient non trouvé"),
            @ApiResponse(responseCode = "409", description = "Conflit : un rendez-vous existe déjà pour ce créneau")
    })
    @PostMapping
    public ResponseEntity<Map<String, Object>> createAppointment(@Valid @RequestBody AppointmentRequestDTO requestDTO) {
        AppointmentResponseDTO appointment = appointmentService.createAppointment(requestDTO);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Rendez-vous créé avec succès");
        response.put("appointment", appointment);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @Operation(
            summary = "Récupérer un rendez-vous par ID",
            description = "Récupère les informations d'un rendez-vous spécifique"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rendez-vous trouvé",
                    content = @Content(schema = @Schema(implementation = AppointmentResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Rendez-vous non trouvé")
    })
    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponseDTO> getAppointmentById(
            @Parameter(description = "ID du rendez-vous", example = "1")
            @PathVariable Long id) {
        AppointmentResponseDTO appointment = appointmentService.getAppointmentById(id);
        return ResponseEntity.ok(appointment);
    }
    
    @Operation(
            summary = "Récupérer les rendez-vous d'un patient",
            description = "Récupère la liste paginée des rendez-vous d'un patient"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès",
                    content = @Content(schema = @Schema(implementation = PageResponse.class)))
    })
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<PageResponse<AppointmentResponseDTO>> getPatientAppointments(
            @Parameter(description = "ID du patient", example = "1")
            @PathVariable Long patientId,
            @Parameter(description = "Numéro de la page (0-indexé)", example = "0")
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @Parameter(description = "Taille de la page", example = "10")
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        int pageNum = (page != null) ? page : 0;
        int sizeNum = (size != null) ? size : 10;
        PageResponse<AppointmentResponseDTO> appointments = appointmentService.getPatientAppointments(patientId, pageNum, sizeNum);
        return ResponseEntity.ok(appointments);
    }
    
    @Operation(
            summary = "Récupérer tous les rendez-vous d'un patient (liste complète)",
            description = "Récupère la liste complète des rendez-vous d'un patient sans pagination"
    )
    @GetMapping("/patient/{patientId}/all")
    public ResponseEntity<List<AppointmentResponseDTO>> getPatientAppointmentsList(
            @Parameter(description = "ID du patient", example = "1")
            @PathVariable Long patientId) {
        List<AppointmentResponseDTO> appointments = appointmentService.getPatientAppointmentsList(patientId);
        return ResponseEntity.ok(appointments);
    }
    
    @Operation(
            summary = "Récupérer les rendez-vous d'un docteur",
            description = "Récupère la liste paginée des rendez-vous d'un docteur"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès",
                    content = @Content(schema = @Schema(implementation = PageResponse.class)))
    })
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<PageResponse<AppointmentResponseDTO>> getDoctorAppointments(
            @Parameter(description = "ID du docteur", example = "1")
            @PathVariable Long doctorId,
            @Parameter(description = "Numéro de la page (0-indexé)", example = "0")
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @Parameter(description = "Taille de la page", example = "10")
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        int pageNum = (page != null) ? page : 0;
        int sizeNum = (size != null) ? size : 10;
        PageResponse<AppointmentResponseDTO> appointments = appointmentService.getDoctorAppointments(doctorId, pageNum, sizeNum);
        return ResponseEntity.ok(appointments);
    }
    
    @Operation(
            summary = "Récupérer tous les rendez-vous d'un docteur (liste complète)",
            description = "Récupère la liste complète des rendez-vous d'un docteur sans pagination"
    )
    @GetMapping("/doctor/{doctorId}/all")
    public ResponseEntity<List<AppointmentResponseDTO>> getDoctorAppointmentsList(
            @Parameter(description = "ID du docteur", example = "1")
            @PathVariable Long doctorId) {
        List<AppointmentResponseDTO> appointments = appointmentService.getDoctorAppointmentsList(doctorId);
        return ResponseEntity.ok(appointments);
    }
    
    @Operation(
            summary = "Récupérer les rendez-vous d'un docteur pour une date spécifique",
            description = "Récupère tous les rendez-vous d'un docteur pour une date donnée"
    )
    @GetMapping("/doctor/{doctorId}/date")
    public ResponseEntity<List<AppointmentResponseDTO>> getDoctorAppointmentsByDate(
            @Parameter(description = "ID du docteur", example = "1")
            @PathVariable Long doctorId,
            @Parameter(description = "Date (AAAA-MM-JJ)", example = "2024-01-15")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<AppointmentResponseDTO> appointments = appointmentService.getDoctorAppointmentsByDate(doctorId, date);
        return ResponseEntity.ok(appointments);
    }
    
    @Operation(
            summary = "Récupérer le calendrier d'un docteur par semaine",
            description = "Récupère tous les rendez-vous d'un docteur pour une semaine (du lundi au dimanche). " +
                          "Le paramètre weekStart peut être n'importe quel jour de la semaine, le système calcule automatiquement le lundi."
    )
    @GetMapping("/doctor/{doctorId}/week")
    public ResponseEntity<List<AppointmentResponseDTO>> getDoctorAppointmentsByWeek(
            @Parameter(description = "ID du docteur", example = "1")
            @PathVariable Long doctorId,
            @Parameter(description = "Date de début de semaine (n'importe quel jour, sera converti en lundi)", example = "2024-01-15")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        List<AppointmentResponseDTO> appointments = appointmentService.getDoctorAppointmentsByWeek(doctorId, weekStart);
        return ResponseEntity.ok(appointments);
    }
    
    @Operation(
            summary = "Vérifier la disponibilité d'un créneau",
            description = "Vérifie si un créneau est disponible pour un docteur à une date et heure données. " +
                          "Vérifie à la fois les créneaux horaires du docteur et les conflits avec d'autres rendez-vous."
    )
    @GetMapping("/check-availability")
    public ResponseEntity<AppointmentAvailabilityDTO> checkAvailability(
            @Parameter(description = "ID du docteur", example = "1")
            @RequestParam Long doctorId,
            @Parameter(description = "Date (AAAA-MM-JJ)", example = "2024-01-15")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "Heure (HH:mm)", example = "10:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime heure) {
        AppointmentAvailabilityDTO availability = appointmentService.checkAvailability(doctorId, date, heure);
        return ResponseEntity.ok(availability);
    }
    
    @Operation(
            summary = "Confirmer un rendez-vous",
            description = "Change le statut d'un rendez-vous à CONFIRMED"
    )
    @PutMapping("/{id}/confirm")
    public ResponseEntity<AppointmentResponseDTO> confirmAppointment(
            @Parameter(description = "ID du rendez-vous", example = "1")
            @PathVariable Long id) {
        AppointmentResponseDTO appointment = appointmentService.confirmAppointment(id);
        return ResponseEntity.ok(appointment);
    }
    
    @Operation(
            summary = "Annuler un rendez-vous",
            description = "Change le statut d'un rendez-vous à CANCELLED"
    )
    @PutMapping("/{id}/cancel")
    public ResponseEntity<AppointmentResponseDTO> cancelAppointment(
            @Parameter(description = "ID du rendez-vous", example = "1")
            @PathVariable Long id) {
        AppointmentResponseDTO appointment = appointmentService.cancelAppointment(id);
        return ResponseEntity.ok(appointment);
    }
    
    @Operation(
            summary = "Marquer un rendez-vous comme terminé",
            description = "Change le statut d'un rendez-vous à COMPLETED"
    )
    @PutMapping("/{id}/complete")
    public ResponseEntity<AppointmentResponseDTO> completeAppointment(
            @Parameter(description = "ID du rendez-vous", example = "1")
            @PathVariable Long id) {
        AppointmentResponseDTO appointment = appointmentService.completeAppointment(id);
        return ResponseEntity.ok(appointment);
    }
    
    @Operation(
            summary = "Marquer un rendez-vous comme absent (NO_SHOW)",
            description = "Change le statut d'un rendez-vous à NO_SHOW"
    )
    @PutMapping("/{id}/no-show")
    public ResponseEntity<AppointmentResponseDTO> markNoShow(
            @Parameter(description = "ID du rendez-vous", example = "1")
            @PathVariable Long id) {
        AppointmentResponseDTO appointment = appointmentService.markNoShow(id);
        return ResponseEntity.ok(appointment);
    }
    
    @Operation(
            summary = "Changer le statut d'un rendez-vous",
            description = "Change le statut d'un rendez-vous (PENDING, CONFIRMED, CANCELLED, COMPLETED, NO_SHOW)"
    )
    @PutMapping("/{id}/status")
    public ResponseEntity<AppointmentResponseDTO> updateAppointmentStatus(
            @Parameter(description = "ID du rendez-vous", example = "1")
            @PathVariable Long id,
            @Parameter(description = "Nouveau statut", example = "CONFIRMED")
            @RequestParam AppointmentStatus status) {
        AppointmentResponseDTO appointment = appointmentService.updateAppointmentStatus(id, status);
        return ResponseEntity.ok(appointment);
    }
    
    @Operation(
            summary = "Reprogrammer un rendez-vous",
            description = "Change la date et l'heure d'un rendez-vous avec vérification de disponibilité"
    )
    @PutMapping("/{id}/reschedule")
    public ResponseEntity<AppointmentResponseDTO> rescheduleAppointment(
            @Parameter(description = "ID du rendez-vous", example = "1")
            @PathVariable Long id,
            @Parameter(description = "Nouvelle date (AAAA-MM-JJ)", example = "2024-01-20")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate newDate,
            @Parameter(description = "Nouvelle heure (HH:mm)", example = "14:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime newHeure) {
        AppointmentResponseDTO appointment = appointmentService.rescheduleAppointment(id, newDate, newHeure);
        return ResponseEntity.ok(appointment);
    }
    
    @Operation(
            summary = "Mettre à jour un rendez-vous",
            description = "Met à jour les informations d'un rendez-vous (docteur, patient, date, heure, motif, notes)"
    )
    @PutMapping("/{id}")
    public ResponseEntity<AppointmentResponseDTO> updateAppointment(
            @Parameter(description = "ID du rendez-vous", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody AppointmentRequestDTO requestDTO) {
        AppointmentResponseDTO appointment = appointmentService.updateAppointment(id, requestDTO);
        return ResponseEntity.ok(appointment);
    }
    
    @Operation(
            summary = "Supprimer un rendez-vous (soft delete)",
            description = "Marque un rendez-vous comme supprimé"
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppointment(
            @Parameter(description = "ID du rendez-vous", example = "1")
            @PathVariable Long id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.noContent().build();
    }
    
    @Operation(
            summary = "Compter les rendez-vous d'un docteur",
            description = "Retourne le nombre total de rendez-vous d'un docteur"
    )
    @GetMapping("/doctor/{doctorId}/count")
    public ResponseEntity<Long> countDoctorAppointments(
            @Parameter(description = "ID du docteur", example = "1")
            @PathVariable Long doctorId) {
        long count = appointmentService.countAppointmentsByDoctor(doctorId);
        return ResponseEntity.ok(count);
    }
    
    @Operation(
            summary = "Compter les rendez-vous d'un patient",
            description = "Retourne le nombre total de rendez-vous d'un patient"
    )
    @GetMapping("/patient/{patientId}/count")
    public ResponseEntity<Long> countPatientAppointments(
            @Parameter(description = "ID du patient", example = "1")
            @PathVariable Long patientId) {
        long count = appointmentService.countAppointmentsByPatient(patientId);
        return ResponseEntity.ok(count);
    }
}

