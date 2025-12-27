# 🔧 Guide de dépannage

## Erreur 403 sur /api/profile

### Problème
L'erreur 403 signifie que vous n'êtes pas authentifié ou que les cookies de session ne sont pas envoyés correctement.

### Solutions

#### 1. Vérifier que vous êtes connecté
- Assurez-vous d'avoir d'abord fait un login via `/login`
- Vérifiez dans les DevTools (F12) > Application > Cookies que le cookie `JSESSIONID` est présent

#### 2. Vérifier la configuration CORS
Le backend doit autoriser les credentials. Vérifiez que dans `SecurityConfig.java` :
```java
configuration.setAllowCredentials(true);
```

#### 3. Vérifier les cookies dans le navigateur
1. Ouvrez les DevTools (F12)
2. Allez dans l'onglet "Application" (Chrome) ou "Storage" (Firefox)
3. Vérifiez les Cookies pour `http://localhost:3000`
4. Vous devriez voir un cookie `JSESSIONID` après la connexion

#### 4. Vérifier la console du navigateur
- Regardez les requêtes réseau dans l'onglet "Network"
- Vérifiez que les requêtes incluent `withCredentials: true`
- Vérifiez que le header `Cookie` est présent dans les requêtes

#### 5. Test manuel
Testez la connexion avec curl :
```bash
# 1. Se connecter
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"votre@email.com","password":"votrepassword"}' \
  -c cookies.txt \
  -v

# 2. Utiliser le cookie pour accéder au profil
curl -X GET http://localhost:8081/api/profile \
  -b cookies.txt \
  -v
```

## Erreur 404 pour favicon.ico

Cette erreur est mineure et n'affecte pas le fonctionnement de l'application. Un favicon a été ajouté pour la corriger.

## Vérification étape par étape

### 1. Backend démarré
```bash
# Vérifiez que le backend est bien démarré sur le port 8081
curl http://localhost:8081/
```

### 2. Frontend démarré
```bash
# Vérifiez que le frontend est bien démarré sur le port 3000
# Ouvrez http://localhost:3000 dans votre navigateur
```

### 3. Test de connexion
1. Allez sur `http://localhost:3000/login`
2. Connectez-vous avec un utilisateur valide
3. Vérifiez dans les DevTools que le cookie `JSESSIONID` est créé
4. Essayez d'accéder à `/profile`

### 4. Si le problème persiste

#### Option A : Vérifier les logs du backend
Regardez les logs Spring Boot pour voir les erreurs détaillées.

#### Option B : Tester avec Postman
1. Créez une requête POST vers `http://localhost:8081/api/auth/login`
2. Dans l'onglet "Cookies", vérifiez que le cookie est bien reçu
3. Utilisez ce cookie pour les requêtes suivantes

#### Option C : Vérifier la configuration Axios
Dans `src/services/api.js`, assurez-vous que :
```javascript
withCredentials: true
```
est bien présent dans la configuration d'Axios.

## Problèmes courants

### Les cookies ne sont pas persistants
- Vérifiez que vous n'êtes pas en mode navigation privée
- Vérifiez les paramètres de cookies du navigateur

### Erreur CORS
- Vérifiez que `http://localhost:3000` est bien dans la liste des origines autorisées
- Vérifiez que `setAllowCredentials(true)` est configuré

### Session expirée
- Les sessions peuvent expirer. Reconnectez-vous si nécessaire.

