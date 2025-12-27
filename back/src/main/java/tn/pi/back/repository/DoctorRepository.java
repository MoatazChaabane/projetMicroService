package tn.pi.back.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.pi.back.model.Doctor;
import tn.pi.back.model.Specialite;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    
    // Trouver un docteur non supprimé par ID
    @Query("SELECT d FROM Doctor d WHERE d.id = :id AND d.deleted = false")
    Optional<Doctor> findByIdAndDeletedFalse(@Param("id") Long id);
    
    // Trouver un docteur par user ID
    @Query("SELECT d FROM Doctor d WHERE d.user.id = :userId AND d.deleted = false")
    Optional<Doctor> findByUserId(@Param("userId") Long userId);
    
    // Recherche par spécialité
    @Query("SELECT d FROM Doctor d WHERE d.deleted = false AND d.specialite = :specialite")
    Page<Doctor> findBySpecialite(@Param("specialite") Specialite specialite, Pageable pageable);
    
    // Recherche par spécialité et téléconsultation
    @Query("SELECT d FROM Doctor d WHERE d.deleted = false AND d.specialite = :specialite AND d.teleconsultation = :teleconsultation")
    Page<Doctor> findBySpecialiteAndTeleconsultation(
            @Param("specialite") Specialite specialite,
            @Param("teleconsultation") Boolean teleconsultation,
            Pageable pageable);
    
    // Recherche par spécialité avec note minimum
    @Query("SELECT d FROM Doctor d WHERE d.deleted = false AND d.specialite = :specialite AND d.rating >= :ratingMin")
    Page<Doctor> findBySpecialiteAndRatingMin(
            @Param("specialite") Specialite specialite,
            @Param("ratingMin") Double ratingMin,
            Pageable pageable);
    
    // Recherche par distance (formule Haversine en SQL natif)
    // Distance en kilomètres
    @Query(value = "SELECT d.*, " +
            "(6371 * acos(cos(radians(:latitude)) * cos(radians(d.latitude)) * " +
            "cos(radians(d.longitude) - radians(:longitude)) + " +
            "sin(radians(:latitude)) * sin(radians(d.latitude)))) AS distance " +
            "FROM doctors d " +
            "WHERE d.deleted = false " +
            "AND d.latitude IS NOT NULL AND d.longitude IS NOT NULL " +
            "HAVING distance <= :rayonKm " +
            "ORDER BY distance",
            nativeQuery = true)
    Page<Doctor> findDoctorsByDistance(
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("rayonKm") Double rayonKm,
            Pageable pageable);
    
    // Recherche combinée : spécialité + distance
    @Query(value = "SELECT d.*, " +
            "(6371 * acos(cos(radians(:latitude)) * cos(radians(d.latitude)) * " +
            "cos(radians(d.longitude) - radians(:longitude)) + " +
            "sin(radians(:latitude)) * sin(radians(d.latitude)))) AS distance " +
            "FROM doctors d " +
            "WHERE d.deleted = false " +
            "AND d.specialite = :specialite " +
            "AND d.latitude IS NOT NULL AND d.longitude IS NOT NULL " +
            "HAVING distance <= :rayonKm " +
            "ORDER BY distance",
            nativeQuery = true)
    Page<Doctor> findDoctorsBySpecialiteAndDistance(
            @Param("specialite") String specialite,
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("rayonKm") Double rayonKm,
            Pageable pageable);
    
    // Recherche avec disponibilité (vérifie si un créneau existe pour le jour et l'heure)
    @Query("SELECT DISTINCT d FROM Doctor d " +
            "JOIN d.horaires h " +
            "WHERE d.deleted = false " +
            "AND h.jour = :jour " +
            "AND h.disponible = true " +
            "AND h.actif = true " +
            "AND h.heureDebut <= :heure " +
            "AND h.heureFin > :heure")
    Page<Doctor> findDoctorsByDisponibilite(
            @Param("jour") String jour,
            @Param("heure") LocalTime heure,
            Pageable pageable);
    
    // Recherche complète : spécialité + distance + disponibilité
    @Query(value = "SELECT DISTINCT d.*, " +
            "(6371 * acos(cos(radians(:latitude)) * cos(radians(d.latitude)) * " +
            "cos(radians(d.longitude) - radians(:longitude)) + " +
            "sin(radians(:latitude)) * sin(radians(d.latitude)))) AS distance " +
            "FROM doctors d " +
            "INNER JOIN time_slots ts ON ts.doctor_id = d.id " +
            "WHERE d.deleted = false " +
            "AND d.specialite = :specialite " +
            "AND d.latitude IS NOT NULL AND d.longitude IS NOT NULL " +
            "AND ts.jour = :jour " +
            "AND ts.disponible = true " +
            "AND ts.actif = true " +
            "AND ts.heure_debut <= :heure " +
            "AND ts.heure_fin > :heure " +
            "HAVING distance <= :rayonKm " +
            "ORDER BY distance",
            nativeQuery = true)
    Page<Doctor> findDoctorsBySpecialiteDistanceAndDisponibilite(
            @Param("specialite") String specialite,
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("rayonKm") Double rayonKm,
            @Param("jour") String jour,
            @Param("heure") LocalTime heure,
            Pageable pageable);
    
    // Compter les docteurs non supprimés
    long countByDeletedFalse();
    
    // Trouver tous les docteurs non supprimés avec pagination
    Page<Doctor> findByDeletedFalse(Pageable pageable);
}

