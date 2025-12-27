# 🔗 Liens d'accès à l'API

## 🌐 URLs principales

### Page d'accueil de l'API
```
http://localhost:8081/
```
Retourne les informations de l'API et les liens vers les endpoints.

---

## 📚 Documentation Swagger/OpenAPI

### Swagger UI (Interface interactive)
```
http://localhost:8081/swagger-ui.html
```
ou
```
http://localhost:8081/swagger-ui/index.html
```
Interface graphique pour tester tous les endpoints de l'API.

### Documentation OpenAPI (JSON)
```
http://localhost:8081/v3/api-docs
```

### Documentation OpenAPI (YAML)
```
http://localhost:8081/v3/api-docs.yaml
```

---

## 🔐 Endpoints d'authentification

### Inscription
```
POST http://localhost:8081/api/auth/register
```

### Connexion
```
POST http://localhost:8081/api/auth/login
```

### Déconnexion
```
POST http://localhost:8081/api/auth/logout
```

### Utilisateur actuel
```
GET http://localhost:8081/api/auth/me
```

---

## 👤 Endpoints de gestion du profil

### Récupérer le profil
```
GET http://localhost:8081/api/profile
```

### Modifier le profil
```
PUT http://localhost:8081/api/profile
```

### Changer le mot de passe
```
PUT http://localhost:8081/api/profile/password
```

### Upload photo de profil
```
POST http://localhost:8081/api/profile/photo
```

### Supprimer photo de profil
```
DELETE http://localhost:8081/api/profile/photo
```

---

## 👨‍💼 Endpoints d'administration (ADMIN uniquement)

### Liste tous les utilisateurs
```
GET http://localhost:8081/api/admin/users
```

### Récupérer un utilisateur par ID
```
GET http://localhost:8081/api/admin/users/{userId}
```

### Modifier un utilisateur
```
PUT http://localhost:8081/api/admin/users/{userId}
```

### Changer le mot de passe d'un utilisateur
```
PUT http://localhost:8081/api/admin/users/{userId}/password
```

### Upload photo pour un utilisateur
```
POST http://localhost:8081/api/admin/users/{userId}/photo
```

---

## 📁 Fichiers statiques

### Accéder aux photos uploadées
```
http://localhost:8081/uploads/{nom_fichier}
```

---

## 🧪 Test rapide avec le navigateur

### Page d'accueil
Ouvrez dans votre navigateur :
```
http://localhost:8081/
```

### Swagger UI
Ouvrez dans votre navigateur :
```
http://localhost:8081/swagger-ui.html
```

---

## 📝 Exemples d'utilisation

### Avec cURL

**Inscription :**
```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "firstName": "Test",
    "lastName": "User",
    "role": "PATIENT"
  }'
```

**Connexion :**
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }' \
  -c cookies.txt
```

**Récupérer le profil (avec cookie) :**
```bash
curl -X GET http://localhost:8081/api/profile \
  -b cookies.txt
```

### Avec Postman

1. **Base URL** : `http://localhost:8081`
2. **Collection** : Créez une collection avec tous les endpoints ci-dessus
3. **Authentification** : Après login, Postman gère automatiquement les cookies de session

---

## 🔍 Vérification de l'état de l'application

### Health Check (si Actuator est configuré)
```
http://localhost:8081/actuator/health
```

---

## ⚙️ Configuration

- **Port** : `8081`
- **Base de données** : MongoDB Atlas (`MedicalApp`)
- **Authentification** : Session + Cookies (JSESSIONID)

---

## 📌 Liens importants à retenir

1. **Swagger UI** : http://localhost:8081/swagger-ui.html ⭐ (Le plus utile pour tester)
2. **Page d'accueil** : http://localhost:8081/
3. **API Docs** : http://localhost:8081/v3/api-docs

