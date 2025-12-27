package tn.pi.back.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "doctors", indexes = {
    @Index(name = "idx_doctor_user", columnList = "user_id"),
    @Index(name = "idx_doctor_specialite", columnList = "specialite"),
    @Index(name = "idx_doctor_latitude_longitude", columnList = "latitude,longitude"),
    @Index(name = "idx_doctor_deleted", columnList = "deleted")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE doctors SET deleted = true WHERE id=?")
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Specialite specialite;
    
    @Column(length = 200)
    private String nomClinique;
    
    @Column(length = 500)
    private String adresse;
    
    @Column(precision = 10, scale = 8)
    private Double latitude;
    
    @Column(precision = 11, scale = 8)
    private Double longitude;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal tarifConsultation;
    
    @ElementCollection
    @CollectionTable(name = "doctor_langues", joinColumns = @JoinColumn(name = "doctor_id"))
    @Column(name = "langue", length = 50)
    @Builder.Default
    private List<String> langues = new ArrayList<>();
    
    @Column(precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal rating = BigDecimal.ZERO;
    
    @Column(nullable = false)
    @Builder.Default
    private Integer nombreAvis = 0;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean teleconsultation = false;
    
    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<TimeSlot> horaires = new ArrayList<>();
    
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
        deleted = false;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

