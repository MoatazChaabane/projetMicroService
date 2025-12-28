package tn.pi.back.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "medical_attachments", indexes = {
    @Index(name = "idx_attachment_medical_record", columnList = "medical_record_id"),
    @Index(name = "idx_attachment_type", columnList = "attachment_type"),
    @Index(name = "idx_attachment_created", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalAttachment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_record_id", nullable = false)
    private MedicalRecord medicalRecord;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private Doctor doctor; // Docteur qui a ajouté le fichier
    
    @Column(nullable = false, length = 100)
    private String fileName; // Nom original du fichier
    
    @Column(nullable = false, length = 100)
    private String fileType; // Type MIME (application/pdf, image/jpeg, etc.)
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AttachmentType attachmentType; // ANALYSE, IMAGE, DOCUMENT, AUTRE
    
    @Column(length = 500)
    private String description; // Description du fichier
    
    @Column(nullable = false)
    private Long fileSize; // Taille en bytes
    
    @Column(length = 100)
    private String gridFsId; // ID du fichier dans GridFS (si utilisé)
    
    @Column(length = 500)
    private String filePath; // Chemin local du fichier (si stockage local)
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public enum AttachmentType {
        ANALYSE,     // Analyse médicale (PDF)
        IMAGE,       // Image médicale (radiographie, scanner, etc.)
        DOCUMENT,    // Document général
        PRESCRIPTION, // Ordonnance
        CERTIFICAT,  // Certificat médical
        AUTRE        // Autre type de document
    }
}

