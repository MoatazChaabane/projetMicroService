package tn.pi.back.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.pi.back.model.MedicalAttachment;
import tn.pi.back.model.MedicalAttachment.AttachmentType;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicalAttachmentRepository extends JpaRepository<MedicalAttachment, Long> {
    
    // Pièces jointes d'un dossier médical triées par date décroissante
    List<MedicalAttachment> findByMedicalRecordIdOrderByCreatedAtDesc(Long medicalRecordId);
    
    // Pièces jointes par type
    List<MedicalAttachment> findByMedicalRecordIdAndAttachmentTypeOrderByCreatedAtDesc(
            Long medicalRecordId, AttachmentType attachmentType);
    
    // Pièce jointe par GridFS ID
    Optional<MedicalAttachment> findByGridFsId(String gridFsId);
    
    // Recherche dans les descriptions de pièces jointes
    @Query("SELECT a FROM MedicalAttachment a WHERE a.medicalRecord.patient.id = :patientId " +
           "AND LOWER(a.description) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "ORDER BY a.createdAt DESC")
    List<MedicalAttachment> searchInAttachments(@Param("patientId") Long patientId, @Param("search") String search);
    
    // Compter les pièces jointes d'un dossier médical
    long countByMedicalRecordId(Long medicalRecordId);
}

