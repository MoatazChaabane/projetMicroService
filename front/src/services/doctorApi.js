import api from './api'

export const doctorAPI = {

  createDoctor: (data) => api.post('/doctors', data),

  getDoctorById: (id) => api.get(`/doctors/${id}`),

  getDoctorByUserId: (userId) => api.get(`/doctors/user/${userId}`),

  getAllDoctors: (params) => {
    const { page = 0, size = 10, sortBy = 'rating', sortDir = 'desc' } = params || {}
    return api.get('/doctors', {
      params: { page, size, sortBy, sortDir }
    })
  },

  searchDoctors: (searchDTO, params) => {
    const { page = 0, size = 10, sortBy = 'rating', sortDir = 'desc' } = params || {}
    return api.post('/doctors/search', searchDTO, {
      params: { page, size, sortBy, sortDir }
    })
  },

  updateDoctor: (id, data) => api.put(`/doctors/${id}`, data),

  deleteDoctor: (id) => api.delete(`/doctors/${id}`),

  countDoctors: () => api.get('/doctors/count')
}

