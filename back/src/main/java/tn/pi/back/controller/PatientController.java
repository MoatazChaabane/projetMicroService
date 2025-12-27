package tn.pi.back.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.pi.back.dto.PageResponse;
import tn.pi.back.dto.PatientRequestDTO;
import tn.pi.back.dto.PatientResponseDTO;
import tn.pi.back.service.PatientService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
@Tag(name = "Gestion des Patients", description = "API pour la gestion CRUD des patients")
@SecurityRequirement(name = "bearerAuth")
public class PatientController {
    
    private final PatientService patientService;
    
    @Operation(
            summary = "Créer un nouveau patient",
            description = "Crée un nouveau patient avec les informations fournies"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Patient créé avec succès",
                    content = @Content(schema = @Schema(implementation = PatientResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Erreur de validation")
    })
    @PostMapping
    public ResponseEntity<Map<String, Object>> createPatient(@Valid @RequestBody PatientRequestDTO requestDTO) {
        PatientResponseDTO patient = patientService.createPatient(requestDTO);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Patient créé avec succès");
        response.put("patient", patient);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @Operation(
            summary = "Récupérer un patient par ID",
            description = "Récupère les informations d'un patient spécifique par son ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Patient trouvé",
                    content = @Content(schema = @Schema(implementation = PatientResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Patient non trouvé")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PatientResponseDTO> getPatientById(@PathVariable Long id) {
        PatientResponseDTO patient = patientService.getPatientById(id);
        return ResponseEntity.ok(patient);
    }
    
    @Operation(
            summary = "Liste tous les patients avec pagination",
            description = "Récupère la liste paginée de tous les patients. Paramètres optionnels: page, size, sortBy, sortDir"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès")
    })
    @GetMapping
    public ResponseEntity<PageResponse<PatientResponseDTO>> getAllPatients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        PageResponse<PatientResponseDTO> patients = patientService.getAllPatients(page, size, sortBy, sortDir);
        return ResponseEntity.ok(patients);
    }
    
    @Operation(
            summary = "Rechercher des patients",
            description = "Recherche des patients par nom, prénom ou téléphone avec pagination"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recherche effectuée avec succès")
    })
    @GetMapping("/search")
    public ResponseEntity<PageResponse<PatientResponseDTO>> searchPatients(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        PageResponse<PatientResponseDTO> patients = patientService.searchPatients(search, page, size, sortBy, sortDir);
        return ResponseEntity.ok(patients);
    }
    
    @Operation(
            summary = "Modifier un patient",
            description = "Met à jour les informations d'un patient existant"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Patient mis à jour avec succès"),
            @ApiResponse(responseCode = "404", description = "Patient non trouvé"),
            @ApiResponse(responseCode = "400", description = "Erreur de validation")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updatePatient(
            @PathVariable Long id,
            @Valid @RequestBody PatientRequestDTO requestDTO) {
        PatientResponseDTO patient = patientService.updatePatient(id, requestDTO);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Patient mis à jour avec succès");
        response.put("patient", patient);
        return ResponseEntity.ok(response);
    }
    
    @Operation(
            summary = "Supprimer un patient (soft delete)",
            description = "Supprime un patient de manière logique (soft delete). Le patient n'est pas physiquement supprimé de la base de données."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Patient supprimé avec succès"),
            @ApiResponse(responseCode = "404", description = "Patient non trouvé")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Patient supprimé avec succès");
        return ResponseEntity.ok(response);
    }
    
    @Operation(
            summary = "Compter le nombre de patients",
            description = "Retourne le nombre total de patients non supprimés"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Nombre de patients")
    })
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> countPatients() {
        long count = patientService.countPatients();
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }
}

