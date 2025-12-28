# API Dossiers Médicaux

## Description

API pour la gestion complète des dossiers médicaux incluant consultations, notes, analyses (PDF), images, et historique chronologique.

---

## Modèle de Données

### MedicalRecord (Dossier Médical)

- `id` (Long) : ID du dossier
- `patientId` (Long) : ID du patient (unique, un dossier par patient)
- `notes` (String) : Notes générales du dossier
- `createdAt` (LocalDateTime) : Date de création
- `updatedAt` (LocalDateTime) : Date de dernière mise à jour

### Visit (Consultation)

- `id` (Long) : ID de la consultation
- `medicalRecordId` (Long) : ID du dossier médical
- `doctorId` (Long) : ID du docteur
- `visitDate` (LocalDate) : Date de la consultation
- `visitTime` (LocalTime) : Heure de la consultation
- `reason` (String) : Motif de la consultation
- `symptoms` (String) : Symptômes observés
- `diagnosis` (String) : Diagnostic
- `treatment` (String) : Traitement prescrit
- `notes` (String) : Notes additionnelles

### MedicalAttachment (Pièce Jointe)

- `id` (Long) : ID de la pièce jointe
- `medicalRecordId` (Long) : ID du dossier médical
- `doctorId` (Long) : ID du docteur (optionnel)
- `fileName` (String) : Nom original du fichier
- `fileType` (String) : Type MIME
- `attachmentType` (Enum) : ANALYSE, IMAGE, DOCUMENT, PRESCRIPTION, CERTIFICAT, AUTRE
- `description` (String) : Description du fichier
- `fileSize` (Long) : Taille en bytes
- `gridFsId` (String) : ID GridFS (si utilisé)
- `filePath` (String) : Chemin local du fichier

---

## Sécurité et RBAC

### Rôles et Permissions

- **PATIENT** : 
  - Peut voir son propre dossier médical
  - Peut voir la timeline de son dossier
  - Peut rechercher dans son historique
  - Peut exporter son dossier en PDF
  - Peut télécharger les pièces jointes de son dossier

- **DOCTOR** : 
  - Peut voir les dossiers de ses patients
  - Peut créer des dossiers médicaux
  - Peut ajouter des consultations
  - Peut ajouter des pièces jointes
  - Peut voir la timeline
  - Peut rechercher dans l'historique
  - Peut exporter les dossiers en PDF

- **ADMIN** : 
  - Peut voir tous les dossiers
  - Peut créer des dossiers
  - Peut ajouter consultations et pièces jointes
  - Accès complet à toutes les fonctionnalités

---

## Endpoints

### 1. Récupérer le dossier médical d'un patient

**GET** `/api/medical-records/patient/{patientId}`

**Headers:**
```
Authorization: Bearer <token>
```

**Réponse:** `200 OK`
```json
{
  "id": 1,
  "patientId": 1,
  "patientName": "Jean Dupont",
  "notes": "Notes générales...",
  "visitsCount": 5,
  "attachmentsCount": 3,
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T14:30:00"
}
```

---

### 2. Créer un dossier médical

**POST** `/api/medical-records/patient/{patientId}`

**Headers:**
```
Authorization: Bearer <token>
```

**Réponse:** `201 Created` avec `MedicalRecordResponseDTO`

---

### 3. Récupérer la timeline (historique chronologique)

**GET** `/api/medical-records/patient/{patientId}/timeline`

**Headers:**
```
Authorization: Bearer <token>
```

**Réponse:** `200 OK`
```json
[
  {
    "type": "VISIT",
    "id": 1,
    "dateTime": "2024-01-15T14:30:00",
    "visitDate": "2024-01-15",
    "visitTime": "14:30",
    "title": "Consultation - Contrôle de routine",
    "doctorName": "Dr. Marie Martin",
    "doctorId": 1,
    "reason": "Contrôle de routine",
    "diagnosis": "État de santé stable"
  },
  {
    "type": "ATTACHMENT",
    "id": 1,
    "dateTime": "2024-01-14T10:00:00",
    "title": "analyse_sang.pdf",
    "doctorName": "Dr. Marie Martin",
    "doctorId": 1,
    "fileName": "analyse_sang.pdf",
    "attachmentType": "ANALYSE",
    "description": "Analyse de sang - NFS"
  }
]
```

---

### 4. Rechercher dans l'historique

**GET** `/api/medical-records/patient/{patientId}/search?q={searchTerm}`

**Headers:**
```
Authorization: Bearer <token>
```

**Réponse:** `200 OK` avec `List<TimelineItemDTO>`

---

### 5. Ajouter une consultation

**POST** `/api/medical-records/visits`

**Headers:**
```
Authorization: Bearer <token>
Content-Type: application/json
```

**Body:**
```json
{
  "medicalRecordId": 1,
  "doctorId": 1,
  "visitDate": "2024-01-15",
  "visitTime": "14:30",
  "reason": "Contrôle de routine",
  "symptoms": "Aucun symptôme particulier",
  "diagnosis": "État de santé stable",
  "treatment": "Aucun traitement nécessaire",
  "notes": "À revoir dans 6 mois"
}
```

**Réponse:** `201 Created` avec `VisitResponseDTO`

---

