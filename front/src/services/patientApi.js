import api from './api'

export const patientAPI = {
  // Créer un patient
  createPatient: (data) => api.post('/patients', data),
  
  // Récupérer un patient par ID
  getPatientById: (id) => api.get(`/patients/${id}`),
  
  // Liste paginée
  getAllPatients: (params) => {
    const { page = 0, size = 10, sortBy = 'id', sortDir = 'asc' } = params || {}
    return api.get('/patients', {
      params: { page, size, sortBy, sortDir }
    })
  },
  
  // Recherche
  searchPatients: (params) => {
    const { search = '', page = 0, size = 10, sortBy = 'id', sortDir = 'asc' } = params || {}
    return api.get('/patients/search', {
      params: { search, page, size, sortBy, sortDir }
    })
  },
  
  // Modifier un patient
  updatePatient: (id, data) => api.put(`/patients/${id}`, data),
  
  // Supprimer un patient
  deletePatient: (id) => api.delete(`/patients/${id}`),
  
  // Compter les patients
  countPatients: () => api.get('/patients/count')
}

