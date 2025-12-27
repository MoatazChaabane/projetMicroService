# API Gestion des Docteurs

## Vue d'ensemble

Module complet de gestion des docteurs avec CRUD et recherche avancée par spécialité, distance GPS, et disponibilité.

## Modèle de données

### Doctor
- **ID** : Identifiant unique
- **User** : Relation OneToOne avec l'utilisateur (docteur)
- **Spécialité** : Enum (CARDIOLOGIE, DERMATOLOGIE, etc.)
- **Nom clinique** : Nom de la clinique
- **Adresse** : Adresse complète
- **Latitude/Longitude** : Coordonnées GPS pour recherche par distance
- **Tarif consultation** : Prix en euros
- **Langues** : Liste des langues parlées
- **Rating** : Note moyenne (sur 5)
- **Nombre avis** : Nombre total d'avis
- **Téléconsultation** : Boolean (oui/non)
- **Horaires** : Liste de TimeSlot (créneaux horaires)
- **Audit** : createdAt, updatedAt
- **Soft delete** : deleted

### TimeSlot (Créneaux horaires)
Modèle recommandé pour gérer les disponibilités :

- **ID** : Identifiant unique
- **Doctor** : Relation ManyToOne avec le docteur
- **Jour** : Enum JourSemaine (LUNDI, MARDI, etc.)
- **Heure début** : LocalTime (ex: 09:00)
- **Heure fin** : LocalTime (ex: 12:00)
- **Disponible** : Boolean (true si le créneau est disponible)
- **Actif** : Boolean (pour désactiver un créneau sans le supprimer)

**Structure recommandée** : Un créneau par jour + plage horaire. Exemple :
- LUNDI : 09:00 - 12:00, 14:00 - 18:00
- MARDI : 09:00 - 12:00, 14:00 - 18:00
- etc.

## Endpoints REST

### Base URL
```
/api/doctors
```

### 1. Créer un docteur
```http
POST /api/doctors
Authorization: Bearer <token>
Content-Type: application/json

{
  "userId": 1,
  "specialite": "CARDIOLOGIE",
  "nomClinique": "Clinique du Cœur",
  "adresse": "123 Rue de la Santé, 75014 Paris",
  "latitude": 48.8566,
  "longitude": 2.3522,
  "tarifConsultation": 50.00,
  "langues": ["Français", "Anglais", "Arabe"],
  "teleconsultation": true,
  "horaires": [
    {
      "jour": "LUNDI",
      "heureDebut": "09:00",
      "heureFin": "12:00",
      "disponible": true
    },
    {
      "jour": "LUNDI",
      "heureDebut": "14:00",
      "heureFin": "18:00",
      "disponible": true
    },
    {
      "jour": "MARDI",
      "heureDebut": "09:00",
      "heureFin": "12:00",
      "disponible": true
    }
  ]
}
```

**Réponse 201** :
```json
{
  "message": "Docteur créé avec succès",
  "doctor": {
    "id": 1,
    "userId": 1,
    "nomComplet": "Dr. Jean Dupont",
    "email": "jean.dupont@example.com",
    "telephone": "+33123456789",
    "specialite": "CARDIOLOGIE",
    "nomClinique": "Clinique du Cœur",
    "adresse": "123 Rue de la Santé, 75014 Paris",
    "latitude": 48.8566,
    "longitude": 2.3522,
    "tarifConsultation": 50.00,
    "langues": ["Français", "Anglais", "Arabe"],
    "rating": 0.00,
    "nombreAvis": 0,
    "teleconsultation": true,
    "horaires": [...],
    "createdAt": "2023-12-27T18:00:00",
    "updatedAt": "2023-12-27T18:00:00"
  }
}
```

### 2. Récupérer un docteur par ID
```http
GET /api/doctors/{id}
Authorization: Bearer <token>
```

### 3. Récupérer un docteur par ID utilisateur
```http
GET /api/doctors/user/{userId}
Authorization: Bearer <token>
```

### 4. Lister tous les docteurs (pagination)
```http
GET /api/doctors?page=0&size=10&sortBy=rating&sortDir=desc
Authorization: Bearer <token>
```

**Paramètres** :
- `page` : Numéro de page (0-indexé, défaut: 0)
- `size` : Taille de la page (défaut: 10)
- `sortBy` : Champ de tri (défaut: rating)
- `sortDir` : Direction (asc/desc, défaut: desc)

