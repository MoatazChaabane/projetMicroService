package tn.pi.back.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.pi.back.dto.*;
import tn.pi.back.model.MedicalAttachment.AttachmentType;
import tn.pi.back.service.MedicalRecordService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/medical-records")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Dossiers Médicaux", description = "API pour la gestion des dossiers médicaux")
public class MedicalRecordController {
    
    private final MedicalRecordService medicalRecordService;
    
    @Operation(
            summary = "Récupérer le dossier médical d'un patient",
            description = "Récupère le dossier médical d'un patient. " +
                         "Les patients peuvent voir leur propre dossier, les docteurs ceux de leurs patients, et les admins tous les dossiers."
    )
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'ADMIN')")
    public ResponseEntity<MedicalRecordResponseDTO> getMedicalRecordByPatientId(
            @Parameter(description = "ID du patient", example = "1")
            @PathVariable Long patientId) {
        MedicalRecordResponseDTO record = medicalRecordService.getMedicalRecordByPatientId(patientId);
        return ResponseEntity.ok(record);
    }
    
    @Operation(
            summary = "Créer un dossier médical",
            description = "Crée un nouveau dossier médical pour un patient. Accessible uniquement aux docteurs et admins."
    )
    @PostMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<MedicalRecordResponseDTO> createMedicalRecord(
            @Parameter(description = "ID du patient", example = "1")
            @PathVariable Long patientId) {
        MedicalRecordResponseDTO record = medicalRecordService.createMedicalRecord(patientId);
        return new ResponseEntity<>(record, HttpStatus.CREATED);
    }
    
    @Operation(
            summary = "Récupérer la timeline (historique chronologique)",
            description = "Récupère l'historique chronologique complet d'un dossier médical (consultations + pièces jointes)."
    )
    @GetMapping("/patient/{patientId}/timeline")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'ADMIN')")
    public ResponseEntity<List<TimelineItemDTO>> getTimeline(
            @Parameter(description = "ID du patient", example = "1")
            @PathVariable Long patientId) {
        List<TimelineItemDTO> timeline = medicalRecordService.getTimeline(patientId);
        return ResponseEntity.ok(timeline);
    }
    
    @Operation(
            summary = "Rechercher dans l'historique",
            description = "Recherche dans l'historique du dossier médical (consultations et pièces jointes)."
    )
    @GetMapping("/patient/{patientId}/search")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'ADMIN')")
    public ResponseEntity<List<TimelineItemDTO>> searchInHistory(
            @Parameter(description = "ID du patient", example = "1")
            @PathVariable Long patientId,
            @Parameter(description = "Terme de recherche", example = "rhume")
            @RequestParam String q) {
        List<TimelineItemDTO> results = medicalRecordService.searchInHistory(patientId, q);
        return ResponseEntity.ok(results);
    }
    
    @Operation(
            summary = "Ajouter une consultation",
            description = "Ajoute une nouvelle consultation au dossier médical. Accessible uniquement aux docteurs et admins."
    )
    @PostMapping("/visits")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<VisitResponseDTO> addVisit(@Valid @RequestBody VisitRequestDTO requestDTO) {
        VisitResponseDTO visit = medicalRecordService.addVisit(requestDTO);
        return new ResponseEntity<>(visit, HttpStatus.CREATED);
    }
    
    @Operation(
            summary = "Ajouter une pièce jointe",
            description = "Ajoute une pièce jointe (PDF, image, etc.) au dossier médical. Accessible uniquement aux docteurs et admins."
    )
    @PostMapping(value = "/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<AttachmentResponseDTO> addAttachment(
            @RequestParam("medicalRecordId") Long medicalRecordId,
            @RequestParam(value = "doctorId", required = false) Long doctorId,
            @RequestParam("attachmentType") AttachmentType attachmentType,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("file") MultipartFile file) {
        try {
            AttachmentRequestDTO requestDTO = AttachmentRequestDTO.builder()
                    .medicalRecordId(medicalRecordId)
                    .doctorId(doctorId)
                    .attachmentType(attachmentType)
                    .description(description)
                    .file(file)
                    .build();
            
            AttachmentResponseDTO attachment = medicalRecordService.addAttachment(requestDTO);
            return new ResponseEntity<>(attachment, HttpStatus.CREATED);
        } catch (IOException e) {
            log.error("Erreur lors de l'ajout de la pièce jointe", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @Operation(
            summary = "Télécharger une pièce jointe",
            description = "Télécharge une pièce jointe du dossier médical."
    )
    @GetMapping("/attachments/{attachmentId}/download")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'ADMIN')")
    public ResponseEntity<byte[]> downloadAttachment(
            @Parameter(description = "ID de la pièce jointe", example = "1")
            @PathVariable Long attachmentId) {
        try {
            byte[] fileContent = medicalRecordService.downloadAttachment(attachmentId);
            AttachmentResponseDTO attachmentInfo = medicalRecordService.getAttachmentInfo(attachmentId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(attachmentInfo.getFileType()));
            headers.setContentDispositionFormData("attachment", attachmentInfo.getFileName());
            headers.setContentLength(fileContent.length);
            
            return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
        } catch (IOException e) {
            log.error("Erreur lors du téléchargement de la pièce jointe", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @Operation(
            summary = "Exporter le dossier médical en PDF",
            description = "Exporte le dossier médical complet (consultations + pièces jointes) en PDF. " +
                         "Les patients peuvent exporter leur propre dossier, les docteurs ceux de leurs patients, et les admins tous les dossiers."
    )
    @GetMapping("/patient/{patientId}/export-pdf")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'ADMIN')")
    public ResponseEntity<byte[]> exportMedicalRecordPDF(
            @Parameter(description = "ID du patient", example = "1")
            @PathVariable Long patientId) {
        try {
            byte[] pdfContent = medicalRecordService.exportMedicalRecordPDF(patientId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "dossier-medical-" + patientId + ".pdf");
            headers.setContentLength(pdfContent.length);
            
            return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Erreur lors de l'export PDF du dossier médical", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

