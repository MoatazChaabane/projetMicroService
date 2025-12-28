package tn.pi.back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.pi.back.model.Specialite;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Réponse de l'analyse de symptômes")
public class SymptomAnalysisResponseDTO {
    
    @Schema(description = "ID de l'analyse de symptômes", example = "1")
    private Long id;
    
    @Schema(description = "Liste des symptômes structurés extraits", 
            example = "[\"douleur thoracique\", \"essoufflement\", \"difficulté à respirer\"]")
    private List<String> symptoms;
    
    @Schema(description = "Sévérité perçue (1-10)", example = "7")
    private Integer severity;
    
    @Schema(description = "Durée des symptômes en jours", example = "3")
    private Integer duration;
    
    @Schema(description = "Indicateurs d'urgence (red flags)", 
            example = "[\"douleur thoracique sévère\", \"essoufflement au repos\"]")
    private List<String> redFlags;
    
    @Schema(description = "Spécialités suggérées basées sur les symptômes")
    private List<Specialite> suggestedSpecialties;
    
    @Schema(description = "Questions de clarification pour affiner l'analyse")
    private List<String> questions;
    
    @Schema(description = "Résumé structuré pour le docteur")
    private String summary;
    
    @Schema(description = "Recommandation d'urgence", example = "true")
    private Boolean urgentRecommendation;
    
    @Schema(description = "Message de recommandation", 
            example = "Consultez un médecin en urgence si la douleur persiste ou s'aggrave")
    private String recommendationMessage;
    
    @Schema(description = "Avertissement de sécurité (pas de diagnostic)")
    private String safetyWarning;
}

