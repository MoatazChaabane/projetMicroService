import api from './api'

export const doctorAPI = {
  // Créer un docteur
  createDoctor: (data) => api.post('/doctors', data),
  
  // Récupérer un docteur par ID
  getDoctorById: (id) => api.get(`/doctors/${id}`),
  
  // Récupérer un docteur par ID utilisateur
  getDoctorByUserId: (userId) => api.get(`/doctors/user/${userId}`),
  
  // Liste paginée
  getAllDoctors: (params) => {
    const { page = 0, size = 10, sortBy = 'rating', sortDir = 'desc' } = params || {}
    return api.get('/doctors', {
      params: { page, size, sortBy, sortDir }
    })
  },
  
  // Recherche avancée avec filtres
  searchDoctors: (searchDTO, params) => {
    const { page = 0, size = 10, sortBy = 'rating', sortDir = 'desc' } = params || {}
    return api.post('/doctors/search', searchDTO, {
      params: { page, size, sortBy, sortDir }
    })
  },
  
  // Modifier un docteur
  updateDoctor: (id, data) => api.put(`/doctors/${id}`, data),
  
  // Supprimer un docteur
  deleteDoctor: (id) => api.delete(`/doctors/${id}`),
  
  // Compter les docteurs
  countDoctors: () => api.get('/doctors/count')
}

