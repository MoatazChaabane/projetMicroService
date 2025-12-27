# API Endpoints - Module Gestion Profil

## Base URL
```
http://localhost:8080
```

## Authentification

### 1. Inscription
- **URL**: `POST /api/auth/register`
- **Description**: Crée un nouveau compte utilisateur
- **Body**:
```json
{
  "email": "user@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+1234567890",
  "role": "PATIENT" // ou "DOCTOR" ou "ADMIN"
}
```
- **Response**: 201 Created
```json
{
  "message": "Inscription réussie",
  "user": {
    "id": "...",
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "role": "PATIENT",
    ...
  }
}
```

### 2. Connexion
- **URL**: `POST /api/auth/login`
- **Description**: Connecte un utilisateur (crée une session)
- **Body**:
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```
- **Response**: 200 OK
```json
{
  "message": "Connexion réussie",
  "user": { ... }
}
```

### 3. Déconnexion
- **URL**: `POST /api/auth/logout`
- **Description**: Déconnecte l'utilisateur actuel
- **Response**: 200 OK
```json
{
  "message": "Déconnexion réussie"
}
```

### 4. Utilisateur actuel
- **URL**: `GET /api/auth/me`
- **Description**: Récupère les informations de l'utilisateur connecté
- **Response**: 200 OK
```json
{
  "id": "...",
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "role": "PATIENT",
  ...
}
```

## Gestion du Profil

### 5. Obtenir le profil
- **URL**: `GET /api/profile`
- **Description**: Récupère le profil de l'utilisateur connecté
- **Authentification**: Requise
- **Response**: 200 OK

### 6. Modifier le profil
- **URL**: `PUT /api/profile`
- **Description**: Met à jour le profil de l'utilisateur connecté
- **Authentification**: Requise
- **Body**:
```json
{
  "email": "newemail@example.com", // optionnel
  "firstName": "Jane", // optionnel
  "lastName": "Smith", // optionnel
  "phoneNumber": "+9876543210" // optionnel
}
```
- **Response**: 200 OK

### 7. Changer le mot de passe
- **URL**: `PUT /api/profile/password`
- **Description**: Change le mot de passe de l'utilisateur connecté
- **Authentification**: Requise
- **Body**:
```json
{
  "currentPassword": "oldpassword",
  "newPassword": "newpassword123"
}
```
- **Response**: 200 OK

### 8. Upload photo de profil
- **URL**: `POST /api/profile/photo`
- **Description**: Upload une photo de profil
- **Authentification**: Requise
- **Content-Type**: `multipart/form-data`
- **Body**: `file` (fichier image)
- **Response**: 200 OK

### 9. Supprimer photo de profil
- **URL**: `DELETE /api/profile/photo`
- **Description**: Supprime la photo de profil
- **Authentification**: Requise
- **Response**: 200 OK

## Administration (ADMIN uniquement)

### 10. Liste des utilisateurs
- **URL**: `GET /api/admin/users`
- **Description**: Récupère la liste de tous les utilisateurs
- **Authentification**: Requise (ADMIN)
- **Response**: 200 OK

### 11. Obtenir un utilisateur par ID
- **URL**: `GET /api/admin/users/{userId}`
- **Description**: Récupère les informations d'un utilisateur spécifique
- **Authentification**: Requise (ADMIN)
- **Response**: 200 OK

### 12. Modifier un utilisateur
- **URL**: `PUT /api/admin/users/{userId}`
- **Description**: Met à jour le profil d'un utilisateur
- **Authentification**: Requise (ADMIN)
- **Body**: Même format que la modification de profil
- **Response**: 200 OK

### 13. Changer le mot de passe d'un utilisateur
- **URL**: `PUT /api/admin/users/{userId}/password`
- **Description**: Change le mot de passe d'un utilisateur
- **Authentification**: Requise (ADMIN)
- **Body**: Même format que le changement de mot de passe
- **Response**: 200 OK

### 14. Upload photo pour un utilisateur
- **URL**: `POST /api/admin/users/{userId}/photo`
- **Description**: Upload une photo pour un utilisateur
- **Authentification**: Requise (ADMIN)
- **Content-Type**: `multipart/form-data`
- **Body**: `file` (fichier image)
- **Response**: 200 OK

## Rôles disponibles
- `PATIENT`: Patient
- `DOCTOR`: Médecin
- `ADMIN`: Administrateur

## Gestion des erreurs

Toutes les erreurs suivent le format suivant:
```json
{
  "message": "Message d'erreur",
  "status": 400,
  "timestamp": "2024-01-01T12:00:00"
}
```

Codes de statut HTTP:
- `200`: Succès
- `201`: Créé
- `400`: Erreur de validation
- `401`: Non autorisé
- `403`: Interdit
- `404`: Non trouvé
- `500`: Erreur serveur

