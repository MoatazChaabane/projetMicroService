package tn.pi.back.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.pi.back.model.MedicalRecord;

import java.util.Optional;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    
    // Trouver le dossier médical d'un patient
    Optional<MedicalRecord> findByPatientId(Long patientId);
    
    // Vérifier si un dossier existe pour un patient
    boolean existsByPatientId(Long patientId);
}

