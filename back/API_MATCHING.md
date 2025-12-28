# API Matching de Docteurs

## Endpoint: `/api/match/doctors`

### Description
Endpoint intelligent pour trouver les docteurs les plus pertinents basés sur:
- **Symptômes** : texte libre ou tags
- **Position géographique** : latitude/longitude du patient
- **Rayon de recherche** : en kilomètres
- **Date souhaitée** : optionnelle pour vérifier la disponibilité
- **Spécialité** : optionnelle pour filtrer les résultats

Les résultats sont triés par **score de pertinence** combinant plusieurs critères.

---

## Algorithme de Scoring

### Pondérations
Le score total est calculé avec les pondérations suivantes :

1. **Score Symptômes/Spécialité** : **40%**
   - `1.0` : Spécialité correspond exactement aux symptômes analysés
   - `0.7` : Spécialité générale (médecine générale) si symptômes non spécifiques
   - `0.5` : Score neutre si aucune spécialité suggérée
   - `0.0` : Aucune correspondance

2. **Score Distance** : **30%**
   - Calculé comme `1.0 - (distance / rayonMax)`
   - Plus proche = score plus élevé
   - Normalisé entre 0.0 et 1.0

3. **Score Disponibilité** : **30%**
   - `1.0` : Disponible à la date souhaitée
   - `0.5` : Score neutre si pas de date fournie
   - `0.0` : Non disponible à la date souhaitée

### Formule
```
Score Total = (ScoreSymptomes × 0.4) + (ScoreDistance × 0.3) + (ScoreDisponibilité × 0.3)
```

---

## Requête

### POST `/api/match/doctors`

**Headers:**
```
Authorization: Bearer <token>
Content-Type: application/json
```

**Body:**
```json
{
  "symptomes": "douleur thoracique, essoufflement",
  "tags": ["douleur thoracique", "essoufflement"],
  "specialite": "CARDIOLOGIE",
  "latitude": 36.8065,
  "longitude": 10.1815,
  "rayonKm": 10.0,
  "dateSouhaitee": "2024-01-15",
  "limit": 10
}
```

**Paramètres:**
- `symptomes` (obligatoire) : Texte libre décrivant les symptômes
- `tags` (optionnel) : Liste de tags de symptômes
- `specialite` (optionnel) : Spécialité pour filtrer (enum `Specialite`)
- `latitude` (obligatoire) : Latitude de la position du patient
- `longitude` (obligatoire) : Longitude de la position du patient
- `rayonKm` (obligatoire) : Rayon de recherche en kilomètres
- `dateSouhaitee` (optionnel) : Date souhaitée pour le rendez-vous (format: YYYY-MM-DD)
- `limit` (optionnel) : Nombre maximum de résultats (défaut: 10)

---

## Réponse

### Format de réponse
```json
[
  {
    "doctor": {
      "id": 1,
      "userId": 5,
      "nomComplet": "Dr. Jean Dupont",
      "email": "jean.dupont@example.com",
      "telephone": "+33123456789",
      "specialite": "CARDIOLOGIE",
      "nomClinique": "Clinique du Cœur",
      "adresse": "123 Rue de la Santé, 75014 Paris",
      "latitude": 36.8065,
      "longitude": 10.1815,
      "tarifConsultation": 50.00,
      "langues": ["français", "anglais"],
      "rating": 4.5,
      "nombreAvis": 120,
      "teleconsultation": true,
      "horaires": [...],
      "createdAt": "2023-01-01T10:00:00",
      "updatedAt": "2023-12-27T15:30:00"
    },
    "scoreTotal": 0.85,
    "scoreSymptomes": 1.0,
    "scoreDistance": 0.9,
    "scoreDisponibilite": 0.6,
    "distanceKm": 2.5,
    "disponible": true,
    "message": "Spécialité: CARDIOLOGIE - Distance: 2.50 km - Disponible - Correspondance parfaite avec vos symptômes"
  }
]
```

**Champs:**
- `doctor` : Informations complètes du docteur (format `DoctorResponseDTO`)
- `scoreTotal` : Score de pertinence total (0.0 à 1.0)
- `scoreSymptomes` : Score de correspondance symptômes/spécialité (0.0 à 1.0)
- `scoreDistance` : Score de distance normalisé (0.0 à 1.0)
- `scoreDisponibilite` : Score de disponibilité (0.0 ou 1.0, ou 0.5 si pas de date)
- `distanceKm` : Distance réelle en kilomètres
- `disponible` : Indique si le docteur est disponible à la date souhaitée
- `message` : Message explicatif du matching

---

## Requêtes SQL

