# Module React - Gestion des Patients

## 📋 Vue d'ensemble

Module complet de gestion des patients avec interface utilisateur moderne, pagination, recherche et modals.

## 🎯 Fonctionnalités

- ✅ **Liste paginée** : Affichage des patients avec pagination
- ✅ **Recherche** : Recherche par nom, prénom ou téléphone
- ✅ **Ajout** : Modal pour créer un nouveau patient
- ✅ **Modification** : Modal pour modifier un patient existant
- ✅ **Détails** : Modal pour voir tous les détails d'un patient
- ✅ **Suppression** : Modal de confirmation avec soft delete
- ✅ **Tri** : Tri par colonnes (nom, prénom, date de naissance)
- ✅ **Pagination** : Navigation entre les pages avec sélection de taille
- ✅ **Gestion d'erreurs** : Affichage des erreurs avec possibilité de fermer
- ✅ **Loading states** : Indicateurs de chargement
- ✅ **Responsive** : Adapté aux différentes tailles d'écran

## 📁 Structure des fichiers

```
src/
├── pages/
│   └── Patients.jsx              # Page principale
├── components/
│   └── patients/
│       ├── PatientTable.jsx     # Tableau des patients
│       ├── PatientModal.jsx     # Modal ajout/modification
│       ├── PatientDetailsModal.jsx # Modal détails
│       ├── DeleteConfirmModal.jsx  # Modal confirmation suppression
│       ├── SearchBar.jsx         # Barre de recherche
│       └── Pagination.jsx         # Composant pagination
└── services/
    └── patientApi.js              # Service API pour les patients
```

## 🚀 Utilisation

### Accéder à la page
```
http://localhost:3000/patients
```

### Actions disponibles

1. **Ajouter un patient**
   - Cliquez sur le bouton "+ Ajouter un patient"
   - Remplissez le formulaire
   - Les champs marqués * sont obligatoires

2. **Voir les détails**
   - Cliquez sur l'icône 👁️ dans la ligne du patient

3. **Modifier un patient**
   - Cliquez sur l'icône ✏️ dans la ligne du patient
   - Ou cliquez sur "Modifier" dans la modal de détails

4. **Supprimer un patient**
   - Cliquez sur l'icône 🗑️ dans la ligne du patient
   - Confirmez la suppression

5. **Rechercher**
   - Utilisez la barre de recherche en haut
   - Recherche par nom, prénom ou téléphone
   - Appuyez sur Entrée ou cliquez sur "Rechercher"

6. **Trier**
   - Cliquez sur les en-têtes de colonnes (Nom, Prénom, Date de naissance)
   - Le tri alterne entre ascendant et descendant

7. **Pagination**
   - Utilisez les flèches pour naviguer entre les pages
   - Sélectionnez le nombre d'éléments par page (5, 10, 20, 50)

## 🎨 Composants

### PatientTable
Tableau responsive avec :
- Colonnes : Nom, Prénom, Date de naissance, Âge, Sexe, Téléphone, Actions
- Tri par colonnes
- Badges pour le sexe
- Boutons d'action (Voir, Modifier, Supprimer)

### PatientModal
Modal pour ajouter ou modifier un patient avec :
- Formulaire complet avec validation
- Champs obligatoires marqués avec *
- Validation en temps réel
- Gestion des erreurs

### PatientDetailsModal
Modal pour afficher tous les détails d'un patient :
- Informations personnelles
- Informations médicales
- Contact d'urgence
- Informations système (dates de création/modification)

### DeleteConfirmModal
Modal de confirmation pour la suppression :
- Affiche le nom du patient
- Avertissement sur l'irréversibilité
- Gestion des erreurs

### SearchBar
Barre de recherche avec :
- Champ de recherche
- Bouton de recherche
- Bouton pour effacer la recherche

### Pagination
Composant de pagination avec :
- Navigation première/dernière page
- Navigation précédent/suivant
- Numéros de pages
- Information sur le nombre total d'éléments

## 🔧 Configuration

### Service API
Le service `patientAPI` est configuré dans `src/services/patientApi.js` et utilise l'instance Axios configurée avec les cookies de session.

### Authentification
La page est protégée par `PrivateRoute` et nécessite une authentification. Seuls les utilisateurs avec les rôles DOCTOR ou ADMIN peuvent accéder (configuré côté backend).

## 📱 Responsive

L'interface s'adapte automatiquement aux différentes tailles d'écran :
- Sur mobile : Les colonnes du tableau peuvent être scrollables horizontalement
- Les modals s'adaptent à la taille de l'écran
- La barre de recherche devient verticale sur petits écrans

## 🎯 États de l'application

- **Loading** : Affichage d'un spinner pendant le chargement
- **Error** : Affichage des erreurs avec possibilité de fermer
- **Empty** : Message quand aucun patient n'est trouvé
- **Success** : Actions silencieuses avec rafraîchissement automatique

## 🔐 Sécurité

- Toutes les requêtes nécessitent une authentification
- Les cookies de session sont gérés automatiquement
- Redirection vers login en cas d'erreur 401/403

