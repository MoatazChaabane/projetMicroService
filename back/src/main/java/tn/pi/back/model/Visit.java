package tn.pi.back.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "visits", indexes = {
    @Index(name = "idx_visit_medical_record", columnList = "medical_record_id"),
    @Index(name = "idx_visit_doctor", columnList = "doctor_id"),
    @Index(name = "idx_visit_date", columnList = "visit_date"),
    @Index(name = "idx_visit_created", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Visit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_record_id", nullable = false)
    private MedicalRecord medicalRecord;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;
    
    @Column(nullable = false)
    private LocalDate visitDate;
    
    @Column
    private LocalTime visitTime;
    
    @Column(columnDefinition = "TEXT")
    private String reason; // Motif de la consultation
    
    @Column(columnDefinition = "TEXT")
    private String symptoms; // Symptômes observés
    
    @Column(columnDefinition = "TEXT")
    private String diagnosis; // Diagnostic
    
    @Column(columnDefinition = "TEXT")
    private String treatment; // Traitement prescrit
    
    @Column(columnDefinition = "TEXT")
    private String notes; // Notes additionnelles
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (visitDate == null) {
            visitDate = LocalDate.now();
        }
        if (visitTime == null) {
            visitTime = LocalTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

