package tn.pi.back.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.pi.back.dto.PageResponse;
import tn.pi.back.dto.PrescriptionRequestDTO;
import tn.pi.back.dto.PrescriptionResponseDTO;

import java.util.List;

@RestController
@RequestMapping("/api/prescriptions")
@RequiredArgsConstructor
@Tag(name = "Prescriptions", description = "API pour la gestion des ordonnances")
public class PrescriptionController {
    
    private final tn.pi.back.service.PrescriptionService prescriptionService;
    
    @Operation(
            summary = "Créer une ordonnance",
            description = "Crée une nouvelle ordonnance avec génération automatique du PDF et signature numérique. " +
                         "Seuls les docteurs peuvent créer des ordonnances."
    )
    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<PrescriptionResponseDTO> createPrescription(
            @Valid @RequestBody PrescriptionRequestDTO requestDTO) {
        PrescriptionResponseDTO prescription = prescriptionService.createPrescription(requestDTO);
        return ResponseEntity.ok(prescription);
    }
    
    @Operation(
            summary = "Récupérer une ordonnance par ID",
            description = "Récupère les détails d'une ordonnance par son ID. " +
                         "Les patients peuvent voir leurs ordonnances, les docteurs celles qu'ils ont créées, et les admins toutes."
    )
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'ADMIN')")
    public ResponseEntity<PrescriptionResponseDTO> getPrescriptionById(
            @Parameter(description = "ID de l'ordonnance", example = "1")
            @PathVariable Long id) {
        PrescriptionResponseDTO prescription = prescriptionService.getPrescriptionById(id);
        return ResponseEntity.ok(prescription);
    }
    
    @Operation(
            summary = "Récupérer les ordonnances d'un patient",
            description = "Récupère toutes les ordonnances d'un patient (paginé). " +
                         "Accessible par le patient lui-même, les docteurs et les admins."
    )
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'ADMIN')")
    public ResponseEntity<PageResponse<PrescriptionResponseDTO>> getPatientPrescriptions(
            @Parameter(description = "ID du patient", example = "1")
            @PathVariable Long patientId,
            @Parameter(description = "Numéro de page (0-indexé)", example = "0")
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @Parameter(description = "Taille de la page", example = "10")
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        int pageNum = (page != null) ? page : 0;
        int sizeNum = (size != null) ? size : 10;
        PageResponse<PrescriptionResponseDTO> prescriptions = prescriptionService.getPatientPrescriptions(patientId, pageNum, sizeNum);
        return ResponseEntity.ok(prescriptions);
    }
    
    @Operation(
            summary = "Récupérer les ordonnances d'un docteur",
            description = "Récupère toutes les ordonnances créées par un docteur (paginé). " +
                         "Accessible par le docteur lui-même et les admins."
    )
    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<PageResponse<PrescriptionResponseDTO>> getDoctorPrescriptions(
            @Parameter(description = "ID du docteur", example = "1")
            @PathVariable Long doctorId,
            @Parameter(description = "Numéro de page (0-indexé)", example = "0")
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @Parameter(description = "Taille de la page", example = "10")
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        int pageNum = (page != null) ? page : 0;
        int sizeNum = (size != null) ? size : 10;
        PageResponse<PrescriptionResponseDTO> prescriptions = prescriptionService.getDoctorPrescriptions(doctorId, pageNum, sizeNum);
        return ResponseEntity.ok(prescriptions);
    }
    
    @Operation(
            summary = "Récupérer toutes les ordonnances",
            description = "Récupère toutes les ordonnances. Accessible uniquement aux admins."
    )
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PrescriptionResponseDTO>> getAllPrescriptions() {
        List<PrescriptionResponseDTO> prescriptions = prescriptionService.getAllPrescriptions();
        return ResponseEntity.ok(prescriptions);
    }
}

