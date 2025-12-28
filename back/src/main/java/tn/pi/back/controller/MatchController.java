package tn.pi.back.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.pi.back.dto.DoctorMatchRequestDTO;
import tn.pi.back.dto.DoctorMatchResponseDTO;
import tn.pi.back.service.DoctorService;

import java.util.List;

@RestController
@RequestMapping("/api/match")
@RequiredArgsConstructor
@Tag(name = "Matching", description = "API pour le matching intelligent de docteurs")
public class MatchController {
    
    private final DoctorService doctorService;
    
    @Operation(
            summary = "Rechercher des docteurs par matching intelligent",
            description = "Trouve les docteurs les plus pertinents basés sur les symptômes, " +
                         "la position géographique, la distance et la disponibilité. " +
                         "Les résultats sont triés par score de pertinence (symptômes 40%, distance 30%, disponibilité 30%)."
    )
    @PostMapping("/doctors")
    @PreAuthorize("hasAnyRole('PATIENT', 'ADMIN')")
    public ResponseEntity<List<DoctorMatchResponseDTO>> matchDoctors(
            @Valid @RequestBody DoctorMatchRequestDTO matchRequest) {
        List<DoctorMatchResponseDTO> matches = doctorService.matchDoctors(matchRequest);
        return ResponseEntity.ok(matches);
    }
}

