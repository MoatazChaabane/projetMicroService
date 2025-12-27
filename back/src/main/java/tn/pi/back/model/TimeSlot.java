package tn.pi.back.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@Table(name = "time_slots", indexes = {
    @Index(name = "idx_timeslot_doctor", columnList = "doctor_id"),
    @Index(name = "idx_timeslot_jour", columnList = "jour")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private JourSemaine jour;
    
    @Column(nullable = false)
    private LocalTime heureDebut;
    
    @Column(nullable = false)
    private LocalTime heureFin;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean disponible = true;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean actif = true;
}

