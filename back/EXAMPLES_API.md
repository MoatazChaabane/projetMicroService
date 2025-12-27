# Exemples de Body pour les Endpoints API

## 1. Register (Inscription)

**Endpoint**: `POST /api/auth/register`

**Content-Type**: `application/json`

### Exemple 1 : Inscription d'un PATIENT

```json
{
  "email": "patient@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+1234567890",
  "role": "PATIENT"
}
```

### Exemple 2 : Inscription d'un DOCTOR

```json
{
  "email": "doctor@example.com",
  "password": "doctor123",
  "firstName": "Jane",
  "lastName": "Smith",
  "phoneNumber": "+9876543210",
  "role": "DOCTOR"
}
```

### Exemple 3 : Inscription d'un ADMIN

```json
{
  "email": "admin@example.com",
  "password": "admin123",
  "firstName": "Admin",
  "lastName": "User",
  "phoneNumber": "+1111111111",
  "role": "ADMIN"
}
```

### Exemple 4 : Inscription sans numéro de téléphone (optionnel)

```json
{
  "email": "user@example.com",
  "password": "password123",
  "firstName": "Alice",
  "lastName": "Johnson",
  "role": "PATIENT"
}
```

### Réponse attendue (201 Created)

```json
{
  "message": "Inscription réussie",
  "user": {
    "id": "507f1f77bcf86cd799439011",
    "email": "patient@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+1234567890",
    "role": "PATIENT",
    "photoUrl": null,
    "createdAt": "2024-01-01T12:00:00",
    "updatedAt": "2024-01-01T12:00:00"
  }
}
```

---

## 2. Login (Connexion)

**Endpoint**: `POST /api/auth/login`

**Content-Type**: `application/json`

### Exemple de body

```json
{
  "email": "patient@example.com",
  "password": "password123"
}
```

### Réponse attendue (200 OK)

```json
{
  "message": "Connexion réussie",
  "user": {
    "id": "507f1f77bcf86cd799439011",
    "email": "patient@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+1234567890",
    "role": "PATIENT",
    "photoUrl": null,
    "createdAt": "2024-01-01T12:00:00",
    "updatedAt": "2024-01-01T12:00:00"
  }
}
```

**Note**: Après une connexion réussie, un cookie `JSESSIONID` sera automatiquement créé et utilisé pour les requêtes suivantes.

---

## Rôles disponibles

Les valeurs possibles pour le champ `role` sont :
- `"PATIENT"` - Patient
- `"DOCTOR"` - Médecin
- `"ADMIN"` - Administrateur

---

## Validation

### Register
- `email` : Obligatoire, doit être un email valide
- `password` : Obligatoire, minimum 6 caractères
- `firstName` : Obligatoire
- `lastName` : Obligatoire
- `phoneNumber` : Optionnel
- `role` : Obligatoire, doit être PATIENT, DOCTOR ou ADMIN

### Login
- `email` : Obligatoire, doit être un email valide
- `password` : Obligatoire

---

## Exemples d'utilisation avec cURL

### Register
```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "patient@example.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+1234567890",
    "role": "PATIENT"
  }'
```

### Login
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "patient@example.com",
    "password": "password123"
  }' \
  -c cookies.txt
```

---

## Exemples d'utilisation avec Postman

### Register
1. Méthode : `POST`
2. URL : `http://localhost:8081/api/auth/register`
3. Headers : `Content-Type: application/json`
4. Body (raw JSON) :
```json
{
  "email": "patient@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+1234567890",
  "role": "PATIENT"
}
```

### Login
1. Méthode : `POST`
2. URL : `http://localhost:8081/api/auth/login`
3. Headers : `Content-Type: application/json`
4. Body (raw JSON) :
```json
{
  "email": "patient@example.com",
  "password": "password123"
}
```

