package tn.pi.back.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.pi.back.dto.SymptomAnalysisRequestDTO;
import tn.pi.back.dto.SymptomAnalysisResponseDTO;
import tn.pi.back.exception.ResourceNotFoundException;
import tn.pi.back.model.*;
import tn.pi.back.repository.AppointmentRepository;
import tn.pi.back.repository.PatientRepository;
import tn.pi.back.repository.SymptomAnalysisRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SymptomAnalysisService {
    
    private final SymptomAnalysisRepository symptomAnalysisRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    
    // Mapping des mots-clés vers les symptômes structurés
    private static final Map<String, String> SYMPTOM_KEYWORDS = createSymptomKeywords();
    
    // Red flags (symptômes nécessitant une attention urgente)
    private static final Map<String, String> RED_FLAG_KEYWORDS = createRedFlagKeywords();
    
    // Mapping symptômes -> spécialités
    private static final Map<String, Specialite> SYMPTOM_TO_SPECIALTY = createSymptomToSpecialtyMap();
    
    @Transactional
    public SymptomAnalysisResponseDTO analyzeSymptoms(SymptomAnalysisRequestDTO request) {
        // Vérifier que le patient existe
        Patient patient = patientRepository.findByIdAndDeletedFalse(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient non trouvé avec l'ID: " + request.getPatientId()));
        
        // Vérifier le rendez-vous si fourni
        Appointment appointment = null;
        if (request.getAppointmentId() != null) {
            appointment = appointmentRepository.findById(request.getAppointmentId())
                    .filter(a -> !a.getDeleted())
                    .orElseThrow(() -> new ResourceNotFoundException("Rendez-vous non trouvé avec l'ID: " + request.getAppointmentId()));
        }
        
        String description = request.getDescription().toLowerCase();
        
        // Extraire les symptômes
        List<String> symptoms = extractSymptoms(description);
        
        // Détecter les red flags
        List<String> redFlags = detectRedFlags(description);
        
        // Extraire la sévérité (1-10)
        Integer severity = extractSeverity(description);
        
        // Extraire la durée
        Integer duration = extractDuration(description);
        
        // Suggérer des spécialités
        List<Specialite> suggestedSpecialties = suggestSpecialties(symptoms, description);
        
        // Générer des questions de clarification
        List<String> questions = generateQuestions(symptoms, severity, duration, redFlags);
        
        // Générer le résumé pour le docteur
        String summary = generateSummary(symptoms, severity, duration, redFlags, suggestedSpecialties);
        
        // Déterminer si c'est urgent
        boolean urgent = !redFlags.isEmpty() || (severity != null && severity >= 8);
        String recommendationMessage = generateRecommendationMessage(urgent, redFlags);
        String safetyWarning = "⚠️ IMPORTANT: Cette analyse n'est pas un diagnostic médical. " +
                               "Consultez toujours un professionnel de santé pour un diagnostic précis.";
        
        // Sauvegarder l'analyse
        SymptomAnalysis analysis = SymptomAnalysis.builder()
                .patient(patient)
                .appointment(appointment)
                .originalDescription(request.getDescription())
                .symptoms(symptoms)
                .severity(severity)
                .duration(duration)
                .redFlags(redFlags)
                .suggestedSpecialties(suggestedSpecialties)
                .questions(questions)
                .summary(summary)
                .urgentRecommendation(urgent)
                .recommendationMessage(recommendationMessage)
                .build();
        
        SymptomAnalysis saved = symptomAnalysisRepository.save(analysis);
        log.info("Analyse de symptômes créée pour patient {}: {} symptômes, {} red flags", 
                patient.getId(), symptoms.size(), redFlags.size());
        
        return mapToResponseDTO(saved, safetyWarning);
    }
    
    @Transactional(readOnly = true)
    public SymptomAnalysisResponseDTO getAnalysisById(Long id) {
        SymptomAnalysis analysis = symptomAnalysisRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Analyse non trouvée avec l'ID: " + id));
        
        String safetyWarning = "⚠️ IMPORTANT: Cette analyse n'est pas un diagnostic médical. " +
                               "Consultez toujours un professionnel de santé pour un diagnostic précis.";
        
        return mapToResponseDTO(analysis, safetyWarning);
    }
    
    @Transactional(readOnly = true)
    public List<SymptomAnalysisResponseDTO> getPatientAnalyses(Long patientId) {
        List<SymptomAnalysis> analyses = symptomAnalysisRepository.findByPatientIdOrderByCreatedAtDesc(patientId);
        String safetyWarning = "⚠️ IMPORTANT: Cette analyse n'est pas un diagnostic médical. " +
                               "Consultez toujours un professionnel de santé pour un diagnostic précis.";
        
        return analyses.stream()
                .map(analysis -> mapToResponseDTO(analysis, safetyWarning))
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public SymptomAnalysisResponseDTO getAnalysisByAppointmentId(Long appointmentId) {
        SymptomAnalysis analysis = symptomAnalysisRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Aucune analyse trouvée pour ce rendez-vous"));
        
        String safetyWarning = "⚠️ IMPORTANT: Cette analyse n'est pas un diagnostic médical. " +
                               "Consultez toujours un professionnel de santé pour un diagnostic précis.";
        
        return mapToResponseDTO(analysis, safetyWarning);
    }
    
    private List<String> extractSymptoms(String description) {
        Set<String> foundSymptoms = new HashSet<>();
        String normalized = normalizeText(description);
        
        for (Map.Entry<String, String> entry : SYMPTOM_KEYWORDS.entrySet()) {
            if (normalized.contains(entry.getKey())) {
                foundSymptoms.add(entry.getValue());
            }
        }
        
        return new ArrayList<>(foundSymptoms);
    }
    
    private List<String> detectRedFlags(String description) {
        List<String> flags = new ArrayList<>();
        String normalized = normalizeText(description);
        
        for (Map.Entry<String, String> entry : RED_FLAG_KEYWORDS.entrySet()) {
            if (normalized.contains(entry.getKey())) {
                flags.add(entry.getValue());
            }
        }
        
        return flags;
    }
    
    private Integer extractSeverity(String description) {
        // Rechercher des mentions de sévérité (1-10, léger, modéré, sévère, etc.)
        Pattern severityPattern = Pattern.compile("\\b([0-9]|10)\\s*/(10|10)|sévère|grave|très (douloureux|fort)|intense");
        if (severityPattern.matcher(description).find()) {
            // Extraire un nombre si présent
            Pattern numberPattern = Pattern.compile("\\b([0-9]|10)\\b");
            java.util.regex.Matcher matcher = numberPattern.matcher(description);
            if (matcher.find()) {
                try {
                    return Integer.parseInt(matcher.group(1));
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
            // Sinon, estimer selon les mots-clés
            if (description.contains("sévère") || description.contains("grave") || description.contains("intense")) {
                return 8;
            } else if (description.contains("modéré") || description.contains("moyen")) {
                return 5;
            } else if (description.contains("léger") || description.contains("faible")) {
                return 3;
            }
        }
        return null;
    }
    
    private Integer extractDuration(String description) {
        // Rechercher des mentions de durée (jours, semaines, mois)
        Pattern dayPattern = Pattern.compile("\\b(\\d+)\\s*(jour|jours)\\b");
        java.util.regex.Matcher dayMatcher = dayPattern.matcher(description);
        if (dayMatcher.find()) {
            try {
                return Integer.parseInt(dayMatcher.group(1));
            } catch (NumberFormatException e) {
                // Ignore
            }
        }
        
        Pattern weekPattern = Pattern.compile("\\b(\\d+)\\s*(semaine|semaines)\\b");
        java.util.regex.Matcher weekMatcher = weekPattern.matcher(description);
        if (weekMatcher.find()) {
            try {
                return Integer.parseInt(weekMatcher.group(1)) * 7;
            } catch (NumberFormatException e) {
                // Ignore
            }
        }
        
        return null;
    }
    
    private List<Specialite> suggestSpecialties(List<String> symptoms, String description) {
        Set<Specialite> specialties = new HashSet<>();
        String normalized = normalizeText(description);
        
        for (String symptom : symptoms) {
            for (Map.Entry<String, Specialite> entry : SYMPTOM_TO_SPECIALTY.entrySet()) {
                if (symptom.toLowerCase().contains(entry.getKey()) || normalized.contains(entry.getKey())) {
                    specialties.add(entry.getValue());
                }
            }
        }
        
        // Si aucune spécialité trouvée, suggérer médecine générale
        if (specialties.isEmpty()) {
            specialties.add(Specialite.MEDECINE_GENERALE);
        }
        
        return new ArrayList<>(specialties);
    }
    
    private List<String> generateQuestions(List<String> symptoms, Integer severity, Integer duration, List<String> redFlags) {
        List<String> questions = new ArrayList<>();
        
        if (symptoms.isEmpty()) {
            questions.add("Pouvez-vous décrire plus précisément vos symptômes ?");
        }
        
        if (severity == null) {
            questions.add("Sur une échelle de 1 à 10, comment évalueriez-vous l'intensité de vos symptômes ?");
        }
        
        if (duration == null) {
            questions.add("Depuis combien de temps ressentez-vous ces symptômes ?");
        }
        
        if (!redFlags.isEmpty()) {
            questions.add("Les symptômes s'aggravent-ils rapidement ?");
            questions.add("Avez-vous d'autres symptômes associés (fièvre, nausées, vertiges) ?");
        }
        
        // Questions spécifiques selon les symptômes
        if (symptoms.stream().anyMatch(s -> s.contains("douleur thoracique") || s.contains("cœur"))) {
            questions.add("La douleur irradie-t-elle vers le bras gauche, la mâchoire ou le dos ?");
            questions.add("Avez-vous des antécédents cardiaques ?");
        }
        
        if (symptoms.stream().anyMatch(s -> s.contains("essoufflement") || s.contains("respiration"))) {
            questions.add("L'essoufflement survient-il au repos ou seulement à l'effort ?");
            questions.add("Avez-vous de la toux ou des expectorations ?");
        }
        
        if (symptoms.stream().anyMatch(s -> s.contains("maux de tête") || s.contains("céphalée"))) {
            questions.add("Avez-vous déjà eu ce type de maux de tête auparavant ?");
            questions.add("Les maux de tête sont-ils accompagnés de nausées ou de sensibilité à la lumière ?");
        }
        
        return questions;
    }
    
    private String generateSummary(List<String> symptoms, Integer severity, Integer duration, 
                                   List<String> redFlags, List<Specialite> specialties) {
        StringBuilder summary = new StringBuilder();
        
        summary.append("Résumé de l'analyse des symptômes:\n\n");
        
        summary.append("Symptômes identifiés: ");
        if (symptoms.isEmpty()) {
            summary.append("Non spécifiés");
        } else {
            summary.append(String.join(", ", symptoms));
        }
        summary.append("\n");
        
        if (severity != null) {
            summary.append("Sévérité: ").append(severity).append("/10\n");
        }
        
        if (duration != null) {
            summary.append("Durée: ").append(duration).append(" jour(s)\n");
        }
        
        if (!redFlags.isEmpty()) {
            summary.append("\n⚠️ Indicateurs d'urgence détectés: ").append(String.join(", ", redFlags)).append("\n");
        }
        
        if (!specialties.isEmpty()) {
            summary.append("\nSpécialités suggérées: ");
            summary.append(specialties.stream()
                    .map(s -> s.name().replace("_", " "))
                    .collect(Collectors.joining(", ")));
            summary.append("\n");
        }
        
        return summary.toString();
    }
    
    private String generateRecommendationMessage(boolean urgent, List<String> redFlags) {
        if (urgent) {
            return "⚠️ URGENCE MÉDICALE: Consultez immédiatement un médecin ou appelez le 190 (SAMU). " +
                   "Les symptômes décrits nécessitent une attention médicale urgente.";
        } else if (!redFlags.isEmpty()) {
            return "⚠️ Consultez un médecin dans les plus brefs délais si les symptômes persistent ou s'aggravent.";
        } else {
            return "Il est recommandé de consulter un médecin pour une évaluation appropriée de vos symptômes.";
        }
    }
    
    private String normalizeText(String text) {
        return text.toLowerCase()
                .replaceAll("[^a-zàâäéèêëïîôùûüÿç\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
    
    private SymptomAnalysisResponseDTO mapToResponseDTO(SymptomAnalysis analysis, String safetyWarning) {
        return SymptomAnalysisResponseDTO.builder()
                .id(analysis.getId())
                .symptoms(analysis.getSymptoms())
                .severity(analysis.getSeverity())
                .duration(analysis.getDuration())
                .redFlags(analysis.getRedFlags())
                .suggestedSpecialties(analysis.getSuggestedSpecialties())
                .questions(analysis.getQuestions())
                .summary(analysis.getSummary())
                .urgentRecommendation(analysis.getUrgentRecommendation())
                .recommendationMessage(analysis.getRecommendationMessage())
                .safetyWarning(safetyWarning)
                .build();
    }
    
    // Méthodes de création des maps statiques
    private static Map<String, String> createSymptomKeywords() {
        Map<String, String> map = new HashMap<>();
        map.put("douleur thoracique", "douleur thoracique");
        map.put("douleur poitrine", "douleur thoracique");
        map.put("essoufflement", "essoufflement");
        map.put("difficulté respirer", "essoufflement");
        map.put("maux de tête", "maux de tête");
        map.put("céphalée", "maux de tête");
        map.put("fièvre", "fièvre");
        map.put("toux", "toux");
        map.put("nausée", "nausées");
        map.put("vomissement", "vomissements");
        map.put("douleur ventre", "douleur abdominale");
        map.put("douleur estomac", "douleur abdominale");
        map.put("diarrhée", "diarrhée");
        map.put("constipation", "constipation");
        map.put("vertige", "vertiges");
        map.put("fatigue", "fatigue");
        map.put("douleur articulation", "douleur articulaire");
        map.put("rougeur", "rougeur");
        map.put("démangeaison", "démangeaisons");
        map.put("éruption", "éruption cutanée");
        return map;
    }
    
    private static Map<String, String> createRedFlagKeywords() {
        Map<String, String> map = new HashMap<>();
        map.put("douleur thoracique sévère", "douleur thoracique sévère");
        map.put("essoufflement repos", "essoufflement au repos");
        map.put("perte connaissance", "perte de conscience");
        map.put("saignement important", "saignement important");
        map.put("traumatisme crânien", "traumatisme crânien");
        map.put("fièvre élevée", "fièvre élevée (>39°C)");
        map.put("douleur insupportable", "douleur insupportable");
        return map;
    }
    
    private static Map<String, Specialite> createSymptomToSpecialtyMap() {
        Map<String, Specialite> map = new HashMap<>();
        map.put("cœur", Specialite.CARDIOLOGIE);
        map.put("cardiaque", Specialite.CARDIOLOGIE);
        map.put("thoracique", Specialite.CARDIOLOGIE);
        map.put("peau", Specialite.DERMATOLOGIE);
        map.put("dermatologique", Specialite.DERMATOLOGIE);
        map.put("acné", Specialite.DERMATOLOGIE);
        map.put("diabète", Specialite.ENDOCRINOLOGIE);
        map.put("thyroïde", Specialite.ENDOCRINOLOGIE);
        map.put("ventre", Specialite.GASTROENTEROLOGIE);
        map.put("estomac", Specialite.GASTROENTEROLOGIE);
        map.put("digestion", Specialite.GASTROENTEROLOGIE);
        map.put("femme", Specialite.GYNECOLOGIE);
        map.put("gynécologique", Specialite.GYNECOLOGIE);
        map.put("grossesse", Specialite.GYNECOLOGIE);
        map.put("cerveau", Specialite.NEUROLOGIE);
        map.put("neurologique", Specialite.NEUROLOGIE);
        map.put("migraine", Specialite.NEUROLOGIE);
        map.put("cancer", Specialite.ONCOLOGIE);
        map.put("œil", Specialite.OPHTALMOLOGIE);
        map.put("vision", Specialite.OPHTALMOLOGIE);
        map.put("os", Specialite.ORTHOPEDIE);
        map.put("articulation", Specialite.ORTHOPEDIE);
        map.put("enfant", Specialite.PEDIATRIE);
        map.put("bébé", Specialite.PEDIATRIE);
        map.put("mental", Specialite.PSYCHIATRIE);
        map.put("psychologique", Specialite.PSYCHIATRIE);
        map.put("poumon", Specialite.PNEUMOLOGIE);
        map.put("respiration", Specialite.PNEUMOLOGIE);
        map.put("rhumatisme", Specialite.RHUMATOLOGIE);
        map.put("urinaire", Specialite.UROLOGIE);
        map.put("rein", Specialite.UROLOGIE);
        return map;
    }
}

