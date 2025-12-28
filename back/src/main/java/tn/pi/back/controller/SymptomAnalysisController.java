package tn.pi.back.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.pi.back.dto.SymptomAnalysisRequestDTO;
import tn.pi.back.dto.SymptomAnalysisResponseDTO;

import java.util.List;

@RestController
@RequestMapping("/api/symptom-analysis")
@RequiredArgsConstructor
@Tag(name = "Symptom Assistant", description = "API pour l'assistant d'analyse de symptômes")
public class SymptomAnalysisController {
    
    private final tn.pi.back.service.SymptomAnalysisService symptomAnalysisService;
    
    @Operation(
            summary = "Analyser des symptômes",
            description = "Analyse la description des symptômes du patient, extrait des informations structurées, " +
                         "détecte les red flags, suggère des spécialités et génère un résumé. " +
                         "⚠️ IMPORTANT: Cette analyse n'est pas un diagnostic médical."
    )
    @PostMapping
    @PreAuthorize("hasAnyRole('PATIENT', 'ADMIN')")
    public ResponseEntity<SymptomAnalysisResponseDTO> analyzeSymptoms(
            @Valid @RequestBody SymptomAnalysisRequestDTO request) {
        SymptomAnalysisResponseDTO response = symptomAnalysisService.analyzeSymptoms(request);
        return ResponseEntity.ok(response);
    }
    
    @Operation(
            summary = "Récupérer une analyse par ID",
            description = "Récupère une analyse de symptômes précédente par son ID"
    )
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'ADMIN')")
    public ResponseEntity<SymptomAnalysisResponseDTO> getAnalysisById(
            @Parameter(description = "ID de l'analyse", example = "1")
            @PathVariable Long id) {
        SymptomAnalysisResponseDTO response = symptomAnalysisService.getAnalysisById(id);
        return ResponseEntity.ok(response);
    }
    
    @Operation(
            summary = "Récupérer les analyses d'un patient",
            description = "Récupère toutes les analyses de symptômes d'un patient"
    )
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'ADMIN')")
    public ResponseEntity<List<SymptomAnalysisResponseDTO>> getPatientAnalyses(
            @Parameter(description = "ID du patient", example = "1")
            @PathVariable Long patientId) {
        List<SymptomAnalysisResponseDTO> analyses = symptomAnalysisService.getPatientAnalyses(patientId);
        return ResponseEntity.ok(analyses);
    }
    
    @Operation(
            summary = "Récupérer l'analyse liée à un rendez-vous",
            description = "Récupère l'analyse de symptômes liée à un rendez-vous spécifique"
    )
    @GetMapping("/appointment/{appointmentId}")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'ADMIN')")
    public ResponseEntity<SymptomAnalysisResponseDTO> getAnalysisByAppointmentId(
            @Parameter(description = "ID du rendez-vous", example = "1")
            @PathVariable Long appointmentId) {
        SymptomAnalysisResponseDTO response = symptomAnalysisService.getAnalysisByAppointmentId(appointmentId);
        return ResponseEntity.ok(response);
    }
}

