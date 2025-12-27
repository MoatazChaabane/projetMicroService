# API Gestion des Patients

## Base URL
```
http://localhost:8081
```

## Endpoints

### 1. Créer un patient
- **URL**: `POST /api/patients`
- **Description**: Crée un nouveau patient
- **Authentification**: Requise (DOCTOR ou ADMIN)
- **Body**:
```json
{
  "nom": "Dupont",
  "prenom": "Jean",
  "dateNaissance": "1990-01-15",
  "sexe": "M",
  "telephone": "+33123456789",
  "adresse": "123 Rue de la Paix, 75001 Paris",
  "allergies": "Pénicilline, Pollen",
  "antecedents": "Hypertension, Diabète type 2",
  "contactUrgenceNom": "Marie Dupont",
  "contactUrgenceTelephone": "+33987654321",
  "contactUrgenceRelation": "Épouse"
}
```
- **Response**: 201 Created
```json
{
  "message": "Patient créé avec succès",
  "patient": {
    "id": 1,
    "nom": "Dupont",
    "prenom": "Jean",
    ...
  }
}
```

### 2. Récupérer un patient par ID
- **URL**: `GET /api/patients/{id}`
- **Description**: Récupère les informations d'un patient spécifique
- **Authentification**: Requise (DOCTOR ou ADMIN)
- **Response**: 200 OK

### 3. Liste tous les patients (avec pagination)
- **URL**: `GET /api/patients`
- **Description**: Récupère la liste paginée de tous les patients
- **Authentification**: Requise (DOCTOR ou ADMIN)
- **Paramètres de requête**:
  - `page` (optionnel, défaut: 0): Numéro de page (0-indexed)
  - `size` (optionnel, défaut: 10): Taille de la page
  - `sortBy` (optionnel, défaut: "id"): Champ de tri
  - `sortDir` (optionnel, défaut: "asc"): Direction du tri ("asc" ou "desc")
- **Exemple**: `GET /api/patients?page=0&size=20&sortBy=nom&sortDir=asc`
- **Response**: 200 OK
```json
{
  "content": [
    {
      "id": 1,
      "nom": "Dupont",
      "prenom": "Jean",
      ...
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 50,
  "totalPages": 5,
  "first": true,
  "last": false
}
```

### 4. Rechercher des patients
- **URL**: `GET /api/patients/search`
- **Description**: Recherche des patients par nom, prénom ou téléphone
- **Authentification**: Requise (DOCTOR ou ADMIN)
- **Paramètres de requête**:
  - `search` (optionnel): Terme de recherche (nom, prénom ou téléphone)
  - `page` (optionnel, défaut: 0): Numéro de page
  - `size` (optionnel, défaut: 10): Taille de la page
  - `sortBy` (optionnel, défaut: "id"): Champ de tri
  - `sortDir` (optionnel, défaut: "asc"): Direction du tri
- **Exemple**: `GET /api/patients/search?search=Dupont&page=0&size=10`
- **Response**: 200 OK (même format que la liste)

### 5. Modifier un patient
- **URL**: `PUT /api/patients/{id}`
- **Description**: Met à jour les informations d'un patient
- **Authentification**: Requise (DOCTOR ou ADMIN)
- **Body**: Même format que la création
- **Response**: 200 OK

### 6. Supprimer un patient (soft delete)
- **URL**: `DELETE /api/patients/{id}`
- **Description**: Supprime un patient de manière logique (soft delete)
- **Authentification**: Requise (DOCTOR ou ADMIN)
- **Response**: 200 OK
```json
{
  "message": "Patient supprimé avec succès"
}
```

### 7. Compter le nombre de patients
- **URL**: `GET /api/patients/count`
- **Description**: Retourne le nombre total de patients non supprimés
- **Authentification**: Requise (DOCTOR ou ADMIN)
- **Response**: 200 OK
```json
{
  "count": 50
}
```

## Champs du modèle Patient

| Champ | Type | Obligatoire | Description |
|-------|------|-------------|-------------|
| `nom` | String | Oui | Nom du patient |
| `prenom` | String | Oui | Prénom du patient |
| `dateNaissance` | LocalDate | Oui | Date de naissance (format: YYYY-MM-DD) |
| `sexe` | Enum | Oui | Sexe (M ou F) |
| `telephone` | String | Non | Numéro de téléphone (format validé) |
| `adresse` | String | Non | Adresse complète |
| `allergies` | String | Non | Allergies connues |
| `antecedents` | String | Non | Antécédents médicaux |
| `contactUrgenceNom` | String | Non | Nom du contact d'urgence |
| `contactUrgenceTelephone` | String | Non | Téléphone du contact d'urgence |
| `contactUrgenceRelation` | String | Non | Relation avec le contact d'urgence |

## Validations

- **nom** : Obligatoire, max 100 caractères
- **prenom** : Obligatoire, max 100 caractères
- **dateNaissance** : Obligatoire, doit être dans le passé
- **sexe** : Obligatoire, valeurs: M ou F
- **telephone** : Optionnel, format validé (8-15 chiffres, peut commencer par +)
- **contactUrgenceTelephone** : Optionnel, format validé

## Pagination

Tous les endpoints de liste supportent la pagination avec les paramètres :
- `page` : Numéro de page (commence à 0)
- `size` : Nombre d'éléments par page
- `sortBy` : Champ de tri (ex: "nom", "prenom", "dateNaissance", "id")
- `sortDir` : Direction ("asc" ou "desc")

## Recherche

La recherche fonctionne sur :
- Nom (insensible à la casse)
- Prénom (insensible à la casse)
- Téléphone (recherche partielle)

## Soft Delete

Les patients supprimés ne sont pas physiquement supprimés de la base de données. Le champ `deleted` est mis à `true`. Les patients supprimés n'apparaissent pas dans les listes et recherches.

## Audit

Chaque patient a automatiquement :
- `createdAt` : Date de création (non modifiable)
- `updatedAt` : Date de dernière mise à jour (mise à jour automatique)

## Exemples d'utilisation

### Créer un patient
```bash
curl -X POST http://localhost:8081/api/patients \
  -H "Content-Type: application/json" \
  -d '{
    "nom": "Dupont",
    "prenom": "Jean",
    "dateNaissance": "1990-01-15",
    "sexe": "M",
    "telephone": "+33123456789"
  }'
```

### Rechercher des patients
```bash
curl "http://localhost:8081/api/patients/search?search=Dupont&page=0&size=10"
```

### Modifier un patient
```bash
curl -X PUT http://localhost:8081/api/patients/1 \
  -H "Content-Type: application/json" \
  -d '{
    "nom": "Dupont",
    "prenom": "Jean-Pierre",
    "dateNaissance": "1990-01-15",
    "sexe": "M"
  }'
```

