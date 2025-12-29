import api from './api'

export const medicalRecordAPI = {
  getMedicalRecordByPatientId: (patientId) => {
    return api.get(`/medical-records/patient/${patientId}`)
  },
  
  createMedicalRecord: (patientId) => {
    return api.post(`/medical-records/patient/${patientId}`)
  },
  
  getTimeline: (patientId) => {
    return api.get(`/medical-records/patient/${patientId}/timeline`)
  },
  
  searchInHistory: (patientId, searchTerm) => {
    return api.get(`/medical-records/patient/${patientId}/search`, {
      params: { q: searchTerm }
    })
  },
  
  addVisit: (data) => {
    return api.post('/medical-records/visits', data)
  },
  
  addAttachment: (formData) => {
    return api.post('/medical-records/attachments', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },
  
  downloadAttachment: (attachmentId) => {
    return api.get(`/medical-records/attachments/${attachmentId}/download`, {
      responseType: 'blob'
    })
  },
  
  exportPDF: (patientId) => {
    return api.get(`/medical-records/patient/${patientId}/export-pdf`, {
      responseType: 'blob'
    })
  }
}

