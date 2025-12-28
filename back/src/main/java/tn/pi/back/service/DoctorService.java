package tn.pi.back.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.pi.back.dto.*;
import tn.pi.back.exception.ResourceNotFoundException;
import tn.pi.back.model.Doctor;
import tn.pi.back.model.JourSemaine;
import tn.pi.back.model.Specialite;
import tn.pi.back.model.TimeSlot;
import tn.pi.back.model.User;
import tn.pi.back.repository.DoctorRepository;
import tn.pi.back.repository.TimeSlotRepository;
import tn.pi.back.repository.UserRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorService {
    
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final TimeSlotRepository timeSlotRepository;
    
    @Transactional
    public DoctorResponseDTO createDoctor(DoctorRequestDTO requestDTO) {
        // Vérifier que l'utilisateur existe
        User user = userRepository.findById(requestDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'ID: " + requestDTO.getUserId()));
        
        // Vérifier que l'utilisateur n'a pas déjà un profil docteur
        if (doctorRepository.findByUserId(requestDTO.getUserId()).isPresent()) {
            throw new RuntimeException("Cet utilisateur a déjà un profil docteur");
        }
        
        Doctor doctor = Doctor.builder()
                .user(user)
                .specialite(requestDTO.getSpecialite())
                .nomClinique(requestDTO.getNomClinique())
                .adresse(requestDTO.getAdresse())
                .latitude(requestDTO.getLatitude())
                .longitude(requestDTO.getLongitude())
                .tarifConsultation(requestDTO.getTarifConsultation())
                .langues(requestDTO.getLangues() != null ? requestDTO.getLangues() : List.of())
                .teleconsultation(requestDTO.getTeleconsultation() != null ? requestDTO.getTeleconsultation() : false)
                .rating(java.math.BigDecimal.ZERO)
                .nombreAvis(0)
                .deleted(false)
                .build();
        
        Doctor savedDoctor = doctorRepository.save(doctor);
        
        // Créer les créneaux horaires si fournis
        if (requestDTO.getHoraires() != null && !requestDTO.getHoraires().isEmpty()) {
            List<TimeSlot> timeSlots = requestDTO.getHoraires().stream()
                    .map(slotDTO -> TimeSlot.builder()
                            .doctor(savedDoctor)
                            .jour(slotDTO.getJour())
                            .heureDebut(slotDTO.getHeureDebut())
                            .heureFin(slotDTO.getHeureFin())
                            .disponible(slotDTO.getDisponible() != null ? slotDTO.getDisponible() : true)
                            .actif(true)
                            .build())
                    .collect(Collectors.toList());
            timeSlotRepository.saveAll(timeSlots);
            savedDoctor.setHoraires(timeSlots);
        }
        
        log.info("Nouveau docteur créé: {} {}", savedDoctor.getUser().getFirstName(), savedDoctor.getUser().getLastName());
        return mapToResponseDTO(savedDoctor);
    }
    
    @Transactional(readOnly = true)
    public DoctorResponseDTO getDoctorById(Long id) {
        Doctor doctor = doctorRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Docteur non trouvé avec l'ID: " + id));
        return mapToResponseDTO(doctor);
    }
    
    @Transactional(readOnly = true)
    public DoctorResponseDTO getDoctorByUserId(Long userId) {
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Docteur non trouvé pour l'utilisateur ID: " + userId));
        return mapToResponseDTO(doctor);
    }
    
    @Transactional(readOnly = true)
    public PageResponse<DoctorResponseDTO> getAllDoctors(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Doctor> doctorsPage = doctorRepository.findByDeletedFalse(pageable);
        
        List<DoctorResponseDTO> content = doctorsPage.getContent().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
        
        return PageResponse.<DoctorResponseDTO>builder()
                .content(content)
                .page(doctorsPage.getNumber())
                .size(doctorsPage.getSize())
                .totalElements(doctorsPage.getTotalElements())
                .totalPages(doctorsPage.getTotalPages())
                .first(doctorsPage.isFirst())
                .last(doctorsPage.isLast())
                .build();
    }
    
    @Transactional(readOnly = true)
    public PageResponse<DoctorResponseDTO> searchDoctors(DoctorSearchDTO searchDTO, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Doctor> doctorsPage;
        
        // Recherche complète : spécialité + distance + disponibilité
        if (searchDTO.getSpecialite() != null 
                && searchDTO.getLatitude() != null 
                && searchDTO.getLongitude() != null 
                && searchDTO.getRayonKm() != null
                && searchDTO.getDate() != null
                && searchDTO.getHeure() != null) {
            
            JourSemaine jour = convertDateToJourSemaine(searchDTO.getDate());
            doctorsPage = doctorRepository.findDoctorsBySpecialiteDistanceAndDisponibilite(
                    searchDTO.getSpecialite().name(),
                    searchDTO.getLatitude(),
                    searchDTO.getLongitude(),
                    searchDTO.getRayonKm(),
                    jour.name(),
                    searchDTO.getHeure(),
                    pageable
            );
        }
        // Recherche : spécialité + distance
        else if (searchDTO.getSpecialite() != null 
                && searchDTO.getLatitude() != null 
                && searchDTO.getLongitude() != null 
                && searchDTO.getRayonKm() != null) {
            doctorsPage = doctorRepository.findDoctorsBySpecialiteAndDistance(
                    searchDTO.getSpecialite().name(),
                    searchDTO.getLatitude(),
                    searchDTO.getLongitude(),
                    searchDTO.getRayonKm(),
                    pageable
            );
        }
        // Recherche : spécialité + téléconsultation
        else if (searchDTO.getSpecialite() != null && searchDTO.getTeleconsultation() != null) {
            doctorsPage = doctorRepository.findBySpecialiteAndTeleconsultation(
                    searchDTO.getSpecialite(),
                    searchDTO.getTeleconsultation(),
                    pageable
            );
        }
        // Recherche : spécialité + rating minimum
        else if (searchDTO.getSpecialite() != null && searchDTO.getRatingMin() != null) {
            doctorsPage = doctorRepository.findBySpecialiteAndRatingMin(
                    searchDTO.getSpecialite(),
                    searchDTO.getRatingMin(),
                    pageable
            );
        }
        // Recherche : spécialité seule
        else if (searchDTO.getSpecialite() != null) {
            doctorsPage = doctorRepository.findBySpecialite(searchDTO.getSpecialite(), pageable);
        }
        // Recherche : distance seule
        else if (searchDTO.getLatitude() != null 
                && searchDTO.getLongitude() != null 
                && searchDTO.getRayonKm() != null) {
            doctorsPage = doctorRepository.findDoctorsByDistance(
                    searchDTO.getLatitude(),
                    searchDTO.getLongitude(),
                    searchDTO.getRayonKm(),
                    pageable
            );
        }
        // Recherche : disponibilité seule
        else if (searchDTO.getDate() != null && searchDTO.getHeure() != null) {
            JourSemaine jour = convertDateToJourSemaine(searchDTO.getDate());
            doctorsPage = doctorRepository.findDoctorsByDisponibilite(
                    jour.name(),
                    searchDTO.getHeure(),
                    pageable
            );
        }
        // Par défaut : tous les docteurs
        else {
            doctorsPage = doctorRepository.findByDeletedFalse(pageable);
        }
        
        List<DoctorResponseDTO> content = doctorsPage.getContent().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
        
        return PageResponse.<DoctorResponseDTO>builder()
                .content(content)
                .page(doctorsPage.getNumber())
                .size(doctorsPage.getSize())
                .totalElements(doctorsPage.getTotalElements())
                .totalPages(doctorsPage.getTotalPages())
                .first(doctorsPage.isFirst())
                .last(doctorsPage.isLast())
                .build();
    }
    
    @Transactional
    public DoctorResponseDTO updateDoctor(Long id, DoctorRequestDTO requestDTO) {
        Doctor doctor = doctorRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Docteur non trouvé avec l'ID: " + id));
        
        doctor.setSpecialite(requestDTO.getSpecialite());
        doctor.setNomClinique(requestDTO.getNomClinique());
        doctor.setAdresse(requestDTO.getAdresse());
        doctor.setLatitude(requestDTO.getLatitude());
        doctor.setLongitude(requestDTO.getLongitude());
        doctor.setTarifConsultation(requestDTO.getTarifConsultation());
        doctor.setLangues(requestDTO.getLangues() != null ? requestDTO.getLangues() : List.of());
        doctor.setTeleconsultation(requestDTO.getTeleconsultation() != null ? requestDTO.getTeleconsultation() : false);
        
        // Mettre à jour les créneaux horaires
        if (requestDTO.getHoraires() != null) {
            // Supprimer les anciens créneaux
            timeSlotRepository.deleteByDoctorId(id);
            
            // Créer les nouveaux créneaux
            List<TimeSlot> timeSlots = requestDTO.getHoraires().stream()
                    .map(slotDTO -> TimeSlot.builder()
                            .doctor(doctor)
                            .jour(slotDTO.getJour())
                            .heureDebut(slotDTO.getHeureDebut())
                            .heureFin(slotDTO.getHeureFin())
                            .disponible(slotDTO.getDisponible() != null ? slotDTO.getDisponible() : true)
                            .actif(true)
                            .build())
                    .collect(Collectors.toList());
            timeSlotRepository.saveAll(timeSlots);
            doctor.setHoraires(timeSlots);
        }
        
        Doctor updatedDoctor = doctorRepository.save(doctor);
        log.info("Docteur mis à jour: {} {}", updatedDoctor.getUser().getFirstName(), updatedDoctor.getUser().getLastName());
        return mapToResponseDTO(updatedDoctor);
    }
    
    @Transactional
    public void deleteDoctor(Long id) {
        Doctor doctor = doctorRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Docteur non trouvé avec l'ID: " + id));
        doctor.setDeleted(true);
        doctorRepository.save(doctor);
        log.info("Docteur supprimé (soft delete): {} {}", doctor.getUser().getFirstName(), doctor.getUser().getLastName());
    }
    
    @Transactional(readOnly = true)
    public long countDoctors() {
        return doctorRepository.countByDeletedFalse();
    }
    
    @Transactional(readOnly = true)
    public List<DoctorMatchResponseDTO> matchDoctors(DoctorMatchRequestDTO matchRequest) {
        // Récupérer les docteurs dans le rayon
        List<Doctor> doctors;
        if (matchRequest.getSpecialite() != null) {
            doctors = doctorRepository.findDoctorsBySpecialiteWithinRadiusForMatching(
                    matchRequest.getSpecialite().name(),
                    matchRequest.getLatitude(),
                    matchRequest.getLongitude(),
                    matchRequest.getRayonKm()
            );
        } else {
            doctors = doctorRepository.findDoctorsWithinRadiusForMatching(
                    matchRequest.getLatitude(),
                    matchRequest.getLongitude(),
                    matchRequest.getRayonKm()
            );
        }
        
        // Analyser les symptômes pour déterminer les spécialités pertinentes
        List<Specialite> suggestedSpecialites = matchRequest.getSpecialite() != null
                ? List.of(matchRequest.getSpecialite())
                : mapSymptomsToSpecialities(matchRequest.getSymptomes(), matchRequest.getTags());
        
        // Calculer les scores pour chaque docteur
        List<DoctorMatchResponseDTO> matches = doctors.stream()
                .map(doctor -> calculateMatchScore(doctor, matchRequest, suggestedSpecialites))
                .filter(match -> match.getScoreTotal() > 0) // Filtrer les scores nuls
                .sorted((a, b) -> Double.compare(b.getScoreTotal(), a.getScoreTotal())) // Trier par score décroissant
                .limit(matchRequest.getLimit() != null ? matchRequest.getLimit() : 10)
                .collect(Collectors.toList());
        
        log.info("Matching de {} docteurs trouvés pour les symptômes: {}", matches.size(), matchRequest.getSymptomes());
        return matches;
    }
    
    private DoctorMatchResponseDTO calculateMatchScore(Doctor doctor, DoctorMatchRequestDTO matchRequest, List<Specialite> suggestedSpecialites) {
        // Calculer la distance
        Double distance = calculateDistance(
                matchRequest.getLatitude(),
                matchRequest.getLongitude(),
                doctor.getLatitude(),
                doctor.getLongitude()
        );
        
        // Score symptômes/spécialité (pondération: 40%)
        double scoreSymptomes = calculateSymptomScore(doctor.getSpecialite(), suggestedSpecialites);
        
        // Score distance (pondération: 30%) - plus proche = score plus élevé
        double scoreDistance = calculateDistanceScore(distance, matchRequest.getRayonKm());
        
        // Score disponibilité (pondération: 30%)
        double scoreDisponibilite = 0.0;
        boolean disponible = false;
        if (matchRequest.getDateSouhaitee() != null) {
            disponible = checkDoctorAvailability(doctor.getId(), matchRequest.getDateSouhaitee());
            scoreDisponibilite = disponible ? 1.0 : 0.0;
        } else {
            // Si pas de date, on donne un score neutre
            scoreDisponibilite = 0.5;
        }
        
        // Score total pondéré
        double scoreTotal = (scoreSymptomes * 0.4) + (scoreDistance * 0.3) + (scoreDisponibilite * 0.3);
        
        // Message explicatif
        String message = buildMatchMessage(doctor.getSpecialite(), distance, disponible, scoreSymptomes);
        
        return DoctorMatchResponseDTO.builder()
                .doctor(mapToResponseDTO(doctor))
                .scoreTotal(scoreTotal)
                .scoreSymptomes(scoreSymptomes)
                .scoreDistance(scoreDistance)
                .scoreDisponibilite(scoreDisponibilite)
                .distanceKm(distance)
                .disponible(disponible || matchRequest.getDateSouhaitee() == null)
                .message(message)
                .build();
    }
    
    private double calculateSymptomScore(Specialite doctorSpecialite, List<Specialite> suggestedSpecialites) {
        if (suggestedSpecialites.isEmpty()) {
            return 0.5; // Score neutre si pas de spécialités suggérées
        }
        if (suggestedSpecialites.contains(doctorSpecialite)) {
            return 1.0; // Score parfait si correspond exactement
        }
        // Score partiel si spécialité générale est suggérée
        if (suggestedSpecialites.contains(Specialite.MEDECINE_GENERALE) && 
            doctorSpecialite != Specialite.MEDECINE_GENERALE) {
            return 0.7; // Les généralistes peuvent traiter beaucoup de cas
        }
        return 0.0; // Aucune correspondance
    }
    
    private double calculateDistanceScore(Double distanceKm, Double maxRayonKm) {
        if (distanceKm == null || maxRayonKm == null || maxRayonKm <= 0) {
            return 0.5; // Score neutre si pas de distance
        }
        // Score inverse : plus proche = score plus élevé
        // Normalisé entre 0 et 1 : score = 1 - (distance / maxRayon)
        double normalizedDistance = Math.min(distanceKm / maxRayonKm, 1.0);
        return 1.0 - normalizedDistance;
    }
    
    private boolean checkDoctorAvailability(Long doctorId, LocalDate date) {
        try {
            JourSemaine jour = convertDateToJourSemaine(date);
            // Vérifier si le docteur a au moins un créneau disponible ce jour-là
            List<TimeSlot> availableSlots = timeSlotRepository.findAvailableSlotsByDoctorAndDay(doctorId, jour);
            return !availableSlots.isEmpty();
        } catch (Exception e) {
            log.warn("Erreur lors de la vérification de disponibilité pour docteur {} à la date {}", doctorId, date);
            return false;
        }
    }
    
    private List<Specialite> mapSymptomsToSpecialities(String symptomes, List<String> tags) {
        List<String> symptomTokens = new ArrayList<>();
        
        // Normaliser le texte des symptômes
        if (symptomes != null && !symptomes.trim().isEmpty()) {
            String[] words = symptomes.toLowerCase()
                    .replaceAll("[^a-zàâäéèêëïîôùûüÿç\\s]", " ")
                    .split("\\s+");
            symptomTokens.addAll(Arrays.asList(words));
        }
        
        // Ajouter les tags
        if (tags != null) {
            tags.stream()
                    .map(String::toLowerCase)
                    .forEach(symptomTokens::add);
        }
        
        // Map de correspondance symptômes -> spécialités
        Map<String, Specialite> symptomMap = new HashMap<>();
        symptomMap.put("cœur", Specialite.CARDIOLOGIE);
        symptomMap.put("cardiaque", Specialite.CARDIOLOGIE);
        symptomMap.put("thorax", Specialite.CARDIOLOGIE);
        symptomMap.put("thoracique", Specialite.CARDIOLOGIE);
        symptomMap.put("tension", Specialite.CARDIOLOGIE);
        symptomMap.put("hypertension", Specialite.CARDIOLOGIE);
        symptomMap.put("essoufflement", Specialite.CARDIOLOGIE);
        symptomMap.put("peau", Specialite.DERMATOLOGIE);
        symptomMap.put("dermatologique", Specialite.DERMATOLOGIE);
        symptomMap.put("acné", Specialite.DERMATOLOGIE);
        symptomMap.put("eczéma", Specialite.DERMATOLOGIE);
        symptomMap.put("psoriasis", Specialite.DERMATOLOGIE);
        symptomMap.put("diabète", Specialite.ENDOCRINOLOGIE);
        symptomMap.put("thyroïde", Specialite.ENDOCRINOLOGIE);
        symptomMap.put("hormone", Specialite.ENDOCRINOLOGIE);
        symptomMap.put("ventre", Specialite.GASTROENTEROLOGIE);
        symptomMap.put("estomac", Specialite.GASTROENTEROLOGIE);
        symptomMap.put("digestion", Specialite.GASTROENTEROLOGIE);
        symptomMap.put("intestin", Specialite.GASTROENTEROLOGIE);
        symptomMap.put("femme", Specialite.GYNECOLOGIE);
        symptomMap.put("gynécologique", Specialite.GYNECOLOGIE);
        symptomMap.put("grossesse", Specialite.GYNECOLOGIE);
        symptomMap.put("cerveau", Specialite.NEUROLOGIE);
        symptomMap.put("neurologique", Specialite.NEUROLOGIE);
        symptomMap.put("migraine", Specialite.NEUROLOGIE);
        symptomMap.put("cancer", Specialite.ONCOLOGIE);
        symptomMap.put("oncologique", Specialite.ONCOLOGIE);
        symptomMap.put("œil", Specialite.OPHTALMOLOGIE);
        symptomMap.put("vision", Specialite.OPHTALMOLOGIE);
        symptomMap.put("vue", Specialite.OPHTALMOLOGIE);
        symptomMap.put("os", Specialite.ORTHOPEDIE);
        symptomMap.put("articulation", Specialite.ORTHOPEDIE);
        symptomMap.put("fracture", Specialite.ORTHOPEDIE);
        symptomMap.put("enfant", Specialite.PEDIATRIE);
        symptomMap.put("bébé", Specialite.PEDIATRIE);
        symptomMap.put("mental", Specialite.PSYCHIATRIE);
        symptomMap.put("psychologique", Specialite.PSYCHIATRIE);
        symptomMap.put("dépression", Specialite.PSYCHIATRIE);
        symptomMap.put("poumon", Specialite.PNEUMOLOGIE);
        symptomMap.put("respiration", Specialite.PNEUMOLOGIE);
        symptomMap.put("asthme", Specialite.PNEUMOLOGIE);
        symptomMap.put("rhumatisme", Specialite.RHUMATOLOGIE);
        symptomMap.put("urinaire", Specialite.UROLOGIE);
        symptomMap.put("rein", Specialite.UROLOGIE);
        
        Set<Specialite> matchedSpecialites = new HashSet<>();
        
        // Rechercher les correspondances
        for (String token : symptomTokens) {
            for (Map.Entry<String, Specialite> entry : symptomMap.entrySet()) {
                if (token.contains(entry.getKey()) || entry.getKey().contains(token)) {
                    matchedSpecialites.add(entry.getValue());
                }
            }
        }
        
        // Si aucune correspondance, suggérer médecine générale
        if (matchedSpecialites.isEmpty()) {
            matchedSpecialites.add(Specialite.MEDECINE_GENERALE);
        }
        
        return new ArrayList<>(matchedSpecialites);
    }
    
    private Double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return null;
        }
        
        final int EARTH_RADIUS_KM = 6371;
        
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS_KM * c;
    }
    
    private String buildMatchMessage(Specialite specialite, Double distance, boolean disponible, double scoreSymptomes) {
        StringBuilder message = new StringBuilder();
        message.append("Spécialité: ").append(specialite.name().replace("_", " "));
        if (distance != null) {
            message.append(" - Distance: ").append(String.format("%.2f", distance)).append(" km");
        }
        if (disponible) {
            message.append(" - Disponible");
        } else {
            message.append(" - Non disponible à la date souhaitée");
        }
        if (scoreSymptomes >= 0.9) {
            message.append(" - Correspondance parfaite avec vos symptômes");
        }
        return message.toString();
    }
    
    // Méthode utilitaire pour convertir une date en jour de la semaine
    private JourSemaine convertDateToJourSemaine(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return switch (dayOfWeek) {
            case MONDAY -> JourSemaine.LUNDI;
            case TUESDAY -> JourSemaine.MARDI;
            case WEDNESDAY -> JourSemaine.MERCREDI;
            case THURSDAY -> JourSemaine.JEUDI;
            case FRIDAY -> JourSemaine.VENDREDI;
            case SATURDAY -> JourSemaine.SAMEDI;
            case SUNDAY -> JourSemaine.DIMANCHE;
        };
    }
    
    private DoctorResponseDTO mapToResponseDTO(Doctor doctor) {
        List<TimeSlotDTO> horairesDTO = doctor.getHoraires() != null 
                ? doctor.getHoraires().stream()
                        .filter(TimeSlot::getActif)
                        .map(ts -> TimeSlotDTO.builder()
                                .id(ts.getId())
                                .jour(ts.getJour())
                                .heureDebut(ts.getHeureDebut())
                                .heureFin(ts.getHeureFin())
                                .disponible(ts.getDisponible())
                                .build())
                        .collect(Collectors.toList())
                : List.of();
        
        return DoctorResponseDTO.builder()
                .id(doctor.getId())
                .userId(doctor.getUser().getId())
                .nomComplet("Dr. " + doctor.getUser().getFirstName() + " " + doctor.getUser().getLastName())
                .email(doctor.getUser().getEmail())
                .telephone(doctor.getUser().getPhoneNumber())
                .specialite(doctor.getSpecialite())
                .nomClinique(doctor.getNomClinique())
                .adresse(doctor.getAdresse())
                .latitude(doctor.getLatitude())
                .longitude(doctor.getLongitude())
                .tarifConsultation(doctor.getTarifConsultation())
                .langues(doctor.getLangues())
                .rating(doctor.getRating())
                .nombreAvis(doctor.getNombreAvis())
                .teleconsultation(doctor.getTeleconsultation())
                .horaires(horairesDTO)
                .createdAt(doctor.getCreatedAt())
                .updatedAt(doctor.getUpdatedAt())
                .build();
    }
}

