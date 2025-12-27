# 🔧 Configuration MySQL

## 📋 Informations de connexion

### Base de données
- **Nom de la base** : `medicalapp`
- **phpMyAdmin** : http://localhost:9090/phpmyadmin/index.php?route=/database/structure&db=medicalapp
- **Port MySQL** : `3306` (par défaut)
- **Host** : `localhost`

### Identifiants
- **Username** : `root` (par défaut)
- **Password** : (à configurer selon votre installation)

---

## ⚙️ Configuration dans application.properties

La configuration actuelle est :

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/medicalapp?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

### ⚠️ Important : Configurer le mot de passe

Si votre MySQL a un mot de passe, modifiez la ligne dans `application.properties` :

```properties
spring.datasource.password=votre_mot_de_passe
```

---

## 🗄️ Structure de la base de données

### Table : `users`

La table sera créée automatiquement par Hibernate au démarrage de l'application grâce à :
```properties
spring.jpa.hibernate.ddl-auto=update
```

Structure de la table :

| Colonne | Type | Description |
|---------|------|-------------|
| `id` | BIGINT | Identifiant unique (auto-increment) |
| `email` | VARCHAR(255) | Email unique |
| `password` | VARCHAR(255) | Mot de passe hashé |
| `first_name` | VARCHAR(255) | Prénom |
| `last_name` | VARCHAR(255) | Nom |
| `phone_number` | VARCHAR(50) | Numéro de téléphone (optionnel) |
| `role` | VARCHAR(20) | Rôle (PATIENT, DOCTOR, ADMIN) |
| `photo_url` | VARCHAR(500) | URL de la photo (optionnel) |
| `created_at` | DATETIME | Date de création |
| `updated_at` | DATETIME | Date de mise à jour |
| `enabled` | BOOLEAN | Statut actif/inactif |

---

## 🚀 Étapes de configuration

### 1. Vérifier que MySQL est démarré
Assurez-vous que votre serveur MySQL est en cours d'exécution.

### 2. Accéder à phpMyAdmin
Ouvrez votre navigateur et allez sur :
```
http://localhost:9090/phpmyadmin/index.php?route=/database/structure&db=medicalapp
```

### 3. Vérifier/Créer la base de données
- Si la base `medicalapp` n'existe pas, créez-la via phpMyAdmin ou utilisez le script `database_setup.sql`
- La base sera créée automatiquement si `createDatabaseIfNotExist=true` est dans l'URL

### 4. Configurer le mot de passe (si nécessaire)
Si votre MySQL a un mot de passe, modifiez `application.properties` :
```properties
spring.datasource.password=votre_mot_de_passe
```

### 5. Démarrer l'application
Au démarrage, Hibernate créera automatiquement la table `users` si elle n'existe pas.

---

## 🔍 Vérification

### Via phpMyAdmin
1. Allez sur http://localhost:9090/phpmyadmin
2. Sélectionnez la base `medicalapp`
3. Vérifiez que la table `users` existe
4. Consultez les données après avoir créé des utilisateurs via l'API

### Via l'application
1. Démarrez l'application Spring Boot
2. Les logs afficheront les requêtes SQL si `spring.jpa.show-sql=true`
3. Testez l'inscription d'un utilisateur via l'API
4. Vérifiez dans phpMyAdmin que l'utilisateur a été créé

---

## 🛠️ Script SQL manuel

Si vous préférez créer la table manuellement, exécutez ce script dans phpMyAdmin :

```sql
CREATE DATABASE IF NOT EXISTS medicalapp CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE medicalapp;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(50),
    role VARCHAR(20) NOT NULL,
    photo_url VARCHAR(500),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    INDEX idx_email (email),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

## ⚠️ Dépannage

### Erreur : "Access denied for user 'root'@'localhost'"
- Vérifiez le mot de passe dans `application.properties`
- Ou créez un utilisateur MySQL avec les permissions nécessaires

### Erreur : "Unknown database 'medicalapp'"
- Créez la base de données manuellement via phpMyAdmin
- Ou vérifiez que `createDatabaseIfNotExist=true` est dans l'URL

### Erreur : "Table 'users' doesn't exist"
- L'application créera la table automatiquement au démarrage
- Ou exécutez le script SQL manuellement

### Erreur de connexion
- Vérifiez que MySQL est démarré
- Vérifiez le port (3306 par défaut)
- Vérifiez les identifiants dans `application.properties`

---

## 📌 Liens utiles

- **phpMyAdmin** : http://localhost:9090/phpmyadmin
- **Base de données** : http://localhost:9090/phpmyadmin/index.php?route=/database/structure&db=medicalapp
- **API Swagger** : http://localhost:8081/swagger-ui.html

