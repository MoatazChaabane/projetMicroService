package tn.pi.back.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.pi.back.model.Appointment;
import tn.pi.back.model.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    
    // RDV d'un patient
    Page<Appointment> findByPatientIdAndDeletedFalseOrderByDateDescHeureDesc(Long patientId, Pageable pageable);
    
    List<Appointment> findByPatientIdAndDeletedFalseOrderByDateDescHeureDesc(Long patientId);
    
    // RDV d'un docteur
    Page<Appointment> findByDoctorIdAndDeletedFalseOrderByDateDescHeureDesc(Long doctorId, Pageable pageable);
    
    List<Appointment> findByDoctorIdAndDeletedFalseOrderByDateDescHeureDesc(Long doctorId);
    
    // RDV d'un docteur pour une date spécifique
    List<Appointment> findByDoctorIdAndDateAndDeletedFalseOrderByHeureAsc(Long doctorId, LocalDate date);
    
    // RDV d'un docteur pour une semaine (du lundi au dimanche)
    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId " +
           "AND a.date >= :startDate AND a.date <= :endDate " +
           "AND a.deleted = false ORDER BY a.date ASC, a.heure ASC")
    List<Appointment> findByDoctorIdAndDateBetween(
            @Param("doctorId") Long doctorId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    // Vérifier conflit : même docteur, même date, même heure, statut actif
    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId " +
           "AND a.date = :date AND a.heure = :heure " +
           "AND a.status IN ('PENDING', 'CONFIRMED') AND a.deleted = false")
    List<Appointment> findConflictingAppointments(
            @Param("doctorId") Long doctorId,
            @Param("date") LocalDate date,
            @Param("heure") LocalTime heure);
    
    // Vérifier disponibilité : pas de RDV confirmé ou pending pour ce slot
    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.doctor.id = :doctorId " +
           "AND a.date = :date AND a.heure = :heure " +
           "AND a.status IN ('PENDING', 'CONFIRMED') AND a.deleted = false")
    boolean existsByDoctorIdAndDateAndHeureAndActiveStatus(
            @Param("doctorId") Long doctorId,
            @Param("date") LocalDate date,
            @Param("heure") LocalTime heure);
    
    // RDV par statut
    Page<Appointment> findByDoctorIdAndStatusAndDeletedFalseOrderByDateDescHeureDesc(
            Long doctorId, AppointmentStatus status, Pageable pageable);
    
    Page<Appointment> findByPatientIdAndStatusAndDeletedFalseOrderByDateDescHeureDesc(
            Long patientId, AppointmentStatus status, Pageable pageable);
    
    // Compter les RDV
    long countByDoctorIdAndDeletedFalse(Long doctorId);
    long countByPatientIdAndDeletedFalse(Long patientId);
    long countByDoctorIdAndStatusAndDeletedFalse(Long doctorId, AppointmentStatus status);
    long countByPatientIdAndStatusAndDeletedFalse(Long patientId, AppointmentStatus status);
}

