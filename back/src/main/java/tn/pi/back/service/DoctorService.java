package tn.pi.back.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.pi.back.dto.*;
import tn.pi.back.exception.ResourceNotFoundException;
import tn.pi.back.model.Doctor;
import tn.pi.back.model.JourSemaine;
import tn.pi.back.model.Specialite;
import tn.pi.back.model.TimeSlot;
import tn.pi.back.model.User;
import tn.pi.back.repository.DoctorRepository;
import tn.pi.back.repository.TimeSlotRepository;
import tn.pi.back.repository.UserRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorService {
    
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final TimeSlotRepository timeSlotRepository;
    
    @Transactional
    public DoctorResponseDTO createDoctor(DoctorRequestDTO requestDTO) {
        // Vérifier que l'utilisateur existe
        User user = userRepository.findById(requestDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'ID: " + requestDTO.getUserId()));
        
        // Vérifier que l'utilisateur n'a pas déjà un profil docteur
        if (doctorRepository.findByUserId(requestDTO.getUserId()).isPresent()) {
            throw new RuntimeException("Cet utilisateur a déjà un profil docteur");
        }
        
        Doctor doctor = Doctor.builder()
                .user(user)
                .specialite(requestDTO.getSpecialite())
                .nomClinique(requestDTO.getNomClinique())
                .adresse(requestDTO.getAdresse())
                .latitude(requestDTO.getLatitude())
                .longitude(requestDTO.getLongitude())
                .tarifConsultation(requestDTO.getTarifConsultation())
                .langues(requestDTO.getLangues() != null ? requestDTO.getLangues() : List.of())
                .teleconsultation(requestDTO.getTeleconsultation() != null ? requestDTO.getTeleconsultation() : false)
                .rating(java.math.BigDecimal.ZERO)
                .nombreAvis(0)
                .deleted(false)
                .build();
        
        Doctor savedDoctor = doctorRepository.save(doctor);
        
        // Créer les créneaux horaires si fournis
        if (requestDTO.getHoraires() != null && !requestDTO.getHoraires().isEmpty()) {
            List<TimeSlot> timeSlots = requestDTO.getHoraires().stream()
                    .map(slotDTO -> TimeSlot.builder()
                            .doctor(savedDoctor)
                            .jour(slotDTO.getJour())
                            .heureDebut(slotDTO.getHeureDebut())
                            .heureFin(slotDTO.getHeureFin())
                            .disponible(slotDTO.getDisponible() != null ? slotDTO.getDisponible() : true)
                            .actif(true)
                            .build())
                    .collect(Collectors.toList());
            timeSlotRepository.saveAll(timeSlots);
            savedDoctor.setHoraires(timeSlots);
        }
        
        log.info("Nouveau docteur créé: {} {}", savedDoctor.getUser().getFirstName(), savedDoctor.getUser().getLastName());
        return mapToResponseDTO(savedDoctor);
    }
    
    @Transactional(readOnly = true)
    public DoctorResponseDTO getDoctorById(Long id) {
        Doctor doctor = doctorRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Docteur non trouvé avec l'ID: " + id));
        return mapToResponseDTO(doctor);
    }
    
    @Transactional(readOnly = true)
    public DoctorResponseDTO getDoctorByUserId(Long userId) {
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Docteur non trouvé pour l'utilisateur ID: " + userId));
        return mapToResponseDTO(doctor);
    }
    
    @Transactional(readOnly = true)
    public PageResponse<DoctorResponseDTO> getAllDoctors(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Doctor> doctorsPage = doctorRepository.findByDeletedFalse(pageable);
        
        List<DoctorResponseDTO> content = doctorsPage.getContent().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
        
        return PageResponse.<DoctorResponseDTO>builder()
                .content(content)
                .page(doctorsPage.getNumber())
                .size(doctorsPage.getSize())
                .totalElements(doctorsPage.getTotalElements())
                .totalPages(doctorsPage.getTotalPages())
                .first(doctorsPage.isFirst())
                .last(doctorsPage.isLast())
                .build();
    }
    
    @Transactional(readOnly = true)
    public PageResponse<DoctorResponseDTO> searchDoctors(DoctorSearchDTO searchDTO, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Doctor> doctorsPage;
        
        // Recherche complète : spécialité + distance + disponibilité
        if (searchDTO.getSpecialite() != null 
                && searchDTO.getLatitude() != null 
                && searchDTO.getLongitude() != null 
                && searchDTO.getRayonKm() != null
                && searchDTO.getDate() != null
                && searchDTO.getHeure() != null) {
            
            JourSemaine jour = convertDateToJourSemaine(searchDTO.getDate());
            doctorsPage = doctorRepository.findDoctorsBySpecialiteDistanceAndDisponibilite(
                    searchDTO.getSpecialite().name(),
                    searchDTO.getLatitude(),
                    searchDTO.getLongitude(),
                    searchDTO.getRayonKm(),
                    jour.name(),
                    searchDTO.getHeure(),
                    pageable
            );
        }
        // Recherche : spécialité + distance
        else if (searchDTO.getSpecialite() != null 
                && searchDTO.getLatitude() != null 
                && searchDTO.getLongitude() != null 
                && searchDTO.getRayonKm() != null) {
            doctorsPage = doctorRepository.findDoctorsBySpecialiteAndDistance(
                    searchDTO.getSpecialite().name(),
                    searchDTO.getLatitude(),
                    searchDTO.getLongitude(),
                    searchDTO.getRayonKm(),
                    pageable
            );
        }
        // Recherche : spécialité + téléconsultation
        else if (searchDTO.getSpecialite() != null && searchDTO.getTeleconsultation() != null) {
            doctorsPage = doctorRepository.findBySpecialiteAndTeleconsultation(
                    searchDTO.getSpecialite(),
                    searchDTO.getTeleconsultation(),
                    pageable
            );
        }
        // Recherche : spécialité + rating minimum
        else if (searchDTO.getSpecialite() != null && searchDTO.getRatingMin() != null) {
            doctorsPage = doctorRepository.findBySpecialiteAndRatingMin(
                    searchDTO.getSpecialite(),
                    searchDTO.getRatingMin(),
                    pageable
            );
        }
        // Recherche : spécialité seule
        else if (searchDTO.getSpecialite() != null) {
            doctorsPage = doctorRepository.findBySpecialite(searchDTO.getSpecialite(), pageable);
        }
        // Recherche : distance seule
        else if (searchDTO.getLatitude() != null 
                && searchDTO.getLongitude() != null 
                && searchDTO.getRayonKm() != null) {
            doctorsPage = doctorRepository.findDoctorsByDistance(
                    searchDTO.getLatitude(),
                    searchDTO.getLongitude(),
                    searchDTO.getRayonKm(),
                    pageable
            );
        }
        // Recherche : disponibilité seule
        else if (searchDTO.getDate() != null && searchDTO.getHeure() != null) {
            JourSemaine jour = convertDateToJourSemaine(searchDTO.getDate());
            doctorsPage = doctorRepository.findDoctorsByDisponibilite(
                    jour.name(),
                    searchDTO.getHeure(),
                    pageable
            );
        }
        // Par défaut : tous les docteurs
        else {
            doctorsPage = doctorRepository.findByDeletedFalse(pageable);
        }
        
        List<DoctorResponseDTO> content = doctorsPage.getContent().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
        
        return PageResponse.<DoctorResponseDTO>builder()
                .content(content)
                .page(doctorsPage.getNumber())
                .size(doctorsPage.getSize())
                .totalElements(doctorsPage.getTotalElements())
                .totalPages(doctorsPage.getTotalPages())
                .first(doctorsPage.isFirst())
                .last(doctorsPage.isLast())
                .build();
    }
    
    @Transactional
    public DoctorResponseDTO updateDoctor(Long id, DoctorRequestDTO requestDTO) {
        Doctor doctor = doctorRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Docteur non trouvé avec l'ID: " + id));
        
        doctor.setSpecialite(requestDTO.getSpecialite());
        doctor.setNomClinique(requestDTO.getNomClinique());
        doctor.setAdresse(requestDTO.getAdresse());
        doctor.setLatitude(requestDTO.getLatitude());
        doctor.setLongitude(requestDTO.getLongitude());
        doctor.setTarifConsultation(requestDTO.getTarifConsultation());
        doctor.setLangues(requestDTO.getLangues() != null ? requestDTO.getLangues() : List.of());
        doctor.setTeleconsultation(requestDTO.getTeleconsultation() != null ? requestDTO.getTeleconsultation() : false);
        
        // Mettre à jour les créneaux horaires
        if (requestDTO.getHoraires() != null) {
            // Supprimer les anciens créneaux
            timeSlotRepository.deleteByDoctorId(id);
            
            // Créer les nouveaux créneaux
            List<TimeSlot> timeSlots = requestDTO.getHoraires().stream()
                    .map(slotDTO -> TimeSlot.builder()
                            .doctor(doctor)
                            .jour(slotDTO.getJour())
                            .heureDebut(slotDTO.getHeureDebut())
                            .heureFin(slotDTO.getHeureFin())
                            .disponible(slotDTO.getDisponible() != null ? slotDTO.getDisponible() : true)
                            .actif(true)
                            .build())
                    .collect(Collectors.toList());
            timeSlotRepository.saveAll(timeSlots);
            doctor.setHoraires(timeSlots);
        }
        
        Doctor updatedDoctor = doctorRepository.save(doctor);
        log.info("Docteur mis à jour: {} {}", updatedDoctor.getUser().getFirstName(), updatedDoctor.getUser().getLastName());
        return mapToResponseDTO(updatedDoctor);
    }
    
    @Transactional
    public void deleteDoctor(Long id) {
        Doctor doctor = doctorRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Docteur non trouvé avec l'ID: " + id));
        doctor.setDeleted(true);
        doctorRepository.save(doctor);
        log.info("Docteur supprimé (soft delete): {} {}", doctor.getUser().getFirstName(), doctor.getUser().getLastName());
    }
    
    @Transactional(readOnly = true)
    public long countDoctors() {
        return doctorRepository.countByDeletedFalse();
    }
    
    // Méthode utilitaire pour convertir une date en jour de la semaine
    private JourSemaine convertDateToJourSemaine(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return switch (dayOfWeek) {
            case MONDAY -> JourSemaine.LUNDI;
            case TUESDAY -> JourSemaine.MARDI;
            case WEDNESDAY -> JourSemaine.MERCREDI;
            case THURSDAY -> JourSemaine.JEUDI;
            case FRIDAY -> JourSemaine.VENDREDI;
            case SATURDAY -> JourSemaine.SAMEDI;
            case SUNDAY -> JourSemaine.DIMANCHE;
        };
    }
    
    private DoctorResponseDTO mapToResponseDTO(Doctor doctor) {
        List<TimeSlotDTO> horairesDTO = doctor.getHoraires() != null 
                ? doctor.getHoraires().stream()
                        .filter(TimeSlot::getActif)
                        .map(ts -> TimeSlotDTO.builder()
                                .id(ts.getId())
                                .jour(ts.getJour())
                                .heureDebut(ts.getHeureDebut())
                                .heureFin(ts.getHeureFin())
                                .disponible(ts.getDisponible())
                                .build())
                        .collect(Collectors.toList())
                : List.of();
        
        return DoctorResponseDTO.builder()
                .id(doctor.getId())
                .userId(doctor.getUser().getId())
                .nomComplet("Dr. " + doctor.getUser().getFirstName() + " " + doctor.getUser().getLastName())
                .email(doctor.getUser().getEmail())
                .telephone(doctor.getUser().getPhoneNumber())
                .specialite(doctor.getSpecialite())
                .nomClinique(doctor.getNomClinique())
                .adresse(doctor.getAdresse())
                .latitude(doctor.getLatitude())
                .longitude(doctor.getLongitude())
                .tarifConsultation(doctor.getTarifConsultation())
                .langues(doctor.getLangues())
                .rating(doctor.getRating())
                .nombreAvis(doctor.getNombreAvis())
                .teleconsultation(doctor.getTeleconsultation())
                .horaires(horairesDTO)
                .createdAt(doctor.getCreatedAt())
                .updatedAt(doctor.getUpdatedAt())
                .build();
    }
}

