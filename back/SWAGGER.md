# Documentation Swagger/OpenAPI

## Accès à Swagger UI

Une fois l'application démarrée, vous pouvez accéder à la documentation interactive Swagger UI via :

**URL**: http://localhost:8080/swagger-ui.html

## Endpoints Swagger

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs JSON**: http://localhost:8080/v3/api-docs
- **API Docs YAML**: http://localhost:8080/v3/api-docs.yaml

## Fonctionnalités

### Documentation Interactive
- Interface utilisateur intuitive pour tester les endpoints
- Documentation complète de tous les endpoints REST
- Exemples de requêtes et réponses
- Schémas de données détaillés

### Test des Endpoints
- Test direct des endpoints depuis l'interface
- Support de l'authentification par session
- Validation des données en temps réel
- Affichage des codes de réponse HTTP

### Groupes d'Endpoints

1. **Authentification** (`/api/auth`)
   - Inscription
   - Connexion
   - Déconnexion
   - Utilisateur actuel

2. **Gestion du Profil** (`/api/profile`)
   - Récupérer le profil
   - Modifier le profil
   - Changer le mot de passe
   - Upload/Supprimer photo

3. **Administration** (`/api/admin`)
   - Liste des utilisateurs
   - Gestion des utilisateurs
   - (Réservé aux administrateurs)

## Authentification dans Swagger

Pour tester les endpoints protégés dans Swagger UI :

1. Utilisez d'abord l'endpoint `/api/auth/login` pour vous connecter
2. Spring Security créera automatiquement une session avec un cookie JSESSIONID
3. Les requêtes suivantes utiliseront automatiquement cette session

**Note**: Swagger UI gère automatiquement les cookies de session pour les requêtes authentifiées.

## Configuration

La configuration Swagger se trouve dans :
- `OpenApiConfig.java` : Configuration principale de l'API
- `application.properties` : Paramètres Swagger UI

## Personnalisation

Pour modifier la documentation Swagger, éditez :
- Les annotations `@Operation`, `@ApiResponse` dans les controllers
- Les annotations `@Schema` dans les DTOs
- La classe `OpenApiConfig` pour les informations générales