### 6. Ajouter une pièce jointe

**POST** `/api/medical-records/attachments`

**Headers:**
```
Authorization: Bearer <token>
Content-Type: multipart/form-data
```

**Form Data:**
- `medicalRecordId` (Long) : ID du dossier médical
- `doctorId` (Long, optionnel) : ID du docteur
- `attachmentType` (Enum) : ANALYSE, IMAGE, DOCUMENT, PRESCRIPTION, CERTIFICAT, AUTRE
- `description` (String, optionnel) : Description du fichier
- `file` (MultipartFile) : Fichier à uploader

**Réponse:** `201 Created` avec `AttachmentResponseDTO`

---

### 7. Télécharger une pièce jointe

**GET** `/api/medical-records/attachments/{attachmentId}/download`

**Headers:**
```
Authorization: Bearer <token>
```

**Réponse:** `200 OK` avec le fichier en binaire (PDF, image, etc.)

---

### 8. Exporter le dossier médical en PDF

**GET** `/api/medical-records/patient/{patientId}/export-pdf`

**Headers:**
```
Authorization: Bearer <token>
```

**Réponse:** `200 OK` avec le PDF en binaire

**Contenu du PDF:**
- Informations patient
- Liste chronologique des consultations (date, docteur, motif, symptômes, diagnostic, traitement, notes)
- Liste des pièces jointes (nom, type, date, description)
- Notes générales
- Date de génération

---

## Stockage des Fichiers

### Stockage Local (par défaut)

- **Répertoire:** `medical-records/attachments/`
- **Format:** `{timestamp}_{uuid}.{extension}`
- **Taille max:** 10MB (configurable via `medical.records.attachments.max-size`)

### GridFS (Optionnel pour MongoDB)

Pour utiliser GridFS avec MongoDB :

1. **Configuration:**
```java
@Configuration
public class GridFSConfig {
    @Bean
    public GridFSBucket gridFSBucket(MongoDatabaseFactory mongoDatabaseFactory) {
        return GridFSBuckets.create(mongoDatabaseFactory.getMongoDatabase());
    }
}
```

2. **Service modifié:**
```java
public String storeInGridFS(byte[] fileContent, String fileName) {
    ObjectId fileId = gridFSBucket.uploadFromStream(
        fileName,
        new ByteArrayInputStream(fileContent)
    );
    return fileId.toString();
}
```

---

## Configuration

```properties
# Medical Records Configuration
medical.records.attachments.directory=medical-records/attachments
medical.records.attachments.max-size=10485760  # 10MB en bytes
```

---

## Codes de Statut HTTP

- `200 OK` : Succès
- `201 Created` : Ressource créée
- `400 Bad Request` : Données invalides
- `401 Unauthorized` : Token manquant ou invalide
- `403 Forbidden` : Accès refusé (mauvais rôle ou pas autorisé à voir ce dossier)
- `404 Not Found` : Ressource non trouvée
- `500 Internal Server Error` : Erreur serveur

---

## Exemples d'Utilisation

### Exemple 1: Ajouter une consultation

```bash
curl -X POST http://localhost:8081/api/medical-records/visits \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "medicalRecordId": 1,
    "doctorId": 1,
    "visitDate": "2024-01-15",
    "visitTime": "14:30",
    "reason": "Consultation de routine",
    "symptoms": "Aucun symptôme",
    "diagnosis": "Bonne santé",
    "treatment": "Aucun traitement",
    "notes": "À revoir dans 6 mois"
  }'
```

### Exemple 2: Ajouter une pièce jointe (analyse PDF)

```bash
curl -X POST http://localhost:8081/api/medical-records/attachments \
  -H "Authorization: Bearer <token>" \
  -F "medicalRecordId=1" \
  -F "doctorId=1" \
  -F "attachmentType=ANALYSE" \
  -F "description=Analyse de sang - NFS" \
  -F "file=@analyse_sang.pdf"
```

### Exemple 3: Récupérer la timeline

```bash
curl -X GET "http://localhost:8081/api/medical-records/patient/1/timeline" \
  -H "Authorization: Bearer <token>"
```

### Exemple 4: Rechercher dans l'historique

```bash
curl -X GET "http://localhost:8081/api/medical-records/patient/1/search?q=rhume" \
  -H "Authorization: Bearer <token>"
```

### Exemple 5: Exporter le dossier en PDF

```bash
curl -X GET "http://localhost:8081/api/medical-records/patient/1/export-pdf" \
  -H "Authorization: Bearer <token>" \
  --output dossier-medical.pdf
```

---

## Notes Importantes

1. **RBAC Strict** : Chaque endpoint vérifie les permissions selon le rôle de l'utilisateur
2. **Un dossier par patient** : Un patient ne peut avoir qu'un seul dossier médical
3. **Stockage fichiers** : Par défaut, stockage local. GridFS peut être utilisé pour MongoDB
4. **Taille max** : 10MB par fichier (configurable)
5. **Timeline** : Triée automatiquement par date décroissante (plus récent en premier)
6. **Recherche** : Recherche dans les consultations (motif, symptômes, diagnostic, traitement, notes) et descriptions de pièces jointes
7. **Export PDF** : Inclut toutes les consultations et la liste des pièces jointes

