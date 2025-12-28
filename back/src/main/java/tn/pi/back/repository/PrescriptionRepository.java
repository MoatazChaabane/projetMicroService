package tn.pi.back.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.pi.back.model.Prescription;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    
    // Ordonnances d'un patient
    Page<Prescription> findByPatientIdOrderByDateDesc(Long patientId, Pageable pageable);
    
    List<Prescription> findByPatientIdOrderByDateDesc(Long patientId);
    
    // Ordonnances d'un docteur
    Page<Prescription> findByDoctorIdOrderByDateDesc(Long doctorId, Pageable pageable);
    
    List<Prescription> findByDoctorIdOrderByDateDesc(Long doctorId);
    
    // Ordonnances d'un patient par un docteur
    @Query("SELECT p FROM Prescription p WHERE p.patient.id = :patientId AND p.doctor.id = :doctorId ORDER BY p.date DESC")
    Page<Prescription> findByPatientIdAndDoctorId(@Param("patientId") Long patientId, 
                                                   @Param("doctorId") Long doctorId, 
                                                   Pageable pageable);
    
    // Ordonnances par date
    List<Prescription> findByDateBetweenOrderByDateDesc(LocalDate startDate, LocalDate endDate);
    
    // Ordonnances d'un patient par date
    List<Prescription> findByPatientIdAndDateBetweenOrderByDateDesc(Long patientId, 
                                                                     LocalDate startDate, 
                                                                     LocalDate endDate);
}

