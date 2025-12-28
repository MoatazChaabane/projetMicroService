import api from './api'

export const medicalRecordAPI = {
  // Récupérer le dossier médical d'un patient
  getMedicalRecordByPatientId: (patientId) => {
    return api.get(`/medical-records/patient/${patientId}`)
  },
  
  // Créer un dossier médical
  createMedicalRecord: (patientId) => {
    return api.post(`/medical-records/patient/${patientId}`)
  },
  
  // Récupérer la timeline
  getTimeline: (patientId) => {
    return api.get(`/medical-records/patient/${patientId}/timeline`)
  },
  
  // Rechercher dans l'historique
  searchInHistory: (patientId, searchTerm) => {
    return api.get(`/medical-records/patient/${patientId}/search`, {
      params: { q: searchTerm }
    })
  },
  
  // Ajouter une consultation
  addVisit: (data) => {
    return api.post('/medical-records/visits', data)
  },
  
  // Ajouter une pièce jointe
  addAttachment: (formData) => {
    return api.post('/medical-records/attachments', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },
  
  // Télécharger une pièce jointe
  downloadAttachment: (attachmentId) => {
    return api.get(`/medical-records/attachments/${attachmentId}/download`, {
      responseType: 'blob'
    })
  },
  
  // Exporter le dossier en PDF
  exportPDF: (patientId) => {
    return api.get(`/medical-records/patient/${patientId}/export-pdf`, {
      responseType: 'blob'
    })
  }
}

