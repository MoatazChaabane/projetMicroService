package tn.pi.back.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tn.pi.back.dto.*;
import tn.pi.back.exception.ResourceNotFoundException;
import tn.pi.back.model.*;
import tn.pi.back.repository.*;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicalRecordService {
    
    private final MedicalRecordRepository medicalRecordRepository;
    private final VisitRepository visitRepository;
    private final MedicalAttachmentRepository attachmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    
    @Value("${medical.records.attachments.directory:medical-records/attachments}")
    private String attachmentsDirectory;
    
    @Value("${medical.records.attachments.max-size:10485760}") // 10MB par défaut
    private long maxFileSize;
    
    @Transactional(readOnly = true)
    public MedicalRecordResponseDTO getMedicalRecordByPatientId(Long patientId) {
        MedicalRecord record = medicalRecordRepository.findByPatientId(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier médical non trouvé pour le patient ID: " + patientId));
        
        return mapToResponseDTO(record);
    }
    
    @Transactional
    public MedicalRecordResponseDTO createMedicalRecord(Long patientId) {
        // Vérifier que le patient existe
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient non trouvé avec l'ID: " + patientId));
        
        // Vérifier qu'un dossier n'existe pas déjà
        if (medicalRecordRepository.existsByPatientId(patientId)) {
            throw new RuntimeException("Un dossier médical existe déjà pour ce patient");
        }
        
        MedicalRecord record = MedicalRecord.builder()
                .patient(patient)
                .build();
        
        MedicalRecord saved = medicalRecordRepository.save(record);
        log.info("Dossier médical créé: ID={}, Patient={}", saved.getId(), patientId);
        
        return mapToResponseDTO(saved);
    }
    
    @Transactional
    public VisitResponseDTO addVisit(VisitRequestDTO requestDTO) {
        MedicalRecord record = medicalRecordRepository.findById(requestDTO.getMedicalRecordId())
                .orElseThrow(() -> new ResourceNotFoundException("Dossier médical non trouvé avec l'ID: " + requestDTO.getMedicalRecordId()));
        
        Doctor doctor = doctorRepository.findById(requestDTO.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Docteur non trouvé avec l'ID: " + requestDTO.getDoctorId()));
        
        Visit visit = Visit.builder()
                .medicalRecord(record)
                .doctor(doctor)
                .visitDate(requestDTO.getVisitDate() != null ? requestDTO.getVisitDate() : LocalDate.now())
                .visitTime(requestDTO.getVisitTime())
                .reason(requestDTO.getReason())
                .symptoms(requestDTO.getSymptoms())
                .diagnosis(requestDTO.getDiagnosis())
                .treatment(requestDTO.getTreatment())
                .notes(requestDTO.getNotes())
                .build();
        
        Visit saved = visitRepository.save(visit);
        log.info("Consultation ajoutée: ID={}, Dossier={}, Docteur={}", saved.getId(), record.getId(), doctor.getId());
        
        return mapToVisitResponseDTO(saved);
    }
    
    @Transactional
    public AttachmentResponseDTO addAttachment(AttachmentRequestDTO requestDTO) throws IOException {
        MedicalRecord record = medicalRecordRepository.findById(requestDTO.getMedicalRecordId())
                .orElseThrow(() -> new ResourceNotFoundException("Dossier médical non trouvé avec l'ID: " + requestDTO.getMedicalRecordId()));
        
        MultipartFile file = requestDTO.getFile();
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Le fichier est vide");
        }
        
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("Le fichier dépasse la taille maximale autorisée (" + (maxFileSize / 1024 / 1024) + "MB)");
        }
        
        Doctor doctor = null;
        if (requestDTO.getDoctorId() != null) {
            doctor = doctorRepository.findById(requestDTO.getDoctorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Docteur non trouvé avec l'ID: " + requestDTO.getDoctorId()));
        }
        
        // Créer le répertoire si nécessaire
        Path dir = Paths.get(attachmentsDirectory);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        
        // Générer un nom de fichier unique
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf('.'));
        String uniqueFilename = System.currentTimeMillis() + "_" + UUID.randomUUID().toString() + fileExtension;
        Path filePath = dir.resolve(uniqueFilename);
        
        // Sauvegarder le fichier
        Files.copy(file.getInputStream(), filePath);
        
        MedicalAttachment attachment = MedicalAttachment.builder()
                .medicalRecord(record)
                .doctor(doctor)
                .fileName(originalFilename)
                .fileType(file.getContentType())
                .attachmentType(requestDTO.getAttachmentType())
                .description(requestDTO.getDescription())
                .fileSize(file.getSize())
                .filePath(filePath.toString())
                .build();
        
        MedicalAttachment saved = attachmentRepository.save(attachment);
        log.info("Pièce jointe ajoutée: ID={}, Dossier={}, Fichier={}", saved.getId(), record.getId(), originalFilename);
        
        return mapToAttachmentResponseDTO(saved);
    }
    
    @Transactional(readOnly = true)
    public List<TimelineItemDTO> getTimeline(Long patientId) {
        MedicalRecord record = medicalRecordRepository.findByPatientId(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier médical non trouvé pour le patient ID: " + patientId));
        
        List<TimelineItemDTO> timeline = new ArrayList<>();
        
        // Ajouter les consultations
        List<Visit> visits = visitRepository.findByMedicalRecordIdOrderByVisitDateDescVisitTimeDesc(record.getId());
        for (Visit visit : visits) {
            timeline.add(TimelineItemDTO.builder()
                    .type(TimelineItemDTO.TimelineItemType.VISIT)
                    .id(visit.getId())
                    .dateTime(visit.getVisitDate().atTime(visit.getVisitTime() != null ? visit.getVisitTime() : visit.getCreatedAt().toLocalTime()))
                    .visitDate(visit.getVisitDate())
                    .visitTime(visit.getVisitTime())
                    .title("Consultation - " + (visit.getReason() != null ? visit.getReason() : "Sans motif"))
                    .doctorName("Dr. " + visit.getDoctor().getUser().getFirstName() + " " + visit.getDoctor().getUser().getLastName())
                    .doctorId(visit.getDoctor().getId())
                    .reason(visit.getReason())
                    .diagnosis(visit.getDiagnosis())
                    .build());
        }
        
        // Ajouter les pièces jointes
        List<MedicalAttachment> attachments = attachmentRepository.findByMedicalRecordIdOrderByCreatedAtDesc(record.getId());
        for (MedicalAttachment attachment : attachments) {
            String doctorName = attachment.getDoctor() != null 
                    ? "Dr. " + attachment.getDoctor().getUser().getFirstName() + " " + attachment.getDoctor().getUser().getLastName()
                    : "Système";
            
            timeline.add(TimelineItemDTO.builder()
                    .type(TimelineItemDTO.TimelineItemType.ATTACHMENT)
                    .id(attachment.getId())
                    .dateTime(attachment.getCreatedAt())
                    .title(attachment.getFileName())
                    .doctorName(doctorName)
                    .doctorId(attachment.getDoctor() != null ? attachment.getDoctor().getId() : null)
                    .fileName(attachment.getFileName())
                    .attachmentType(attachment.getAttachmentType())
                    .description(attachment.getDescription())
                    .build());
        }
        
        // Trier par date décroissante
        timeline.sort((a, b) -> b.getDateTime().compareTo(a.getDateTime()));
        
        return timeline;
    }
    
    @Transactional(readOnly = true)
    public List<TimelineItemDTO> searchInHistory(Long patientId, String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getTimeline(patientId);
        }
        
        MedicalRecord record = medicalRecordRepository.findByPatientId(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier médical non trouvé pour le patient ID: " + patientId));
        
        List<TimelineItemDTO> timeline = new ArrayList<>();
        
        // Rechercher dans les consultations
        List<Visit> visits = visitRepository.searchInVisits(patientId, searchTerm.trim());
        for (Visit visit : visits) {
            timeline.add(TimelineItemDTO.builder()
                    .type(TimelineItemDTO.TimelineItemType.VISIT)
                    .id(visit.getId())
                    .dateTime(visit.getVisitDate().atTime(visit.getVisitTime() != null ? visit.getVisitTime() : visit.getCreatedAt().toLocalTime()))
                    .visitDate(visit.getVisitDate())
                    .visitTime(visit.getVisitTime())
                    .title("Consultation - " + (visit.getReason() != null ? visit.getReason() : "Sans motif"))
                    .doctorName("Dr. " + visit.getDoctor().getUser().getFirstName() + " " + visit.getDoctor().getUser().getLastName())
                    .doctorId(visit.getDoctor().getId())
                    .reason(visit.getReason())
                    .diagnosis(visit.getDiagnosis())
                    .build());
        }
        
        // Rechercher dans les pièces jointes
        List<MedicalAttachment> attachments = attachmentRepository.searchInAttachments(patientId, searchTerm.trim());
        for (MedicalAttachment attachment : attachments) {
            String doctorName = attachment.getDoctor() != null 
                    ? "Dr. " + attachment.getDoctor().getUser().getFirstName() + " " + attachment.getDoctor().getUser().getLastName()
                    : "Système";
            
            timeline.add(TimelineItemDTO.builder()
                    .type(TimelineItemDTO.TimelineItemType.ATTACHMENT)
                    .id(attachment.getId())
                    .dateTime(attachment.getCreatedAt())
                    .title(attachment.getFileName())
                    .doctorName(doctorName)
                    .doctorId(attachment.getDoctor() != null ? attachment.getDoctor().getId() : null)
                    .fileName(attachment.getFileName())
                    .attachmentType(attachment.getAttachmentType())
                    .description(attachment.getDescription())
                    .build());
        }
        
        // Trier par date décroissante
        timeline.sort((a, b) -> b.getDateTime().compareTo(a.getDateTime()));
        
        return timeline;
    }
    
    @Transactional(readOnly = true)
    public byte[] downloadAttachment(Long attachmentId) throws IOException {
        MedicalAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Pièce jointe non trouvée avec l'ID: " + attachmentId));
        
        if (attachment.getFilePath() == null) {
            throw new RuntimeException("Chemin du fichier non disponible");
        }
        
        Path filePath = Paths.get(attachment.getFilePath());
        if (!Files.exists(filePath)) {
            throw new ResourceNotFoundException("Fichier non trouvé sur le disque");
        }
        
        return Files.readAllBytes(filePath);
    }
    
    @Transactional(readOnly = true)
    public AttachmentResponseDTO getAttachmentInfo(Long attachmentId) {
        MedicalAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Pièce jointe non trouvée avec l'ID: " + attachmentId));
        
        return mapToAttachmentResponseDTO(attachment);
    }
    
    @Transactional(readOnly = true)
    public byte[] exportMedicalRecordPDF(Long patientId) throws DocumentException, IOException {
        MedicalRecord record = medicalRecordRepository.findByPatientId(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier médical non trouvé pour le patient ID: " + patientId));
        
        Patient patient = record.getPatient();
        
        // Créer le document PDF
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, baos);
        
        document.open();
        
        // Fonts
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, BaseColor.BLACK);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
        Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.GRAY);
        
        // En-tête
        Paragraph title = new Paragraph("DOSSIER MÉDICAL", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);
        
        // Informations patient
        Paragraph patientInfo = new Paragraph();
        patientInfo.add(new Chunk("Patient: ", headerFont));
        patientInfo.add(new Chunk(patient.getPrenom() + " " + patient.getNom(), normalFont));
        patientInfo.add(Chunk.NEWLINE);
        patientInfo.add(new Chunk("Date de naissance: ", headerFont));
        patientInfo.add(new Chunk(patient.getDateNaissance().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), normalFont));
        patientInfo.add(Chunk.NEWLINE);
        patientInfo.add(new Chunk("Sexe: ", headerFont));
        patientInfo.add(new Chunk(patient.getSexe().name(), normalFont));
        patientInfo.setSpacingAfter(15);
        document.add(patientInfo);
        
        // Timeline - Consultations
        List<Visit> visits = visitRepository.findByMedicalRecordIdOrderByVisitDateDescVisitTimeDesc(record.getId());
        if (!visits.isEmpty()) {
            Paragraph visitsTitle = new Paragraph("Consultations", headerFont);
            visitsTitle.setSpacingBefore(20);
            visitsTitle.setSpacingAfter(10);
            document.add(visitsTitle);
            
            for (Visit visit : visits) {
                // Date et docteur
                Paragraph visitHeader = new Paragraph();
                visitHeader.add(new Chunk("Date: ", headerFont));
                visitHeader.add(new Chunk(visit.getVisitDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), normalFont));
                if (visit.getVisitTime() != null) {
                    visitHeader.add(new Chunk(" à " + visit.getVisitTime().format(DateTimeFormatter.ofPattern("HH:mm")), normalFont));
                }
                visitHeader.add(Chunk.NEWLINE);
                visitHeader.add(new Chunk("Docteur: ", headerFont));
                visitHeader.add(new Chunk("Dr. " + visit.getDoctor().getUser().getFirstName() + " " + visit.getDoctor().getUser().getLastName(), normalFont));
                visitHeader.setSpacingAfter(5);
                document.add(visitHeader);
                
                // Motif
                if (visit.getReason() != null && !visit.getReason().trim().isEmpty()) {
                    Paragraph reason = new Paragraph("Motif: " + visit.getReason(), normalFont);
                    reason.setSpacingAfter(3);
                    document.add(reason);
                }
                
                // Symptômes
                if (visit.getSymptoms() != null && !visit.getSymptoms().trim().isEmpty()) {
                    Paragraph symptoms = new Paragraph("Symptômes: " + visit.getSymptoms(), normalFont);
                    symptoms.setSpacingAfter(3);
                    document.add(symptoms);
                }
                
                // Diagnostic
                if (visit.getDiagnosis() != null && !visit.getDiagnosis().trim().isEmpty()) {
                    Paragraph diagnosis = new Paragraph("Diagnostic: " + visit.getDiagnosis(), normalFont);
                    diagnosis.setSpacingAfter(3);
                    document.add(diagnosis);
                }
                
                // Traitement
                if (visit.getTreatment() != null && !visit.getTreatment().trim().isEmpty()) {
                    Paragraph treatment = new Paragraph("Traitement: " + visit.getTreatment(), normalFont);
                    treatment.setSpacingAfter(3);
                    document.add(treatment);
                }
                
                // Notes
                if (visit.getNotes() != null && !visit.getNotes().trim().isEmpty()) {
                    Paragraph notes = new Paragraph("Notes: " + visit.getNotes(), normalFont);
                    notes.setSpacingAfter(10);
                    document.add(notes);
                }
                
                document.add(new Paragraph("────────────────────────────────────────", smallFont));
                document.add(new Paragraph(" "));
            }
        }
        
        // Pièces jointes
        List<MedicalAttachment> attachments = attachmentRepository.findByMedicalRecordIdOrderByCreatedAtDesc(record.getId());
        if (!attachments.isEmpty()) {
            Paragraph attachmentsTitle = new Paragraph("Pièces Jointes", headerFont);
            attachmentsTitle.setSpacingBefore(20);
            attachmentsTitle.setSpacingAfter(10);
            document.add(attachmentsTitle);
            
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3, 2, 2, 3});
            
            // En-têtes
            addTableHeader(table, "Fichier", headerFont);
            addTableHeader(table, "Type", headerFont);
            addTableHeader(table, "Date", headerFont);
            addTableHeader(table, "Description", headerFont);
            
            // Lignes
            for (MedicalAttachment attachment : attachments) {
                addTableCell(table, attachment.getFileName(), normalFont);
                addTableCell(table, attachment.getAttachmentType().name(), normalFont);
                addTableCell(table, attachment.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), normalFont);
                addTableCell(table, attachment.getDescription() != null ? attachment.getDescription() : "-", normalFont);
            }
            
            document.add(table);
        }
        
        // Notes générales
        if (record.getNotes() != null && !record.getNotes().trim().isEmpty()) {
            Paragraph notesTitle = new Paragraph("Notes Générales", headerFont);
            notesTitle.setSpacingBefore(20);
            notesTitle.setSpacingAfter(10);
            document.add(notesTitle);
            
            Paragraph notes = new Paragraph(record.getNotes(), normalFont);
            document.add(notes);
        }
        
        // Pied de page
        Paragraph footer = new Paragraph();
        footer.add(new Chunk("Document généré le: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")), smallFont));
        footer.setSpacingBefore(30);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
        
        document.close();
        
        return baos.toByteArray();
    }
    
    private void addTableHeader(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cell.setPadding(8);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }
    
    private void addTableCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        table.addCell(cell);
    }
    
    private MedicalRecordResponseDTO mapToResponseDTO(MedicalRecord record) {
        Long visitsCount = visitRepository.countByMedicalRecordId(record.getId());
        Long attachmentsCount = attachmentRepository.countByMedicalRecordId(record.getId());
        
        return MedicalRecordResponseDTO.builder()
                .id(record.getId())
                .patientId(record.getPatient().getId())
                .patientName(record.getPatient().getPrenom() + " " + record.getPatient().getNom())
                .notes(record.getNotes())
                .visitsCount(visitsCount)
                .attachmentsCount(attachmentsCount)
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }
    
    private VisitResponseDTO mapToVisitResponseDTO(Visit visit) {
        return VisitResponseDTO.builder()
                .id(visit.getId())
                .medicalRecordId(visit.getMedicalRecord().getId())
                .doctorId(visit.getDoctor().getId())
                .doctorName("Dr. " + visit.getDoctor().getUser().getPrenom() + " " + visit.getDoctor().getUser().getNom())
                .visitDate(visit.getVisitDate())
                .visitTime(visit.getVisitTime())
                .reason(visit.getReason())
                .symptoms(visit.getSymptoms())
                .diagnosis(visit.getDiagnosis())
                .treatment(visit.getTreatment())
                .notes(visit.getNotes())
                .createdAt(visit.getCreatedAt())
                .updatedAt(visit.getUpdatedAt())
                .build();
    }
    
    private AttachmentResponseDTO mapToAttachmentResponseDTO(MedicalAttachment attachment) {
        return AttachmentResponseDTO.builder()
                .id(attachment.getId())
                .medicalRecordId(attachment.getMedicalRecord().getId())
                .doctorId(attachment.getDoctor() != null ? attachment.getDoctor().getId() : null)
                .doctorName(attachment.getDoctor() != null 
                        ? "Dr. " + attachment.getDoctor().getUser().getFirstName() + " " + attachment.getDoctor().getUser().getLastName()
                        : "Système")
                .fileName(attachment.getFileName())
                .fileType(attachment.getFileType())
                .attachmentType(attachment.getAttachmentType())
                .description(attachment.getDescription())
                .fileSize(attachment.getFileSize())
                .downloadUrl("/api/medical-records/attachments/" + attachment.getId() + "/download")
                .createdAt(attachment.getCreatedAt())
                .build();
    }
}

