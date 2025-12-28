# API Gestion des Rendez-vous

## Vue d'ensemble

Module complet de gestion des rendez-vous avec création, annulation, confirmation, reprogrammation et prévention des conflits.

## Modèle de données

### Appointment
- **ID** : Identifiant unique
- **Doctor** : Relation ManyToOne avec le docteur
- **Patient** : Relation ManyToOne avec le patient
- **Date** : Date du rendez-vous (LocalDate)
- **Heure** : Heure du rendez-vous (LocalTime)
- **Statut** : Enum AppointmentStatus (PENDING, CONFIRMED, CANCELLED, COMPLETED, NO_SHOW)
- **Motif** : Motif de la consultation
- **Notes** : Notes additionnelles
- **Audit** : createdAt, updatedAt
- **Soft delete** : deleted

### Statuts disponibles
- **PENDING** : En attente de confirmation
- **CONFIRMED** : Confirmé
- **CANCELLED** : Annulé
- **COMPLETED** : Terminé
- **NO_SHOW** : Patient absent

## Endpoints REST

### Base URL
```
/api/appointments
```

### 1. Créer un rendez-vous
```http
POST /api/appointments
Authorization: Bearer <token>
Content-Type: application/json

{
  "doctorId": 1,
  "patientId": 1,
  "date": "2024-01-15",
  "heure": "10:00",
  "motif": "Consultation de routine",
  "notes": "Première consultation"
}
```

**Réponse 201** :
```json
{
  "message": "Rendez-vous créé avec succès",
  "appointment": {
    "id": 1,
    "doctorId": 1,
    "doctorNomComplet": "Dr. Jean Dupont",
    "doctorSpecialite": "CARDIOLOGIE",
    "patientId": 1,
    "patientNomComplet": "Marie Martin",
    "date": "2024-01-15",
    "heure": "10:00",
    "status": "PENDING",
    "motif": "Consultation de routine",
    "notes": "Première consultation",
    "createdAt": "2024-01-10T10:00:00",
    "updatedAt": "2024-01-10T10:00:00"
  }
}
```

### 2. Récupérer un rendez-vous par ID
```http
GET /api/appointments/{id}
Authorization: Bearer <token>
```

### 3. RDV d'un patient (paginé)
```http
GET /api/appointments/patient/{patientId}?page=0&size=10
Authorization: Bearer <token>
```

### 4. RDV d'un patient (liste complète)
```http
GET /api/appointments/patient/{patientId}/all
Authorization: Bearer <token>
```

### 5. RDV d'un docteur (paginé)
```http
GET /api/appointments/doctor/{doctorId}?page=0&size=10
Authorization: Bearer <token>
```

### 6. RDV d'un docteur (liste complète)
```http
GET /api/appointments/doctor/{doctorId}/all
Authorization: Bearer <token>
```

### 7. RDV d'un docteur pour une date
```http
GET /api/appointments/doctor/{doctorId}/date?date=2024-01-15
Authorization: Bearer <token>
```

### 8. Calendrier par semaine
```http
GET /api/appointments/doctor/{doctorId}/week?weekStart=2024-01-15
Authorization: Bearer <token>
```

**Description** : Récupère tous les rendez-vous d'un docteur pour une semaine (du lundi au dimanche). Le paramètre `weekStart` peut être n'importe quel jour de la semaine, le système calcule automatiquement le lundi.

### 9. Vérifier la disponibilité
```http
GET /api/appointments/check-availability?doctorId=1&date=2024-01-15&heure=10:00
Authorization: Bearer <token>
```

**Réponse** :
```json
{
  "doctorId": 1,
  "date": "2024-01-15",
  "heure": "10:00",
  "available": true,
  "message": "Créneau disponible"
}
```

### 10. Confirmer un rendez-vous
```http
PUT /api/appointments/{id}/confirm
Authorization: Bearer <token>
```

### 11. Annuler un rendez-vous
```http
PUT /api/appointments/{id}/cancel
Authorization: Bearer <token>
```

### 12. Marquer comme terminé
```http
PUT /api/appointments/{id}/complete
Authorization: Bearer <token>
```

### 13. Marquer comme absent (NO_SHOW)
```http
PUT /api/appointments/{id}/no-show
Authorization: Bearer <token>
```

### 14. Changer le statut
```http
PUT /api/appointments/{id}/status?status=CONFIRMED
Authorization: Bearer <token>
```

### 15. Reprogrammer un rendez-vous
```http
PUT /api/appointments/{id}/reschedule?newDate=2024-01-20&newHeure=14:00
Authorization: Bearer <token>
```

**Description** : Change la date et l'heure d'un rendez-vous avec vérification de disponibilité et prévention des conflits.

### 16. Modifier un rendez-vous
```http
PUT /api/appointments/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "doctorId": 1,
  "patientId": 1,
  "date": "2024-01-20",
  "heure": "14:00",
  "motif": "Consultation de suivi",
  "notes": "Notes mises à jour"
}
```

### 17. Supprimer un rendez-vous (soft delete)
```http
DELETE /api/appointments/{id}
Authorization: Bearer <token>
```

### 18. Compter les RDV d'un docteur
```http
GET /api/appointments/doctor/{doctorId}/count
Authorization: Bearer <token>
```

### 19. Compter les RDV d'un patient
```http
GET /api/appointments/patient/{patientId}/count
Authorization: Bearer <token>
```

## Prévention des conflits

Le système empêche la création de deux rendez-vous pour le même docteur au même créneau horaire (même date et même heure) avec les statuts PENDING ou CONFIRMED.

### Vérifications effectuées

1. **Créneaux horaires** : Vérifie que le docteur a un créneau horaire disponible pour le jour et l'heure demandés
2. **Conflits** : Vérifie qu'il n'existe pas déjà un rendez-vous PENDING ou CONFIRMED pour ce créneau
3. **Disponibilité** : L'endpoint `/check-availability` permet de vérifier avant la création

## Sécurité

- **Authentification** : JWT Bearer Token requis
- **Autorisations** :
  - `GET /api/appointments/**` : Accessible par DOCTOR, ADMIN, PATIENT
  - `POST /api/appointments` : DOCTOR, ADMIN, PATIENT
  - `PUT /api/appointments/{id}/**` : DOCTOR, ADMIN, PATIENT (pour leurs propres RDV)
  - `DELETE /api/appointments/{id}` : DOCTOR, ADMIN

## Notes importantes

1. **Création** : Un rendez-vous est créé avec le statut PENDING par défaut
2. **Conflits** : Impossible d'avoir 2 RDV PENDING ou CONFIRMED pour le même docteur au même créneau
3. **Reprogrammation** : Remet automatiquement le statut à PENDING si le RDV était CONFIRMED
4. **Soft delete** : La suppression est logique (deleted = true), les données restent en base
5. **Calendrier** : Le calendrier hebdomadaire calcule automatiquement le lundi de la semaine à partir de n'importe quel jour fourni

