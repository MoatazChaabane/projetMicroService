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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.pi.back.dto.*;
import tn.pi.back.service.DoctorService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
@Tag(name = "Gestion des Docteurs", description = "API pour la gestion CRUD des docteurs avec recherche avancée (spécialité, distance, disponibilité)")
@SecurityRequirement(name = "bearerAuth")
public class DoctorController {
    
    private final DoctorService doctorService;
    
    @Operation(
            summary = "Créer un nouveau docteur",
            description = "Crée un profil docteur pour un utilisateur existant. L'utilisateur doit avoir le rôle DOCTOR."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Docteur créé avec succès",
                    content = @Content(schema = @Schema(implementation = DoctorResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Erreur de validation"),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé"),
            @ApiResponse(responseCode = "409", description = "L'utilisateur a déjà un profil docteur")
    })
    @PostMapping
    public ResponseEntity<Map<String, Object>> createDoctor(@Valid @RequestBody DoctorRequestDTO requestDTO) {
        DoctorResponseDTO doctor = doctorService.createDoctor(requestDTO);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Docteur créé avec succès");
        response.put("doctor", doctor);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @Operation(
            summary = "Récupérer un docteur par ID",
            description = "Récupère les informations d'un docteur spécifique par son ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Docteur trouvé",
                    content = @Content(schema = @Schema(implementation = DoctorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Docteur non trouvé")
    })
    @GetMapping("/{id}")
    public ResponseEntity<DoctorResponseDTO> getDoctorById(
            @Parameter(description = "ID du docteur", example = "1")
            @PathVariable Long id) {
        DoctorResponseDTO doctor = doctorService.getDoctorById(id);
        return ResponseEntity.ok(doctor);
    }
    
    @Operation(
            summary = "Récupérer un docteur par ID utilisateur",
            description = "Récupère les informations d'un docteur à partir de l'ID de son compte utilisateur"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Docteur trouvé",
                    content = @Content(schema = @Schema(implementation = DoctorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Docteur non trouvé")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<DoctorResponseDTO> getDoctorByUserId(
            @Parameter(description = "ID de l'utilisateur", example = "1")
            @PathVariable Long userId) {
        DoctorResponseDTO doctor = doctorService.getDoctorByUserId(userId);
        return ResponseEntity.ok(doctor);
    }
    
    @Operation(
            summary = "Lister tous les docteurs avec pagination",
            description = "Récupère une liste paginée de tous les docteurs"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste de docteurs récupérée avec succès",
                    content = @Content(schema = @Schema(implementation = PageResponse.class)))
    })
    @GetMapping
    public ResponseEntity<PageResponse<DoctorResponseDTO>> getAllDoctors(
            @Parameter(description = "Numéro de la page (0-indexé)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Taille de la page", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Champ pour le tri", example = "rating")
            @RequestParam(defaultValue = "rating") String sortBy,
            @Parameter(description = "Direction du tri (asc ou desc)", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDir) {
        PageResponse<DoctorResponseDTO> doctors = doctorService.getAllDoctors(page, size, sortBy, sortDir);
        return ResponseEntity.ok(doctors);
    }
    
    @Operation(
            summary = "Rechercher des docteurs avec filtres avancés",
            description = "Recherche des docteurs par spécialité, distance (GPS), disponibilité, téléconsultation, et rating. " +
                    "Les filtres peuvent être combinés. Exemples : " +
                    "- Spécialité seule : ?specialite=CARDIOLOGIE " +
                    "- Spécialité + Distance : ?specialite=CARDIOLOGIE&latitude=48.8566&longitude=2.3522&rayonKm=10 " +
                    "- Spécialité + Distance + Disponibilité : ?specialite=CARDIOLOGIE&latitude=48.8566&longitude=2.3522&rayonKm=10&date=2023-12-28&heure=10:00"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Résultats de recherche récupérés avec succès",
                    content = @Content(schema = @Schema(implementation = PageResponse.class)))
    })
    @PostMapping("/search")
    public ResponseEntity<PageResponse<DoctorResponseDTO>> searchDoctors(
            @Parameter(description = "Critères de recherche")
            @RequestBody DoctorSearchDTO searchDTO,
            @Parameter(description = "Numéro de la page (0-indexé)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Taille de la page", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Champ pour le tri", example = "rating")
            @RequestParam(defaultValue = "rating") String sortBy,
            @Parameter(description = "Direction du tri (asc ou desc)", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDir) {
        PageResponse<DoctorResponseDTO> doctors = doctorService.searchDoctors(searchDTO, page, size, sortBy, sortDir);
        return ResponseEntity.ok(doctors);
    }
    
    @Operation(
            summary = "Mettre à jour un docteur par ID",
            description = "Met à jour les informations d'un docteur spécifique par son ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Docteur mis à jour avec succès",
                    content = @Content(schema = @Schema(implementation = DoctorResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Erreur de validation"),
            @ApiResponse(responseCode = "404", description = "Docteur non trouvé")
    })
    @PutMapping("/{id}")
    public ResponseEntity<DoctorResponseDTO> updateDoctor(
            @Parameter(description = "ID du docteur à mettre à jour", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody DoctorRequestDTO requestDTO) {
        DoctorResponseDTO updatedDoctor = doctorService.updateDoctor(id, requestDTO);
        return ResponseEntity.ok(updatedDoctor);
    }
    
    @Operation(
            summary = "Supprimer (soft delete) un docteur par ID",
            description = "Marque un docteur comme supprimé (soft delete) par son ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Docteur supprimé avec succès"),
            @ApiResponse(responseCode = "404", description = "Docteur non trouvé")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDoctor(
            @Parameter(description = "ID du docteur à supprimer", example = "1")
            @PathVariable Long id) {
        doctorService.deleteDoctor(id);
        return ResponseEntity.noContent().build();
    }
    
    @Operation(
            summary = "Compter le nombre total de docteurs",
            description = "Retourne le nombre total de docteurs non supprimés"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Nombre de docteurs récupéré avec succès",
                    content = @Content(schema = @Schema(implementation = Long.class)))
    })
    @GetMapping("/count")
    public ResponseEntity<Long> countDoctors() {
        long count = doctorService.countDoctors();
        return ResponseEntity.ok(count);
    }
}

