# 🔗 Accès à la base de données MongoDB

## 🌐 MongoDB Atlas (Interface Web)

### Lien principal pour accéder à MongoDB Atlas
```
https://cloud.mongodb.com
```

---

## 📋 Informations de connexion

### Cluster
- **Nom du cluster** : `Cluster0`
- **URL du cluster** : `cluster0.bxa8u.mongodb.net`

### Base de données
- **Nom de la base** : `MedicalApp`

### Utilisateur
- **Username** : `benyounesmaleek`
- **Password** : `malek`

### URI de connexion complète
```
mongodb+srv://benyounesmaleek:malek@cluster0.bxa8u.mongodb.net/MedicalApp
```

---

## 🔐 Étapes pour accéder à MongoDB Atlas

### 1. Se connecter à MongoDB Atlas
1. Allez sur : **https://cloud.mongodb.com**
2. Cliquez sur **"Sign In"** ou **"Log In"**
3. Entrez vos identifiants MongoDB Atlas

### 2. Accéder à votre cluster
1. Une fois connecté, vous verrez votre **Dashboard**
2. Cliquez sur votre cluster : **Cluster0**
3. Vous verrez les informations du cluster

### 3. Accéder à la base de données
1. Dans le menu de gauche, cliquez sur **"Browse Collections"**
2. Sélectionnez la base de données : **MedicalApp**
3. Vous verrez les collections, notamment : **users**

### 4. Voir les données
1. Cliquez sur la collection **users**
2. Vous verrez tous les utilisateurs enregistrés dans l'application

---

## 🛠️ Outils pour accéder à MongoDB

### Option 1 : MongoDB Atlas Web Interface (Recommandé)
```
https://cloud.mongodb.com
```
- Interface web intuitive
- Pas besoin d'installation
- Accès direct depuis le navigateur

### Option 2 : MongoDB Compass (Application Desktop)
1. Téléchargez MongoDB Compass : https://www.mongodb.com/try/download/compass
2. Utilisez la chaîne de connexion :
   ```
   mongodb+srv://benyounesmaleek:malek@cluster0.bxa8u.mongodb.net/MedicalApp
   ```

### Option 3 : MongoDB Shell (mongosh)
1. Installez MongoDB Shell
2. Connectez-vous avec :
   ```bash
   mongosh "mongodb+srv://cluster0.bxa8u.mongodb.net/MedicalApp" --username benyounesmaleek --password malek
   ```

---

## 📊 Collections dans la base de données

### Collection : `users`
- Contient tous les utilisateurs de l'application
- Champs :
  - `_id` : Identifiant unique MongoDB
  - `email` : Email de l'utilisateur (unique)
  - `password` : Mot de passe hashé
  - `firstName` : Prénom
  - `lastName` : Nom
  - `phoneNumber` : Numéro de téléphone
  - `role` : Rôle (PATIENT, DOCTOR, ADMIN)
  - `photoUrl` : URL de la photo de profil
  - `createdAt` : Date de création
  - `updatedAt` : Date de mise à jour
  - `enabled` : Statut actif/inactif

---

## 🔒 Sécurité

### Vérifications importantes dans MongoDB Atlas

#### 1. Network Access (Accès réseau)
- Allez dans **"Network Access"** dans le menu
- Vérifiez que votre IP est autorisée
- Pour le développement, vous pouvez autoriser `0.0.0.0/0` (tous les IPs) - ⚠️ **Seulement pour le développement**

#### 2. Database Access (Accès base de données)
- Allez dans **"Database Access"** dans le menu
- Vérifiez que l'utilisateur `benyounesmaleek` existe
- Vérifiez les permissions (rôle : `Atlas admin` ou `readWrite`)

---

## 📝 Commandes utiles dans MongoDB Shell

### Se connecter
```bash
mongosh "mongodb+srv://cluster0.bxa8u.mongodb.net/MedicalApp" --username benyounesmaleek --password malek
```

### Voir les bases de données
```javascript
show dbs
```

### Utiliser la base MedicalApp
```javascript
use MedicalApp
```

### Voir les collections
```javascript
show collections
```

### Voir tous les utilisateurs
```javascript
db.users.find().pretty()
```

### Compter les utilisateurs
```javascript
db.users.countDocuments()
```

### Trouver un utilisateur par email
```javascript
db.users.findOne({ email: "patient@example.com" })
```

---

## 🚨 En cas de problème de connexion

### Erreur : "not authorized"
1. Vérifiez que votre IP est autorisée dans **Network Access**
2. Vérifiez les permissions de l'utilisateur dans **Database Access**

### Erreur : "authentication failed"
1. Vérifiez le nom d'utilisateur et le mot de passe
2. Vérifiez que l'utilisateur existe dans **Database Access**

### Erreur : "connection timeout"
1. Vérifiez votre connexion internet
2. Vérifiez que le cluster est actif dans MongoDB Atlas

---

## 📌 Liens rapides

- **MongoDB Atlas Dashboard** : https://cloud.mongodb.com
- **MongoDB Compass** : https://www.mongodb.com/try/download/compass
- **Documentation MongoDB** : https://docs.mongodb.com

