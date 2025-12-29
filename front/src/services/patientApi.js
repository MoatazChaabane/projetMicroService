import api from './api'

export const patientAPI = {
  createPatient: (data) => api.post('/patients', data),
  
  getPatientById: (id) => api.get(`/patients/${id}`),
  
  getAllPatients: (params) => {
    const { page = 0, size = 10, sortBy = 'id', sortDir = 'asc' } = params || {}
    return api.get('/patients', {
      params: { page, size, sortBy, sortDir }
    })
  },
  
  searchPatients: (params) => {
    const { search = '', page = 0, size = 10, sortBy = 'id', sortDir = 'asc' } = params || {}
    return api.get('/patients/search', {
      params: { search, page, size, sortBy, sortDir }
    })
  },
  
  updatePatient: (id, data) => api.put(`/patients/${id}`, data),
  
  deletePatient: (id) => api.delete(`/patients/${id}`),
  
  countPatients: () => api.get('/patients/count'),
  
  getPatientByTelephone: (telephone) => api.get(`/patients/by-phone/${telephone}`)
}

