package tn.pi.back.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.pi.back.dto.*;
import tn.pi.back.exception.ResourceNotFoundException;
import tn.pi.back.model.*;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PrescriptionService {
    
    private final tn.pi.back.repository.PrescriptionRepository prescriptionRepository;
    private final tn.pi.back.repository.PatientRepository patientRepository;
    private final tn.pi.back.repository.DoctorRepository doctorRepository;
    
    @Value("${prescription.pdf.directory:prescriptions/pdfs}")
    private String pdfDirectory;
    
    @Value("${prescription.pdf.base-url:/prescriptions/pdfs}")
    private String pdfBaseUrl;
    
    @Transactional
    public PrescriptionResponseDTO createPrescription(PrescriptionRequestDTO requestDTO) {

        Patient patient = patientRepository.findByIdAndDeletedFalse(requestDTO.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient non trouvé avec l'ID: " + requestDTO.getPatientId()));

        Doctor doctor = doctorRepository.findByIdAndDeletedFalse(requestDTO.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Docteur non trouvé avec l'ID: " + requestDTO.getDoctorId()));

        List<Medication> medications = requestDTO.getMedications().stream()
                .map(dto -> Medication.builder()
                        .name(dto.getName())
                        .dosage(dto.getDosage())
                        .frequency(dto.getFrequency())
                        .duration(dto.getDuration())
                        .instructions(dto.getInstructions())
                        .build())
                .collect(Collectors.toList());

        Prescription prescription = Prescription.builder()
                .patient(patient)
                .doctor(doctor)
                .medications(medications)
                .instructions(requestDTO.getInstructions())
                .date(requestDTO.getDate() != null ? requestDTO.getDate() : LocalDate.now())
                .build();
        
        Prescription saved = prescriptionRepository.save(prescription);

        String pdfUrl = generatePDF(saved);
        saved.setPdfUrl(pdfUrl);

        String signatureHash = generateSignature(saved);
        String signatureMetadata = generateSignatureMetadata(saved);
        saved.setSignatureHash(signatureHash);
        saved.setSignatureMetadata(signatureMetadata);
        
        saved = prescriptionRepository.save(saved);
        
        log.info("Ordonnance créée: ID={}, Patient={}, Docteur={}, PDF={}", 
                saved.getId(), patient.getId(), doctor.getId(), pdfUrl);
        
        return mapToResponseDTO(saved);
    }
    
    @Transactional(readOnly = true)
    public PrescriptionResponseDTO getPrescriptionById(Long id) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ordonnance non trouvée avec l'ID: " + id));
        return mapToResponseDTO(prescription);
    }
    
    @Transactional(readOnly = true)
    public PageResponse<PrescriptionResponseDTO> getPatientPrescriptions(Long patientId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
        Page<Prescription> prescriptions = prescriptionRepository.findByPatientIdOrderByDateDesc(patientId, pageable);
        return mapToPageResponse(prescriptions);
    }
    
    @Transactional(readOnly = true)
    public PageResponse<PrescriptionResponseDTO> getDoctorPrescriptions(Long doctorId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
        Page<Prescription> prescriptions = prescriptionRepository.findByDoctorIdOrderByDateDesc(doctorId, pageable);
        return mapToPageResponse(prescriptions);
    }
    
    @Transactional(readOnly = true)
    public List<PrescriptionResponseDTO> getAllPrescriptions() {
        List<Prescription> prescriptions = prescriptionRepository.findAll(Sort.by("date").descending());
        return prescriptions.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    private String generatePDF(Prescription prescription) {
        try {

            Path dir = Paths.get(pdfDirectory);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }

            String fileName = "prescription-" + prescription.getId() + "-" + UUID.randomUUID().toString() + ".pdf";
            String filePath = dir.resolve(fileName).toString();

            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, BaseColor.BLACK);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
            
            Paragraph title = new Paragraph("ORDONNANCE MÉDICALE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            Paragraph doctorInfo = new Paragraph();
            doctorInfo.add(new Chunk("Docteur: ", headerFont));
            doctorInfo.add(new Chunk("Dr. " + prescription.getDoctor().getUser().getFirstName() + " " + 
                                    prescription.getDoctor().getUser().getLastName(), normalFont));
            doctorInfo.add(Chunk.NEWLINE);
            doctorInfo.add(new Chunk("Spécialité: ", headerFont));
            doctorInfo.add(new Chunk(prescription.getDoctor().getSpecialite().name().replace("_", " "), normalFont));
            doctorInfo.add(Chunk.NEWLINE);
            doctorInfo.add(new Chunk("Date: ", headerFont));
            doctorInfo.add(new Chunk(prescription.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), normalFont));
            doctorInfo.setSpacingAfter(15);
            document.add(doctorInfo);

            Paragraph patientInfo = new Paragraph();
            patientInfo.add(new Chunk("Patient: ", headerFont));
            patientInfo.add(new Chunk(prescription.getPatient().getPrenom() + " " + prescription.getPatient().getNom(), normalFont));
            patientInfo.setSpacingAfter(15);
            document.add(patientInfo);

            Paragraph medTitle = new Paragraph("Médicaments:", headerFont);
            medTitle.setSpacingAfter(10);
            document.add(medTitle);
            
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3, 2, 2, 2, 3});

            addTableHeader(table, "Médicament", headerFont);
            addTableHeader(table, "Dosage", headerFont);
            addTableHeader(table, "Fréquence", headerFont);
            addTableHeader(table, "Durée", headerFont);
            addTableHeader(table, "Instructions", headerFont);

            for (Medication med : prescription.getMedications()) {
                addTableCell(table, med.getName(), normalFont);
                addTableCell(table, med.getDosage(), normalFont);
                addTableCell(table, med.getFrequency(), normalFont);
                addTableCell(table, med.getDuration(), normalFont);
                addTableCell(table, med.getInstructions() != null ? med.getInstructions() : "-", normalFont);
            }
            
            document.add(table);

            if (prescription.getInstructions() != null && !prescription.getInstructions().trim().isEmpty()) {
                Paragraph instructionsTitle = new Paragraph("Instructions générales:", headerFont);
                instructionsTitle.setSpacingBefore(15);
                instructionsTitle.setSpacingAfter(5);
                document.add(instructionsTitle);
                
                Paragraph instructions = new Paragraph(prescription.getInstructions(), normalFont);
                instructions.setSpacingAfter(15);
                document.add(instructions);
            }

            Paragraph signature = new Paragraph();
            signature.add(new Chunk("Signature numérique: ", headerFont));
            signature.add(new Chunk(prescription.getSignatureHash() != null ? prescription.getSignatureHash() : "En attente", normalFont));
            signature.setSpacingBefore(30);
            signature.setAlignment(Element.ALIGN_CENTER);
            document.add(signature);
            
            document.close();

            return pdfBaseUrl + "/" + fileName;
            
        } catch (Exception e) {
            log.error("Erreur lors de la génération du PDF pour l'ordonnance {}", prescription.getId(), e);
            throw new RuntimeException("Erreur lors de la génération du PDF: " + e.getMessage());
        }
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
    
    private String generateSignature(Prescription prescription) {
        try {

            StringBuilder dataToSign = new StringBuilder();
            dataToSign.append("PRESCRIPTION-").append(prescription.getId()).append("-");
            dataToSign.append(prescription.getPatient().getId()).append("-");
            dataToSign.append(prescription.getDoctor().getId()).append("-");
            dataToSign.append(prescription.getDate().toString()).append("-");
            dataToSign.append(prescription.getCreatedAt().toString());

            for (Medication med : prescription.getMedications()) {
                dataToSign.append("-").append(med.getName()).append("-").append(med.getDosage());
            }

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(dataToSign.toString().getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
            
        } catch (NoSuchAlgorithmException e) {
            log.error("Erreur lors de la génération de la signature", e);
            throw new RuntimeException("Erreur lors de la génération de la signature");
        }
    }
    
    private String generateSignatureMetadata(Prescription prescription) {

        StringBuilder metadata = new StringBuilder();
        metadata.append("{");
        metadata.append("\"prescriptionId\":").append(prescription.getId()).append(",");
        metadata.append("\"patientId\":").append(prescription.getPatient().getId()).append(",");
        metadata.append("\"doctorId\":").append(prescription.getDoctor().getId()).append(",");
        metadata.append("\"date\":\"").append(prescription.getDate().toString()).append("\",");
        metadata.append("\"createdAt\":\"").append(prescription.getCreatedAt().toString()).append("\",");
        metadata.append("\"algorithm\":\"SHA-256\",");
        metadata.append("\"version\":\"1.0\"");
        metadata.append("}");
        return metadata.toString();
    }
    
    private PrescriptionResponseDTO mapToResponseDTO(Prescription prescription) {
        List<MedicationDTO> medicationsDTO = prescription.getMedications().stream()
                .map(med -> MedicationDTO.builder()
                        .name(med.getName())
                        .dosage(med.getDosage())
                        .frequency(med.getFrequency())
                        .duration(med.getDuration())
                        .instructions(med.getInstructions())
                        .build())
                .collect(Collectors.toList());
        
        return PrescriptionResponseDTO.builder()
                .id(prescription.getId())
                .patientId(prescription.getPatient().getId())
                .patientName(prescription.getPatient().getPrenom() + " " + prescription.getPatient().getNom())
                .doctorId(prescription.getDoctor().getId())
                .doctorName("Dr. " + prescription.getDoctor().getUser().getFirstName() + " " + 
                           prescription.getDoctor().getUser().getLastName())
                .doctorSpeciality(prescription.getDoctor().getSpecialite().name())
                .medications(medicationsDTO)
                .instructions(prescription.getInstructions())
                .date(prescription.getDate())
                .pdfUrl(prescription.getPdfUrl())
                .signatureHash(prescription.getSignatureHash())
                .createdAt(prescription.getCreatedAt())
                .updatedAt(prescription.getUpdatedAt())
                .build();
    }
    
    private PageResponse<PrescriptionResponseDTO> mapToPageResponse(Page<Prescription> prescriptions) {
        List<PrescriptionResponseDTO> content = prescriptions.getContent().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
        
        return PageResponse.<PrescriptionResponseDTO>builder()
                .content(content)
                .page(prescriptions.getNumber())
                .size(prescriptions.getSize())
                .totalElements(prescriptions.getTotalElements())
                .totalPages(prescriptions.getTotalPages())
                .first(prescriptions.isFirst())
                .last(prescriptions.isLast())
                .build();
    }
}

