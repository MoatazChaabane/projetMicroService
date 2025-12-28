import api from './api'

export const symptomAnalysisAPI = {
  // Analyser des symptômes
  analyzeSymptoms: (request) => {
    return api.post('/symptom-analysis', request)
  },
  
  // Récupérer une analyse par ID
  getAnalysisById: (id) => {
    return api.get(`/symptom-analysis/${id}`)
  },
  
  // Récupérer les analyses d'un patient
  getPatientAnalyses: (patientId) => {
    return api.get(`/symptom-analysis/patient/${patientId}`)
  },
  
  // Récupérer l'analyse d'un rendez-vous
  getAnalysisByAppointmentId: (appointmentId) => {
    return api.get(`/symptom-analysis/appointment/${appointmentId}`)
  }
}

