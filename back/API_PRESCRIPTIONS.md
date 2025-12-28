# API Prescriptions (Ordonnances)

## Description

API pour la gestion des ordonnances médicales avec génération automatique de PDF et signature numérique.

---

## Modèle de Données

### Prescription (Ordonnance)

- `id` (Long) : ID de l'ordonnance
- `patientId` (Long) : ID du patient
- `doctorId` (Long) : ID du docteur
- `medications` (List<Medication>) : Liste des médicaments
- `instructions` (String) : Instructions générales
- `date` (LocalDate) : Date de l'ordonnance
- `pdfUrl` (String) : URL/chemin du PDF généré
- `pdfId` (String) : ID du fichier si stocké dans GridFS (optionnel)
- `signatureHash` (String) : Hash SHA-256 de la signature numérique
- `signatureMetadata` (String) : Métadonnées de la signature (JSON)

### Medication (Médicament)

- `name` (String) : Nom du médicament
- `dosage` (String) : Dosage (ex: "500mg")
- `frequency` (String) : Fréquence (ex: "3 fois par jour")
- `duration` (String) : Durée (ex: "7 jours")
- `instructions` (String) : Instructions spéciales

---

## Sécurité et Accès

### Rôles et Permissions

- **DOCTOR** : Peut créer des ordonnances et voir celles qu'il a créées
- **PATIENT** : Peut voir uniquement ses propres ordonnances
- **ADMIN** : Peut voir toutes les ordonnances

---

## Endpoints

### 1. Créer une ordonnance

**POST** `/api/prescriptions`

**Headers:**
```
Authorization: Bearer <token>
Content-Type: application/json
```

**Body:**
```json
{
  "patientId": 1,
  "doctorId": 1,
  "medications": [
    {
      "name": "Paracétamol",
      "dosage": "500mg",
      "frequency": "3 fois par jour",
      "duration": "7 jours",
      "instructions": "Pendant les repas"
    },
    {
      "name": "Ibuprofène",
      "dosage": "400mg",
      "frequency": "2 fois par jour",
      "duration": "5 jours",
      "instructions": "Après les repas"
    }
  ],
  "instructions": "Repos recommandé. Éviter l'alcool pendant le traitement.",
  "date": "2024-01-15"
}
```

