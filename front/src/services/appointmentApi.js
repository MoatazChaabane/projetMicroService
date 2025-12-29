import api from './api'

export const appointmentAPI = {
  createAppointment: (data) => api.post('/appointments', data),
  
  getAppointmentById: (id) => api.get(`/appointments/${id}`),
  
  getPatientAppointments: (patientId, params) => {
    const { page = 0, size = 10 } = params || {}
    return api.get(`/appointments/patient/${patientId}`, {
      params: { page, size }
    })
  },
  
  getPatientAppointmentsList: (patientId) => api.get(`/appointments/patient/${patientId}/all`),
  
  getDoctorAppointments: (doctorId, params) => {
    const { page = 0, size = 10 } = params || {}
    return api.get(`/appointments/doctor/${doctorId}`, {
      params: { page, size }
    })
  },
  
  getDoctorAppointmentsList: (doctorId) => api.get(`/appointments/doctor/${doctorId}/all`),
  
  getDoctorAppointmentsByDate: (doctorId, date) => {
    return api.get(`/appointments/doctor/${doctorId}/date`, {
      params: { date }
    })
  },
  
  getDoctorAppointmentsByWeek: (doctorId, weekStart) => {
    return api.get(`/appointments/doctor/${doctorId}/week`, {
      params: { weekStart }
    })
  },
  
  checkAvailability: (doctorId, date, heure) => {
    return api.get('/appointments/check-availability', {
      params: { doctorId, date, heure }
    })
  },

  confirmAppointment: (id) => api.put(`/appointments/${id}/confirm`),

  cancelAppointment: (id) => api.put(`/appointments/${id}/cancel`),
  
  completeAppointment: (id) => api.put(`/appointments/${id}/complete`),
  
  markNoShow: (id) => api.put(`/appointments/${id}/no-show`),
  
  updateAppointmentStatus: (id, status) => api.put(`/appointments/${id}/status`, null, {
    params: { status }
  }),
  
  rescheduleAppointment: (id, newDate, newHeure) => {
    return api.put(`/appointments/${id}/reschedule`, null, {
      params: { newDate, newHeure }
    })
  },
  
  updateAppointment: (id, data) => api.put(`/appointments/${id}`, data),
  
  deleteAppointment: (id) => api.delete(`/appointments/${id}`),
  
  countDoctorAppointments: (doctorId) => api.get(`/appointments/doctor/${doctorId}/count`),
  countPatientAppointments: (patientId) => api.get(`/appointments/patient/${patientId}/count`)
}

