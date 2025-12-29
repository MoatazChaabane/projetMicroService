import api from './api'

export const symptomAnalysisAPI = {

  analyzeSymptoms: (request) => {
    return api.post('/symptom-analysis', request)
  },

  getAnalysisById: (id) => {
    return api.get(`/symptom-analysis/${id}`)
  },

  getPatientAnalyses: (patientId) => {
    return api.get(`/symptom-analysis/patient/${patientId}`)
  },

  getAnalysisByAppointmentId: (appointmentId) => {
    return api.get(`/symptom-analysis/appointment/${appointmentId}`)
  }
}

