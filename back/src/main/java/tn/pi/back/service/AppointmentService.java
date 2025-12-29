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
import tn.pi.back.model.*;
import tn.pi.back.repository.AppointmentRepository;
import tn.pi.back.repository.DoctorRepository;
import tn.pi.back.repository.PatientRepository;
import tn.pi.back.repository.TimeSlotRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {
    
    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final TimeSlotRepository timeSlotRepository;
    
    @Transactional
    public AppointmentResponseDTO createAppointment(AppointmentRequestDTO requestDTO) {

        Doctor doctor = doctorRepository.findByIdAndDeletedFalse(requestDTO.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Docteur non trouvé avec l'ID: " + requestDTO.getDoctorId()));

        Patient patient = patientRepository.findByIdAndDeletedFalse(requestDTO.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient non trouvé avec l'ID: " + requestDTO.getPatientId()));

        AppointmentAvailabilityDTO availability = checkAvailability(requestDTO.getDoctorId(), requestDTO.getDate(), requestDTO.getHeure());
        if (!availability.getAvailable()) {
            throw new RuntimeException(availability.getMessage());
        }

        List<Appointment> conflicts = appointmentRepository.findConflictingAppointments(
            requestDTO.getDoctorId(),
            requestDTO.getDate(),
            requestDTO.getHeure()
        );
        
        if (!conflicts.isEmpty()) {
            throw new RuntimeException("Un rendez-vous existe déjà pour ce docteur à cette date et heure");
        }

        Appointment appointment = Appointment.builder()
                .doctor(doctor)
                .patient(patient)
                .date(requestDTO.getDate())
                .heure(requestDTO.getHeure())
                .status(AppointmentStatus.PENDING)
                .motif(requestDTO.getMotif())
                .notes(requestDTO.getNotes())
                .deleted(false)
                .build();
        
        Appointment savedAppointment = appointmentRepository.save(appointment);
        log.info("Nouveau rendez-vous créé: ID={}, Docteur={}, Patient={}, Date={}, Heure={}", 
                savedAppointment.getId(), doctor.getId(), patient.getId(), 
                requestDTO.getDate(), requestDTO.getHeure());
        
        return mapToResponseDTO(savedAppointment);
    }
    
    @Transactional(readOnly = true)
    public AppointmentResponseDTO getAppointmentById(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .filter(a -> !a.getDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Rendez-vous non trouvé avec l'ID: " + id));
        return mapToResponseDTO(appointment);
    }
    
    @Transactional(readOnly = true)
    public PageResponse<AppointmentResponseDTO> getPatientAppointments(Long patientId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending().and(Sort.by("heure").descending()));
        Page<Appointment> appointments = appointmentRepository.findByPatientIdAndDeletedFalseOrderByDateDescHeureDesc(patientId, pageable);
        return mapToPageResponse(appointments);
    }
    
    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> getPatientAppointmentsList(Long patientId) {
        List<Appointment> appointments = appointmentRepository.findByPatientIdAndDeletedFalseOrderByDateDescHeureDesc(patientId);
        return appointments.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public PageResponse<AppointmentResponseDTO> getDoctorAppointments(Long doctorId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending().and(Sort.by("heure").descending()));
        Page<Appointment> appointments = appointmentRepository.findByDoctorIdAndDeletedFalseOrderByDateDescHeureDesc(doctorId, pageable);
        return mapToPageResponse(appointments);
    }
    
    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> getDoctorAppointmentsList(Long doctorId) {
        List<Appointment> appointments = appointmentRepository.findByDoctorIdAndDeletedFalseOrderByDateDescHeureDesc(doctorId);
        return appointments.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> getDoctorAppointmentsByDate(Long doctorId, LocalDate date) {
        List<Appointment> appointments = appointmentRepository.findByDoctorIdAndDateAndDeletedFalseOrderByHeureAsc(doctorId, date);
        return appointments.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> getDoctorAppointmentsByWeek(Long doctorId, LocalDate weekStart) {

        LocalDate startDate = weekStart.with(DayOfWeek.MONDAY);
        LocalDate endDate = startDate.plusDays(6);
        
        List<Appointment> appointments = appointmentRepository.findByDoctorIdAndDateBetween(doctorId, startDate, endDate);
        return appointments.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public AppointmentAvailabilityDTO checkAvailability(Long doctorId, LocalDate date, LocalTime heure) {

        Doctor doctor = doctorRepository.findByIdAndDeletedFalse(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Docteur non trouvé avec l'ID: " + doctorId));

        DayOfWeek dayOfWeek = date.getDayOfWeek();

        JourSemaine jour;
        if (dayOfWeek == DayOfWeek.MONDAY) {
            jour = JourSemaine.LUNDI;
        } else if (dayOfWeek == DayOfWeek.TUESDAY) {
            jour = JourSemaine.MARDI;
        } else if (dayOfWeek == DayOfWeek.WEDNESDAY) {
            jour = JourSemaine.MERCREDI;
        } else if (dayOfWeek == DayOfWeek.THURSDAY) {
            jour = JourSemaine.JEUDI;
        } else if (dayOfWeek == DayOfWeek.FRIDAY) {
            jour = JourSemaine.VENDREDI;
        } else if (dayOfWeek == DayOfWeek.SATURDAY) {
            jour = JourSemaine.SAMEDI;
        } else { // SUNDAY
            jour = JourSemaine.DIMANCHE;
        }
        
        boolean hasTimeSlot = timeSlotRepository.findAvailableSlotAtTime(doctorId, jour, heure).isPresent();
        
        if (!hasTimeSlot) {
            return AppointmentAvailabilityDTO.builder()
                    .doctorId(doctorId)
                    .date(date)
                    .heure(heure)
                    .available(false)
                    .message("Le docteur n'a pas de créneau disponible pour ce jour et cette heure")
                    .build();
        }

        boolean hasConflict = appointmentRepository.existsByDoctorIdAndDateAndHeureAndActiveStatus(doctorId, date, heure);
        
        if (hasConflict) {
            return AppointmentAvailabilityDTO.builder()
                    .doctorId(doctorId)
                    .date(date)
                    .heure(heure)
                    .available(false)
                    .message("Ce créneau est déjà réservé")
                    .build();
        }
        
        return AppointmentAvailabilityDTO.builder()
                .doctorId(doctorId)
                .date(date)
                .heure(heure)
                .available(true)
                .message("Créneau disponible")
                .build();
    }
    
    @Transactional
    public AppointmentResponseDTO updateAppointmentStatus(Long id, AppointmentStatus newStatus) {
        Appointment appointment = appointmentRepository.findById(id)
                .filter(a -> !a.getDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Rendez-vous non trouvé avec l'ID: " + id));
        
        AppointmentStatus oldStatus = appointment.getStatus();
        appointment.setStatus(newStatus);
        Appointment updated = appointmentRepository.save(appointment);
        
        log.info("Statut du rendez-vous {} mis à jour: {} -> {}", id, oldStatus, newStatus);
        return mapToResponseDTO(updated);
    }
    
    @Transactional
    public AppointmentResponseDTO confirmAppointment(Long id) {
        return updateAppointmentStatus(id, AppointmentStatus.CONFIRMED);
    }
    
    @Transactional
    public AppointmentResponseDTO cancelAppointment(Long id) {
        return updateAppointmentStatus(id, AppointmentStatus.CANCELLED);
    }
    
    @Transactional
    public AppointmentResponseDTO completeAppointment(Long id) {
        return updateAppointmentStatus(id, AppointmentStatus.COMPLETED);
    }
    
    @Transactional
    public AppointmentResponseDTO markNoShow(Long id) {
        return updateAppointmentStatus(id, AppointmentStatus.NO_SHOW);
    }
    
    @Transactional
    public AppointmentResponseDTO rescheduleAppointment(Long id, LocalDate newDate, LocalTime newHeure) {
        Appointment appointment = appointmentRepository.findById(id)
                .filter(a -> !a.getDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Rendez-vous non trouvé avec l'ID: " + id));

        checkAvailability(appointment.getDoctor().getId(), newDate, newHeure);

        List<Appointment> conflicts = appointmentRepository.findConflictingAppointments(
            appointment.getDoctor().getId(),
            newDate,
            newHeure
        );

        conflicts = conflicts.stream()
                .filter(c -> !c.getId().equals(id))
                .collect(Collectors.toList());
        
        if (!conflicts.isEmpty()) {
            throw new RuntimeException("Un rendez-vous existe déjà pour ce docteur à cette date et heure");
        }
        
        appointment.setDate(newDate);
        appointment.setHeure(newHeure);

        if (appointment.getStatus() == AppointmentStatus.CONFIRMED) {
            appointment.setStatus(AppointmentStatus.PENDING);
        }
        
        Appointment updated = appointmentRepository.save(appointment);
        log.info("Rendez-vous {} reprogrammé: {} {} -> {} {}", 
                id, appointment.getDate(), appointment.getHeure(), newDate, newHeure);
        
        return mapToResponseDTO(updated);
    }
    
    @Transactional
    public AppointmentResponseDTO updateAppointment(Long id, AppointmentRequestDTO requestDTO) {
        Appointment appointment = appointmentRepository.findById(id)
                .filter(a -> !a.getDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Rendez-vous non trouvé avec l'ID: " + id));

        if (!appointment.getDate().equals(requestDTO.getDate()) || 
            !appointment.getHeure().equals(requestDTO.getHeure())) {
            checkAvailability(requestDTO.getDoctorId(), requestDTO.getDate(), requestDTO.getHeure());

            List<Appointment> conflicts = appointmentRepository.findConflictingAppointments(
                requestDTO.getDoctorId(),
                requestDTO.getDate(),
                requestDTO.getHeure()
            );
            
            conflicts = conflicts.stream()
                    .filter(c -> !c.getId().equals(id))
                    .collect(Collectors.toList());
            
            if (!conflicts.isEmpty()) {
                throw new RuntimeException("Un rendez-vous existe déjà pour ce docteur à cette date et heure");
            }
        }

        if (requestDTO.getDoctorId() != null && !requestDTO.getDoctorId().equals(appointment.getDoctor().getId())) {
            Doctor doctor = doctorRepository.findByIdAndDeletedFalse(requestDTO.getDoctorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Docteur non trouvé avec l'ID: " + requestDTO.getDoctorId()));
            appointment.setDoctor(doctor);
        }
        
        if (requestDTO.getPatientId() != null && !requestDTO.getPatientId().equals(appointment.getPatient().getId())) {
            Patient patient = patientRepository.findByIdAndDeletedFalse(requestDTO.getPatientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Patient non trouvé avec l'ID: " + requestDTO.getPatientId()));
            appointment.setPatient(patient);
        }
        
        appointment.setDate(requestDTO.getDate());
        appointment.setHeure(requestDTO.getHeure());
        appointment.setMotif(requestDTO.getMotif());
        appointment.setNotes(requestDTO.getNotes());
        
        Appointment updated = appointmentRepository.save(appointment);
        return mapToResponseDTO(updated);
    }
    
    @Transactional
    public void deleteAppointment(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .filter(a -> !a.getDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Rendez-vous non trouvé avec l'ID: " + id));
        
        appointment.setDeleted(true);
        appointmentRepository.save(appointment);
        log.info("Rendez-vous {} supprimé (soft delete)", id);
    }
    
    @Transactional(readOnly = true)
    public long countAppointmentsByDoctor(Long doctorId) {
        return appointmentRepository.countByDoctorIdAndDeletedFalse(doctorId);
    }
    
    @Transactional(readOnly = true)
    public long countAppointmentsByPatient(Long patientId) {
        return appointmentRepository.countByPatientIdAndDeletedFalse(patientId);
    }
    
    private AppointmentResponseDTO mapToResponseDTO(Appointment appointment) {
        return AppointmentResponseDTO.builder()
                .id(appointment.getId())
                .doctorId(appointment.getDoctor().getId())
                .doctorNomComplet(appointment.getDoctor().getUser().getFirstName() + " " + 
                                 appointment.getDoctor().getUser().getLastName())
                .doctorSpecialite(appointment.getDoctor().getSpecialite().name())
                .patientId(appointment.getPatient().getId())
                .patientNomComplet(appointment.getPatient().getPrenom() + " " + 
                                  appointment.getPatient().getNom())
                .date(appointment.getDate())
                .heure(appointment.getHeure())
                .status(appointment.getStatus())
                .motif(appointment.getMotif())
                .notes(appointment.getNotes())
                .createdAt(appointment.getCreatedAt())
                .updatedAt(appointment.getUpdatedAt())
                .build();
    }
    
    private PageResponse<AppointmentResponseDTO> mapToPageResponse(Page<Appointment> appointments) {
        List<AppointmentResponseDTO> content = appointments.getContent().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
        
        return PageResponse.<AppointmentResponseDTO>builder()
                .content(content)
                .page(appointments.getNumber())
                .size(appointments.getSize())
                .totalElements(appointments.getTotalElements())
                .totalPages(appointments.getTotalPages())
                .first(appointments.isFirst())
                .last(appointments.isLast())
                .build();
    }
}

