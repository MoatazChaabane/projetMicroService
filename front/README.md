# Application Médicale - Frontend React

Application React pour la gestion des profils utilisateurs dans une application médicale.

## 🚀 Installation

```bash
# Installer les dépendances
npm install

# Démarrer le serveur de développement
npm run dev

# Build pour la production
npm run build
```

## 📁 Structure du projet

```
front/
├── src/
│   ├── components/          # Composants réutilisables
│   │   └── PrivateRoute.jsx
│   ├── context/            # Context API
│   │   └── AuthContext.jsx
│   ├── pages/              # Pages de l'application
│   │   ├── Login.jsx
│   │   ├── Register.jsx
│   │   ├── Profile.jsx
│   │   ├── EditProfile.jsx
│   │   └── ChangePassword.jsx
│   ├── services/           # Services API
│   │   └── api.js
│   ├── App.jsx             # Composant principal avec routes
│   ├── main.jsx            # Point d'entrée
│   └── index.css           # Styles globaux
├── package.json
├── vite.config.js
└── index.html
```

## 🎯 Fonctionnalités

- ✅ **Authentification** : Login et Register avec validation
- ✅ **Gestion de profil** : Affichage et modification du profil
- ✅ **Changement de mot de passe** : Sécurisé avec validation
- ✅ **Route Guards** : Protection des routes privées
- ✅ **Gestion d'état** : Context API pour l'authentification
- ✅ **Validation** : React Hook Form avec validation
- ✅ **Appels API** : Axios avec gestion des cookies de session

## 🔧 Configuration

### URL de l'API Backend

Par défaut, l'API backend est configurée sur `http://localhost:8081`.

Pour modifier l'URL, éditez `src/services/api.js` :

```javascript
const API_BASE_URL = 'http://localhost:8081/api'
```

### Proxy Vite

Le proxy est configuré dans `vite.config.js` pour rediriger `/api` vers le backend.

## 📝 Pages

### Login (`/login`)
- Formulaire de connexion avec validation
- Redirection automatique si déjà connecté
- Gestion des erreurs

### Register (`/register`)
- Formulaire d'inscription complet
- Validation des champs
- Sélection du rôle (PATIENT, DOCTOR, ADMIN)

### Profile (`/profile`)
- Affichage des informations du profil
- Bouton de déconnexion
- Liens vers édition et changement de mot de passe

### Edit Profile (`/profile/edit`)
- Modification des informations du profil
- Validation des champs
- Mise à jour en temps réel

### Change Password (`/profile/change-password`)
- Changement de mot de passe sécurisé
- Validation de confirmation
- Vérification du mot de passe actuel

## 🔐 Authentification

L'authentification utilise les cookies de session (JSESSIONID) gérés automatiquement par Axios avec `withCredentials: true`.

## 🛡️ Route Guards

Les routes privées sont protégées par le composant `PrivateRoute` qui vérifie l'authentification avant d'afficher la page.

## 📦 Dépendances principales

- **React** : 18.2.0
- **React Router DOM** : 6.20.0
- **React Hook Form** : 7.48.2
- **Axios** : 1.6.2
- **js-cookie** : 3.0.5
- **Vite** : 5.0.8

## 🚦 Démarrage rapide

1. Installer les dépendances : `npm install`
2. Démarrer le backend sur `http://localhost:8081`
3. Démarrer le frontend : `npm run dev`
4. Ouvrir `http://localhost:3000`

## 📱 Responsive

L'application est responsive et s'adapte aux différentes tailles d'écran.

## 🎨 Styles

Les styles sont organisés par page avec des fichiers CSS dédiés. Le design utilise un gradient moderne avec des cartes blanches pour le contenu.

