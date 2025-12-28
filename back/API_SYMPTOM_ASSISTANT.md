# API Symptom Assistant

## Description

Le Symptom Assistant est un outil d'analyse de symptômes qui aide les patients à structurer leurs symptômes avant une consultation. Il **ne fournit pas de diagnostic médical** et inclut des avertissements de sécurité appropriés.

⚠️ **AVERTISSEMENT IMPORTANT**: Cette API n'effectue pas de diagnostic médical. Elle sert uniquement à structurer l'information pour faciliter la communication avec un professionnel de santé.

---

## Format JSON de Sortie

### Réponse (SymptomAnalysisResponseDTO)

```json
{
  "id": 1,
  "symptoms": [
    "douleur thoracique",
    "essoufflement",
    "difficulté à respirer"
  ],
  "severity": 7,
  "duration": 3,
  "redFlags": [
    "douleur thoracique sévère",
    "essoufflement au repos"
  ],
  "suggestedSpecialties": [
    "CARDIOLOGIE",
    "MEDECINE_GENERALE"
  ],
  "questions": [
    "La douleur irradie-t-elle vers le bras gauche, la mâchoire ou le dos ?",
    "L'essoufflement survient-il au repos ou seulement à l'effort ?",
    "Avez-vous des antécédents cardiaques ?"
  ],
  "summary": "Résumé de l'analyse des symptômes:\n\nSymptômes identifiés: douleur thoracique, essoufflement, difficulté à respirer\nSévérité: 7/10\nDurée: 3 jour(s)\n\n⚠️ Indicateurs d'urgence détectés: douleur thoracique sévère, essoufflement au repos\n\nSpécialités suggérées: CARDIOLOGIE, MEDECINE_GENERALE",
  "urgentRecommendation": true,
  "recommendationMessage": "⚠️ URGENCE MÉDICALE: Consultez immédiatement un médecin ou appelez le 190 (SAMU). Les symptômes décrits nécessitent une attention médicale urgente.",
  "safetyWarning": "⚠️ IMPORTANT: Cette analyse n'est pas un diagnostic médical. Consultez toujours un professionnel de santé pour un diagnostic précis."
}
```

### Champs

- `id` (Long) : ID de l'analyse
- `symptoms` (List<String>) : Liste des symptômes structurés extraits
- `severity` (Integer, 1-10) : Sévérité perçue des symptômes
- `duration` (Integer) : Durée des symptômes en jours
- `redFlags` (List<String>) : Indicateurs d'urgence détectés
- `suggestedSpecialties` (List<Specialite>) : Spécialités suggérées
- `questions` (List<String>) : Questions de clarification
- `summary` (String) : Résumé structuré pour le docteur
- `urgentRecommendation` (Boolean) : Indique si une consultation urgente est recommandée
- `recommendationMessage` (String) : Message de recommandation
- `safetyWarning` (String) : Avertissement de sécurité

---

## Règles de Sécurité

### 1. Pas de Diagnostic Médical

- ❌ L'API **ne fournit jamais** de diagnostic
- ❌ L'API **ne prescrit jamais** de traitement
- ✅ L'API **suggère seulement** des spécialités et des questions
- ✅ Tous les résultats incluent un **avertissement de sécurité**

### 2. Détection des Red Flags

L'API détecte automatiquement les indicateurs d'urgence :

- Douleur thoracique sévère
- Essoufflement au repos
- Perte de conscience
- Saignement important
- Traumatisme crânien
- Fièvre élevée (>39°C)
- Douleur insupportable

### 3. Recommandations d'Urgence

- Si **red flags détectés** → `urgentRecommendation: true`
- Si **sévérité >= 8/10** → `urgentRecommendation: true`
- Message recommandé : "Consultez immédiatement un médecin ou appelez le 190 (SAMU)"

### 4. Avertissement Obligatoire

Toutes les réponses incluent :
```
⚠️ IMPORTANT: Cette analyse n'est pas un diagnostic médical. 
Consultez toujours un professionnel de santé pour un diagnostic précis.
```

---

## Endpoints

### 1. Analyser des Symptômes

**POST** `/api/symptom-analysis`

**Headers:**
```
Authorization: Bearer <token>
Content-Type: application/json
```

**Body:**
```json
{
  "description": "J'ai des douleurs thoraciques depuis 3 jours, surtout en position allongée, avec essoufflement. La douleur est assez sévère, environ 7/10.",
  "patientId": 1,
  "appointmentId": 5
}
```

**Réponse:** `200 OK` avec `SymptomAnalysisResponseDTO`

---

### 2. Récupérer une Analyse par ID

**GET** `/api/symptom-analysis/{id}`

**Headers:**
```
Authorization: Bearer <token>
```

