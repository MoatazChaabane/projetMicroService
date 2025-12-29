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
    
    Page<Appointment> findByPatientIdAndDeletedFalseOrderByDateDescHeureDesc(Long patientId, Pageable pageable);
    
    List<Appointment> findByPatientIdAndDeletedFalseOrderByDateDescHeureDesc(Long patientId);
    
    Page<Appointment> findByDoctorIdAndDeletedFalseOrderByDateDescHeureDesc(Long doctorId, Pageable pageable);
    
    List<Appointment> findByDoctorIdAndDeletedFalseOrderByDateDescHeureDesc(Long doctorId);
    
    List<Appointment> findByDoctorIdAndDateAndDeletedFalseOrderByHeureAsc(Long doctorId, LocalDate date);
    
    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId " +
           "AND a.date >= :startDate AND a.date <= :endDate " +
           "AND a.deleted = false ORDER BY a.date ASC, a.heure ASC")
    List<Appointment> findByDoctorIdAndDateBetween(
            @Param("doctorId") Long doctorId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId " +
           "AND a.date = :date AND a.heure = :heure " +
           "AND a.status IN ('PENDING', 'CONFIRMED') AND a.deleted = false")
    List<Appointment> findConflictingAppointments(
            @Param("doctorId") Long doctorId,
            @Param("date") LocalDate date,
            @Param("heure") LocalTime heure);
    
    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.doctor.id = :doctorId " +
           "AND a.date = :date AND a.heure = :heure " +
           "AND a.status IN ('PENDING', 'CONFIRMED') AND a.deleted = false")
    boolean existsByDoctorIdAndDateAndHeureAndActiveStatus(
            @Param("doctorId") Long doctorId,
            @Param("date") LocalDate date,
            @Param("heure") LocalTime heure);
    
    Page<Appointment> findByDoctorIdAndStatusAndDeletedFalseOrderByDateDescHeureDesc(
            Long doctorId, AppointmentStatus status, Pageable pageable);
    
    Page<Appointment> findByPatientIdAndStatusAndDeletedFalseOrderByDateDescHeureDesc(
            Long patientId, AppointmentStatus status, Pageable pageable);
    
    long countByDoctorIdAndDeletedFalse(Long doctorId);
    long countByPatientIdAndDeletedFalse(Long patientId);
    long countByDoctorIdAndStatusAndDeletedFalse(Long doctorId, AppointmentStatus status);
    long countByPatientIdAndStatusAndDeletedFalse(Long patientId, AppointmentStatus status);
}

