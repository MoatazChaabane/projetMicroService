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

    List<MedicalAttachment> findByMedicalRecordIdOrderByCreatedAtDesc(Long medicalRecordId);

    List<MedicalAttachment> findByMedicalRecordIdAndAttachmentTypeOrderByCreatedAtDesc(
            Long medicalRecordId, AttachmentType attachmentType);

    Optional<MedicalAttachment> findByGridFsId(String gridFsId);

    @Query("SELECT a FROM MedicalAttachment a WHERE a.medicalRecord.patient.id = :patientId " +
           "AND LOWER(a.description) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "ORDER BY a.createdAt DESC")
    List<MedicalAttachment> searchInAttachments(@Param("patientId") Long patientId, @Param("search") String search);

    long countByMedicalRecordId(Long medicalRecordId);
}