**Réponse:** `200 OK`
```json
{
  "id": 1,
  "patientId": 1,
  "patientName": "Jean Dupont",
  "doctorId": 1,
  "doctorName": "Dr. Marie Martin",
  "doctorSpeciality": "CARDIOLOGIE",
  "medications": [...],
  "instructions": "Repos recommandé...",
  "date": "2024-01-15",
  "pdfUrl": "/prescriptions/pdfs/prescription-1-uuid.pdf",
  "signatureHash": "a1b2c3d4e5f6...",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

---

### 2. Récupérer une ordonnance par ID

**GET** `/api/prescriptions/{id}`

**Headers:**
```
Authorization: Bearer <token>
```

**Réponse:** `200 OK` avec `PrescriptionResponseDTO`

---

### 3. Récupérer les ordonnances d'un patient

**GET** `/api/prescriptions/patient/{patientId}?page=0&size=10`

**Headers:**
```
Authorization: Bearer <token>
```

**Réponse:** `200 OK` avec `PageResponse<PrescriptionResponseDTO>`

---

### 4. Récupérer les ordonnances d'un docteur

**GET** `/api/prescriptions/doctor/{doctorId}?page=0&size=10`

**Headers:**
```
Authorization: Bearer <token>
```

**Réponse:** `200 OK` avec `PageResponse<PrescriptionResponseDTO>`

---

### 5. Récupérer toutes les ordonnances (Admin)

**GET** `/api/prescriptions/all`

**Headers:**
```
Authorization: Bearer <token>
```

**Réponse:** `200 OK` avec `List<PrescriptionResponseDTO>`

---

## Génération PDF

### Fonctionnalités

- Génération automatique lors de la création de l'ordonnance
- Format A4 standard
- Contenu :
  - En-tête "ORDONNANCE MÉDICALE"
  - Informations du docteur (nom, spécialité)
  - Informations du patient (nom)
  - Date de l'ordonnance
  - Tableau des médicaments (nom, dosage, fréquence, durée, instructions)
  - Instructions générales
  - Signature numérique (hash)

### Stockage

- Par défaut : stockage fichier local dans `prescriptions/pdfs/`
- Format : `prescription-{id}-{uuid}.pdf`
- URL : `/prescriptions/pdfs/{filename}`

### Configuration

```properties
# application.properties
prescription.pdf.directory=prescriptions/pdfs
prescription.pdf.base-url=/prescriptions/pdfs
```

---

## Signature Numérique

### Algorithme

- **Hash** : SHA-256
- **Données signées** : ID prescription, ID patient, ID docteur, date, createdAt, liste des médicaments

### Format de la Signature

```java
dataToSign = "PRESCRIPTION-{id}-{patientId}-{doctorId}-{date}-{createdAt}-{medications}"
signatureHash = SHA-256(dataToSign)
```

### Métadonnées

JSON contenant :
- `prescriptionId`
- `patientId`
- `doctorId`
- `date`
- `createdAt`
- `algorithm`: "SHA-256"
- `version`: "1.0"

### Vérification

Pour vérifier l'intégrité d'une ordonnance, recalculer le hash avec les mêmes données et comparer avec `signatureHash`.

---

## Stockage GridFS (Optionnel pour MongoDB)

Si vous utilisez MongoDB et souhaitez stocker les PDFs dans GridFS :

1. **Configuration MongoDB GridFS** :
```java
@Configuration
public class GridFSConfig {
    @Bean
    public GridFSBucket gridFSBucket(MongoDatabaseFactory mongoDatabaseFactory) {
        return GridFSBuckets.create(mongoDatabaseFactory.getMongoDatabase());
    }
}
```

2. **Service modifié pour GridFS** :
```java
public String storePDFInGridFS(byte[] pdfContent, String filename) {
    ObjectId fileId = gridFSBucket.uploadFromStream(
        filename,
        new ByteArrayInputStream(pdfContent)
    );
    return fileId.toString();
}
```

3. **Récupération depuis GridFS** :
```java
public byte[] retrievePDFFromGridFS(String fileId) {
    GridFSDownloadStream downloadStream = gridFSBucket.openDownloadStream(
        new ObjectId(fileId)
    );
    return downloadStream.readAllBytes();
}
```

---

## Dépendances Maven

Pour la génération PDF avec iText, ajouter dans `pom.xml` :

```xml
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>itextpdf</artifactId>
    <version>5.5.13.3</version>
</dependency>
```

---

## Codes de Statut HTTP

- `200 OK` : Succès
- `201 Created` : Ressource créée (si nécessaire)
- `400 Bad Request` : Données invalides
- `401 Unauthorized` : Token manquant ou invalide
- `403 Forbidden` : Accès refusé (mauvais rôle)
- `404 Not Found` : Ressource non trouvée

---

## Exemples d'Utilisation

### Exemple 1: Créer une ordonnance

```bash
curl -X POST http://localhost:8081/api/prescriptions \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "patientId": 1,
    "doctorId": 1,
    "medications": [
      {
        "name": "Amoxicilline",
        "dosage": "500mg",
        "frequency": "3 fois par jour",
        "duration": "10 jours",
        "instructions": "Pendant les repas"
      }
    ],
    "instructions": "Prendre les médicaments à heures fixes. Consulter si aggravation.",
    "date": "2024-01-15"
  }'
```

### Exemple 2: Récupérer les ordonnances d'un patient

```bash
curl -X GET "http://localhost:8081/api/prescriptions/patient/1?page=0&size=10" \
  -H "Authorization: Bearer <token>"
```

---

## Notes Importantes

1. **Sécurité** : Seuls les docteurs peuvent créer des ordonnances
2. **PDF** : Généré automatiquement et stocké sur le serveur
3. **Signature** : Hash SHA-256 pour vérifier l'intégrité
4. **Accès** : Les patients voient uniquement leurs ordonnances
5. **Audit** : Toutes les ordonnances sont horodatées (createdAt, updatedAt)