**Réponse:** `200 OK` avec `SymptomAnalysisResponseDTO`

---

### 3. Récupérer les Analyses d'un Patient

**GET** `/api/symptom-analysis/patient/{patientId}`

**Headers:**
```
Authorization: Bearer <token>
```

**Réponse:** `200 OK` avec `List<SymptomAnalysisResponseDTO>`

---

### 4. Récupérer l'Analyse d'un Rendez-vous

**GET** `/api/symptom-analysis/appointment/{appointmentId}`

**Headers:**
```
Authorization: Bearer <token>
```

**Réponse:** `200 OK` avec `SymptomAnalysisResponseDTO`

---

## Exemples d'Utilisation

### Exemple 1: Analyse Simple

**Requête:**
```json
{
  "description": "J'ai des maux de tête depuis 2 jours, modérés, avec sensibilité à la lumière.",
  "patientId": 1
}
```

**Réponse:**
```json
{
  "id": 1,
  "symptoms": ["maux de tête"],
  "severity": 5,
  "duration": 2,
  "redFlags": [],
  "suggestedSpecialties": ["NEUROLOGIE", "MEDECINE_GENERALE"],
  "questions": [
    "Avez-vous déjà eu ce type de maux de tête auparavant ?",
    "Les maux de tête sont-ils accompagnés de nausées ou de sensibilité à la lumière ?"
  ],
  "urgentRecommendation": false,
  "recommendationMessage": "Il est recommandé de consulter un médecin pour une évaluation appropriée de vos symptômes.",
  ...
}
```

### Exemple 2: Cas Urgent (Red Flags)

**Requête:**
```json
{
  "description": "Douleur thoracique sévère depuis 1 heure, essoufflement même au repos, sensation de serrement.",
  "patientId": 1
}
```

**Réponse:**
```json
{
  "id": 2,
  "symptoms": ["douleur thoracique", "essoufflement"],
  "severity": 9,
  "duration": 0,
  "redFlags": [
    "douleur thoracique sévère",
    "essoufflement au repos"
  ],
  "suggestedSpecialties": ["CARDIOLOGIE"],
  "questions": [
    "La douleur irradie-t-elle vers le bras gauche, la mâchoire ou le dos ?",
    "Avez-vous des antécédents cardiaques ?",
    "Les symptômes s'aggravent-ils rapidement ?"
  ],
  "urgentRecommendation": true,
  "recommendationMessage": "⚠️ URGENCE MÉDICALE: Consultez immédiatement un médecin ou appelez le 190 (SAMU). Les symptômes décrits nécessitent une attention médicale urgente.",
  ...
}
```

---

## Logique d'Extraction

### Extraction de Symptômes

L'API utilise un mapping de mots-clés vers des symptômes structurés :

- "douleur thoracique" → "douleur thoracique"
- "essoufflement" → "essoufflement"
- "maux de tête" / "céphalée" → "maux de tête"
- "fièvre" → "fièvre"
- etc.

### Extraction de la Sévérité

- Recherche de nombres (1-10) dans la description
- Mots-clés : "sévère" (8), "modéré" (5), "léger" (3)
- Si aucun trouvé → `null`

### Extraction de la Durée

- Recherche de "X jour(s)" → durée en jours
- Recherche de "X semaine(s)" → converti en jours
- Si aucun trouvé → `null`

### Suggestion de Spécialités

- Mapping symptômes → spécialités (ex: "cœur" → CARDIOLOGIE)
- Si aucune correspondance → MEDECINE_GENERALE

### Génération de Questions

Questions générées selon :
- Symptômes manquants
- Sévérité/durée non spécifiées
- Red flags détectés
- Type de symptômes (questions spécifiques)

---

## Intégration avec les Rendez-vous

L'analyse peut être liée à un rendez-vous via `appointmentId` :

1. Le patient crée une analyse avant le RDV
2. L'analyse est sauvegardée avec `appointmentId`
3. Le docteur peut récupérer l'analyse via `/api/symptom-analysis/appointment/{appointmentId}`
4. Le résumé (`summary`) est disponible dans le dossier du patient

---

## Codes de Statut HTTP

- `200 OK` : Succès
- `400 Bad Request` : Paramètres invalides
- `401 Unauthorized` : Token manquant ou invalide
- `403 Forbidden` : Accès refusé
- `404 Not Found` : Ressource non trouvée

---

## Notes Importantes

1. **Pas de Diagnostic**: Cette API ne remplace jamais une consultation médicale professionnelle.

2. **Red Flags**: Tous les red flags déclenchent une recommandation d'urgence explicite.

3. **Données Sensibles**: Les analyses sont liées au patient et respectent la confidentialité médicale.

4. **Historique**: Toutes les analyses sont sauvegardées pour référence future et peuvent être liées aux rendez-vous.