### 1. Recherche par rayon (avec spécialité)
```sql
SELECT d.*, 
       (6371 * acos(cos(radians(:latitude)) * cos(radians(d.latitude)) * 
        cos(radians(d.longitude) - radians(:longitude)) + 
        sin(radians(:latitude)) * sin(radians(d.latitude)))) AS distance 
FROM doctors d 
WHERE d.deleted = false 
  AND d.specialite = :specialite 
  AND d.latitude IS NOT NULL 
  AND d.longitude IS NOT NULL 
HAVING distance <= :rayonKm 
ORDER BY distance
```

### 2. Recherche par rayon (sans spécialité)
```sql
SELECT d.*, 
       (6371 * acos(cos(radians(:latitude)) * cos(radians(d.latitude)) * 
        cos(radians(d.longitude) - radians(:longitude)) + 
        sin(radians(:latitude)) * sin(radians(d.latitude)))) AS distance 
FROM doctors d 
WHERE d.deleted = false 
  AND d.latitude IS NOT NULL 
  AND d.longitude IS NOT NULL 
HAVING distance <= :rayonKm 
ORDER BY distance
```

### Index recommandés
Les index suivants sont déjà présents sur la table `doctors` :
```sql
CREATE INDEX idx_doctor_specialite ON doctors(specialite);
CREATE INDEX idx_doctor_latitude_longitude ON doctors(latitude, longitude);
CREATE INDEX idx_doctor_deleted ON doctors(deleted);
```

---

## Mapping Symptômes → Spécialités

Le système utilise un mapping prédéfini pour associer les symptômes aux spécialités :

| Symptômes | Spécialité |
|-----------|------------|
| cœur, cardiaque, thorax, thoracique, tension, hypertension, essoufflement | CARDIOLOGIE |
| peau, dermatologique, acné, eczéma, psoriasis | DERMATOLOGIE |
| diabète, thyroïde, hormone | ENDOCRINOLOGIE |
| ventre, estomac, digestion, intestin | GASTROENTEROLOGIE |
| femme, gynécologique, grossesse | GYNECOLOGIE |
| cerveau, neurologique, migraine | NEUROLOGIE |
| cancer, oncologique | ONCOLOGIE |
| œil, vision, vue | OPHTALMOLOGIE |
| os, articulation, fracture | ORTHOPEDIE |
| enfant, bébé | PEDIATRIE |
| mental, psychologique, dépression | PSYCHIATRIE |
| poumon, respiration, asthme | PNEUMOLOGIE |
| rhumatisme | RHUMATOLOGIE |
| urinaire, rein | UROLOGIE |

Si aucun symptôme ne correspond, le système suggère **MEDECINE_GENERALE**.

---

## Exemples d'utilisation

### Exemple 1: Recherche par symptômes
```bash
curl -X POST http://localhost:8081/api/match/doctors \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "symptomes": "douleur thoracique et essoufflement",
    "latitude": 36.8065,
    "longitude": 10.1815,
    "rayonKm": 15.0,
    "limit": 5
  }'
```

### Exemple 2: Recherche avec spécialité et date
```bash
curl -X POST http://localhost:8081/api/match/doctors \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "symptomes": "problème de peau",
    "specialite": "DERMATOLOGIE",
    "latitude": 36.8065,
    "longitude": 10.1815,
    "rayonKm": 10.0,
    "dateSouhaitee": "2024-01-20",
    "limit": 10
  }'
```

### Exemple 3: Recherche avec tags
```bash
curl -X POST http://localhost:8081/api/match/doctors \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "symptomes": "maux de tête fréquents",
    "tags": ["migraine", "céphalée"],
    "latitude": 36.8065,
    "longitude": 10.1815,
    "rayonKm": 20.0
  }'
```

---

## Notes importantes

1. **Performance** : Les requêtes utilisent la formule Haversine pour calculer les distances géodésiques. Les index sur `latitude`, `longitude` et `specialite` améliorent les performances.

2. **Disponibilité** : La vérification de disponibilité se base sur les créneaux horaires (`time_slots`) du docteur pour le jour de la semaine correspondant à la date souhaitée. Elle ne vérifie pas les conflits avec d'autres rendez-vous existants (cela doit être fait lors de la création du rendez-vous).

3. **Symptômes** : Le système fait une analyse de texte simple basée sur des mots-clés. Pour une meilleure précision, il est recommandé d'utiliser également le paramètre `specialite` si connu.

4. **Tri** : Les résultats sont triés par `scoreTotal` décroissant, donc les docteurs les plus pertinents apparaissent en premier.

---

## Codes de statut HTTP

- `200 OK` : Recherche réussie, résultats retournés
- `400 Bad Request` : Paramètres invalides
- `401 Unauthorized` : Token manquant ou invalide
- `403 Forbidden` : Accès refusé (rôle PATIENT ou ADMIN requis)

