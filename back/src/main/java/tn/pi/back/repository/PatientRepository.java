package tn.pi.back.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.pi.back.model.Patient;

import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    
    // Recherche par nom ou prénom (ignorant la casse)
    @Query("SELECT p FROM Patient p WHERE p.deleted = false AND " +
           "(LOWER(p.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.prenom) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Patient> findByNomOrPrenomContainingIgnoreCase(@Param("search") String search, Pageable pageable);
    
    // Recherche par téléphone
    @Query("SELECT p FROM Patient p WHERE p.deleted = false AND p.telephone LIKE CONCAT('%', :telephone, '%')")
    Page<Patient> findByTelephoneContaining(@Param("telephone") String telephone, Pageable pageable);
    
    // Recherche combinée (nom/prénom OU téléphone)
    @Query("SELECT p FROM Patient p WHERE p.deleted = false AND " +
           "((LOWER(p.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.prenom) LIKE LOWER(CONCAT('%', :search, '%'))) OR " +
           "p.telephone LIKE CONCAT('%', :search, '%'))")
    Page<Patient> searchPatients(@Param("search") String search, Pageable pageable);
    
    // Trouver un patient non supprimé par ID
    Optional<Patient> findByIdAndDeletedFalse(Long id);
    
    // Compter les patients non supprimés
    long countByDeletedFalse();
    
    // Trouver tous les patients non supprimés avec pagination
    Page<Patient> findByDeletedFalse(Pageable pageable);
}

