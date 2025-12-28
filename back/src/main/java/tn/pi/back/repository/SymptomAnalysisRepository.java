package tn.pi.back.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.pi.back.model.SymptomAnalysis;

import java.util.List;
import java.util.Optional;

@Repository
public interface SymptomAnalysisRepository extends JpaRepository<SymptomAnalysis, Long> {
    
    // Trouver toutes les analyses d'un patient
    Page<SymptomAnalysis> findByPatientIdOrderByCreatedAtDesc(Long patientId, Pageable pageable);
    
    List<SymptomAnalysis> findByPatientIdOrderByCreatedAtDesc(Long patientId);
    
    // Trouver l'analyse liée à un rendez-vous
    Optional<SymptomAnalysis> findByAppointmentId(Long appointmentId);
    
    // Trouver les analyses urgentes d'un patient
    @Query("SELECT sa FROM SymptomAnalysis sa WHERE sa.patient.id = :patientId AND sa.urgentRecommendation = true ORDER BY sa.createdAt DESC")
    List<SymptomAnalysis> findUrgentByPatientId(@Param("patientId") Long patientId);
}