### 5. Recherche avancée
```http
POST /api/doctors/search?page=0&size=10&sortBy=rating&sortDir=desc
Authorization: Bearer <token>
Content-Type: application/json
```

**Exemples de recherche** :

#### a) Par spécialité seule
```json
{
  "specialite": "CARDIOLOGIE"
}
```

#### b) Par spécialité + distance
```json
{
  "specialite": "CARDIOLOGIE",
  "latitude": 48.8566,
  "longitude": 2.3522,
  "rayonKm": 10.0
}
```

#### c) Par spécialité + distance + disponibilité
```json
{
  "specialite": "CARDIOLOGIE",
  "latitude": 48.8566,
  "longitude": 2.3522,
  "rayonKm": 10.0,
  "date": "2023-12-28",
  "heure": "10:00"
}
```

#### d) Par téléconsultation
```json
{
  "specialite": "CARDIOLOGIE",
  "teleconsultation": true
}
```

#### e) Par rating minimum
```json
{
  "specialite": "CARDIOLOGIE",
  "ratingMin": 4.0
}
```

#### f) Recherche combinée complète
```json
{
  "specialite": "CARDIOLOGIE",
  "latitude": 48.8566,
  "longitude": 2.3522,
  "rayonKm": 10.0,
  "date": "2023-12-28",
  "heure": "10:00",
  "teleconsultation": true,
  "ratingMin": 4.0
}
```

**Réponse** :
```json
{
  "content": [...],
  "page": 0,
  "size": 10,
  "totalElements": 25,
  "totalPages": 3,
  "first": true,
  "last": false
}
```

### 6. Mettre à jour un docteur
```http
PUT /api/doctors/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "userId": 1,
  "specialite": "CARDIOLOGIE",
  "nomClinique": "Clinique du Cœur - Nouvelle adresse",
  "adresse": "456 Avenue de la Santé, 75015 Paris",
  "latitude": 48.8500,
  "longitude": 2.3000,
  "tarifConsultation": 55.00,
  "langues": ["Français", "Anglais"],
  "teleconsultation": true,
  "horaires": [...]
}
```

### 7. Supprimer un docteur (soft delete)
```http
DELETE /api/doctors/{id}
Authorization: Bearer <token>
```

**Réponse 204** : No Content

### 8. Compter les docteurs
```http
GET /api/doctors/count
Authorization: Bearer <token>
```

**Réponse** :
```json
42
```

## Spécialités disponibles

- CARDIOLOGIE
- DERMATOLOGIE
- ENDOCRINOLOGIE
- GASTROENTEROLOGIE
- GYNECOLOGIE
- MEDECINE_GENERALE
- NEUROLOGIE
- ONCOLOGIE
- OPHTALMOLOGIE
- ORTHOPEDIE
- PEDIATRIE
- PSYCHIATRIE
- PNEUMOLOGIE
- RHUMATOLOGIE
- UROLOGIE

## Calcul de distance

Le système utilise la **formule de Haversine** pour calculer la distance entre deux points GPS :

```
distance = 6371 * acos(
  cos(radians(lat1)) * cos(radians(lat2)) * 
  cos(radians(lon2) - radians(lon1)) + 
  sin(radians(lat1)) * sin(radians(lat2))
)
```

Où :
- `6371` = Rayon de la Terre en kilomètres
- Le résultat est en kilomètres

## Sécurité

- **Authentification** : JWT Bearer Token requis
- **Autorisations** :
  - `GET /api/doctors/**` : Accessible par DOCTOR, ADMIN, PATIENT (pour rechercher)
  - `POST /api/doctors` : DOCTOR, ADMIN
  - `PUT /api/doctors/{id}` : DOCTOR, ADMIN
  - `DELETE /api/doctors/{id}` : DOCTOR, ADMIN

## Notes importantes

1. **Création** : Un utilisateur doit exister avec le rôle DOCTOR avant de créer un profil docteur
2. **Unicité** : Un utilisateur ne peut avoir qu'un seul profil docteur
3. **Soft delete** : La suppression est logique (deleted = true), les données restent en base
4. **Horaires** : Les créneaux sont gérés par jour de la semaine + plage horaire
5. **Disponibilité** : Vérifie si un créneau existe pour le jour et l'heure demandés

