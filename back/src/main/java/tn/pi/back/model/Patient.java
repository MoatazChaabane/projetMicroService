package tn.pi.back.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "patients", indexes = {
    @Index(name = "idx_patient_nom", columnList = "nom"),
    @Index(name = "idx_patient_telephone", columnList = "telephone"),
    @Index(name = "idx_patient_deleted", columnList = "deleted")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String nom;
    
    @Column(nullable = false, length = 100)
    private String prenom;
    
    @Column(nullable = false)
    private LocalDate dateNaissance;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Sexe sexe;
    
    @Column(length = 20)
    private String telephone;
    
    @Column(length = 500)
    private String adresse;
    
    @Column(columnDefinition = "TEXT")
    private String allergies;
    
    @Column(columnDefinition = "TEXT")
    private String antecedents;
    
    @Column(length = 100)
    private String contactUrgenceNom;
    
    @Column(length = 20)
    private String contactUrgenceTelephone;
    
    @Column(length = 500)
    private String contactUrgenceRelation;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

