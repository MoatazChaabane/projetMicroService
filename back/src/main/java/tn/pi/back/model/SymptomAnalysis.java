package tn.pi.back.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "symptom_analyses", indexes = {
    @Index(name = "idx_symptom_analysis_patient", columnList = "patient_id"),
    @Index(name = "idx_symptom_analysis_appointment", columnList = "appointment_id"),
    @Index(name = "idx_symptom_analysis_created", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SymptomAnalysis {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String originalDescription;
    
    @ElementCollection
    @CollectionTable(name = "symptom_analysis_symptoms", joinColumns = @JoinColumn(name = "analysis_id"))
    @Column(name = "symptom", length = 200)
    @Builder.Default
    private List<String> symptoms = new ArrayList<>();
    
    @Column
    private Integer severity; // 1-10
    
    @Column
    private Integer duration; // en jours
    
    @ElementCollection
    @CollectionTable(name = "symptom_analysis_red_flags", joinColumns = @JoinColumn(name = "analysis_id"))
    @Column(name = "red_flag", length = 200)
    @Builder.Default
    private List<String> redFlags = new ArrayList<>();
    
    @ElementCollection
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "symptom_analysis_specialties", joinColumns = @JoinColumn(name = "analysis_id"))
    @Column(name = "specialty", length = 50)
    @Builder.Default
    private List<Specialite> suggestedSpecialties = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "symptom_analysis_questions", joinColumns = @JoinColumn(name = "analysis_id"))
    @Column(name = "question", columnDefinition = "TEXT")
    @Builder.Default
    private List<String> questions = new ArrayList<>();
    
    @Column(columnDefinition = "TEXT")
    private String summary;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean urgentRecommendation = false;
    
    @Column(columnDefinition = "TEXT")
    private String recommendationMessage;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

