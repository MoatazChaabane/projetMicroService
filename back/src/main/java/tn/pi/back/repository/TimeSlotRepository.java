package tn.pi.back.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.pi.back.model.JourSemaine;
import tn.pi.back.model.TimeSlot;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

    List<TimeSlot> findByDoctorIdAndActifTrue(Long doctorId);

    @Query("SELECT ts FROM TimeSlot ts WHERE ts.doctor.id = :doctorId " +
           "AND ts.jour = :jour AND ts.disponible = true AND ts.actif = true")
    List<TimeSlot> findAvailableSlotsByDoctorAndDay(
            @Param("doctorId") Long doctorId,
            @Param("jour") JourSemaine jour);

    @Query("SELECT ts FROM TimeSlot ts WHERE ts.doctor.id = :doctorId " +
           "AND ts.jour = :jour AND ts.disponible = true AND ts.actif = true " +
           "AND ts.heureDebut <= :heure AND ts.heureFin > :heure")
    Optional<TimeSlot> findAvailableSlotAtTime(
            @Param("doctorId") Long doctorId,
            @Param("jour") JourSemaine jour,
            @Param("heure") LocalTime heure);

    void deleteByDoctorId(Long doctorId);
}

