package tn.pi.back.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.pi.back.model.Visit;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VisitRepository extends JpaRepository<Visit, Long> {
    
    // Consultations d'un dossier médical triées par date décroissante
    List<Visit> findByMedicalRecordIdOrderByVisitDateDescVisitTimeDesc(Long medicalRecordId);
    
    Page<Visit> findByMedicalRecordIdOrderByVisitDateDescVisitTimeDesc(Long medicalRecordId, Pageable pageable);
    
    // Consultations d'un docteur
    List<Visit> findByDoctorIdOrderByVisitDateDescVisitTimeDesc(Long doctorId);
    
    // Consultations par date
    @Query("SELECT v FROM Visit v WHERE v.medicalRecord.patient.id = :patientId " +
           "AND v.visitDate BETWEEN :startDate AND :endDate " +
           "ORDER BY v.visitDate DESC, v.visitTime DESC")
    List<Visit> findByPatientIdAndDateBetween(
            @Param("patientId") Long patientId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    // Recherche dans les consultations (texte libre)
    @Query("SELECT v FROM Visit v WHERE v.medicalRecord.patient.id = :patientId " +
           "AND (LOWER(v.reason) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(v.symptoms) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(v.diagnosis) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(v.treatment) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(v.notes) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY v.visitDate DESC, v.visitTime DESC")
    List<Visit> searchInVisits(@Param("patientId") Long patientId, @Param("search") String search);
    
    // Compter les consultations d'un dossier médical
    long countByMedicalRecordId(Long medicalRecordId);
}

