package tn.pi.back.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.pi.back.dto.PageResponse;
import tn.pi.back.dto.PatientRequestDTO;
import tn.pi.back.dto.PatientResponseDTO;
import tn.pi.back.exception.ResourceNotFoundException;
import tn.pi.back.model.Patient;
import tn.pi.back.model.Sexe;
import tn.pi.back.repository.PatientRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientService {
    
    private final PatientRepository patientRepository;
    
    @Transactional
    public PatientResponseDTO createPatient(PatientRequestDTO requestDTO) {
        Patient patient = Patient.builder()
                .nom(requestDTO.getNom())
                .prenom(requestDTO.getPrenom())
                .dateNaissance(requestDTO.getDateNaissance())
                .sexe(requestDTO.getSexe())
                .telephone(requestDTO.getTelephone())
                .adresse(requestDTO.getAdresse())
                .allergies(requestDTO.getAllergies())
                .antecedents(requestDTO.getAntecedents())
                .contactUrgenceNom(requestDTO.getContactUrgenceNom())
                .contactUrgenceTelephone(requestDTO.getContactUrgenceTelephone())
                .contactUrgenceRelation(requestDTO.getContactUrgenceRelation())
                .deleted(false)
                .build();
        
        Patient savedPatient = patientRepository.save(patient);
        log.info("Nouveau patient créé: {} {}", savedPatient.getPrenom(), savedPatient.getNom());
        
        return mapToResponseDTO(savedPatient);
    }
    
    @Transactional(readOnly = true)
    public PatientResponseDTO getPatientById(Long id) {
        Patient patient = patientRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient non trouvé avec l'ID: " + id));
        return mapToResponseDTO(patient);
    }
    
    @Transactional(readOnly = true)
    public PageResponse<PatientResponseDTO> getAllPatients(int page, int size, String sortBy, String sortDir) {
        String validSortBy = validateSortField(sortBy);
        
        Sort sort = sortDir != null && sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(validSortBy).descending() 
                : Sort.by(validSortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Patient> patientPage = patientRepository.findByDeletedFalse(pageable);
        
        return buildPageResponse(patientPage);
    }
    
    private String validateSortField(String sortBy) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return "id";
        }
        
        String[] validFields = {"id", "nom", "prenom", "dateNaissance", "telephone", "createdAt", "updatedAt"};
        String normalizedSortBy = sortBy.trim();
        
        for (String field : validFields) {
            if (field.equalsIgnoreCase(normalizedSortBy)) {
                return field;
            }
        }
        
        return "id";
    }
    
    @Transactional(readOnly = true)
    public PageResponse<PatientResponseDTO> searchPatients(String search, int page, int size, String sortBy, String sortDir) {
        String validSortBy = validateSortField(sortBy);
        
        Sort sort = sortDir != null && sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(validSortBy).descending() 
                : Sort.by(validSortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Patient> patientPage;
        
        if (search == null || search.trim().isEmpty()) {
            patientPage = patientRepository.findByDeletedFalse(pageable);
        } else {
            patientPage = patientRepository.searchPatients(search.trim(), pageable);
        }
        
        return buildPageResponse(patientPage);
    }
    
    @Transactional
    public PatientResponseDTO updatePatient(Long id, PatientRequestDTO requestDTO) {
        Patient patient = patientRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient non trouvé avec l'ID: " + id));
        
        patient.setNom(requestDTO.getNom());
        patient.setPrenom(requestDTO.getPrenom());
        patient.setDateNaissance(requestDTO.getDateNaissance());
        patient.setSexe(requestDTO.getSexe());
        patient.setTelephone(requestDTO.getTelephone());
        patient.setAdresse(requestDTO.getAdresse());
        patient.setAllergies(requestDTO.getAllergies());
        patient.setAntecedents(requestDTO.getAntecedents());
        patient.setContactUrgenceNom(requestDTO.getContactUrgenceNom());
        patient.setContactUrgenceTelephone(requestDTO.getContactUrgenceTelephone());
        patient.setContactUrgenceRelation(requestDTO.getContactUrgenceRelation());
        
        Patient updatedPatient = patientRepository.save(patient);
        log.info("Patient mis à jour: {} {}", updatedPatient.getPrenom(), updatedPatient.getNom());
        
        return mapToResponseDTO(updatedPatient);
    }
    
    @Transactional
    public void deletePatient(Long id) {
        Patient patient = patientRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient non trouvé avec l'ID: " + id));
        
        patient.setDeleted(true);
        patientRepository.save(patient);
        log.info("Patient supprimé (soft delete): {} {}", patient.getPrenom(), patient.getNom());
    }
    
    @Transactional(readOnly = true)
    public long countPatients() {
        return patientRepository.countByDeletedFalse();
    }
    
    @Transactional(readOnly = true)
    public PatientResponseDTO getPatientByTelephone(String telephone) {
        Patient patient = patientRepository.findByTelephoneAndDeletedFalse(telephone)
                .orElseThrow(() -> new ResourceNotFoundException("Patient non trouvé avec le téléphone: " + telephone));
        return mapToResponseDTO(patient);
    }
    
    private PatientResponseDTO mapToResponseDTO(Patient patient) {
        Sexe sexe = patient.getSexe();
        if (sexe == null) {
            log.warn("Patient {} {} a un sexe null, utilisation de M par défaut", patient.getPrenom(), patient.getNom());
            sexe = Sexe.M;
        }
        
        return PatientResponseDTO.builder()
                .id(patient.getId())
                .nom(patient.getNom())
                .prenom(patient.getPrenom())
                .dateNaissance(patient.getDateNaissance())
                .sexe(sexe)
                .telephone(patient.getTelephone())
                .adresse(patient.getAdresse())
                .allergies(patient.getAllergies())
                .antecedents(patient.getAntecedents())
                .contactUrgenceNom(patient.getContactUrgenceNom())
                .contactUrgenceTelephone(patient.getContactUrgenceTelephone())
                .contactUrgenceRelation(patient.getContactUrgenceRelation())
                .createdAt(patient.getCreatedAt())
                .updatedAt(patient.getUpdatedAt())
                .build();
    }
    
    private PageResponse<PatientResponseDTO> buildPageResponse(Page<Patient> patientPage) {
        List<PatientResponseDTO> content = patientPage.getContent().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
        
        return PageResponse.<PatientResponseDTO>builder()
                .content(content)
                .page(patientPage.getNumber())
                .size(patientPage.getSize())
                .totalElements(patientPage.getTotalElements())
                .totalPages(patientPage.getTotalPages())
                .first(patientPage.isFirst())
                .last(patientPage.isLast())
                .build();
    }
}

