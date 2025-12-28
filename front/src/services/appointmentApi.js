import api from './api'

export const appointmentAPI = {
  // Créer un rendez-vous
  createAppointment: (data) => api.post('/appointments', data),
  
  // Récupérer un rendez-vous par ID
  getAppointmentById: (id) => api.get(`/appointments/${id}`),
  
  // RDV d'un patient (paginé)
  getPatientAppointments: (patientId, params) => {
    const { page = 0, size = 10 } = params || {}
    return api.get(`/appointments/patient/${patientId}`, {
      params: { page, size }
    })
  },
  
  // RDV d'un patient (liste complète)
  getPatientAppointmentsList: (patientId) => api.get(`/appointments/patient/${patientId}/all`),
  
  // RDV d'un docteur (paginé)
  getDoctorAppointments: (doctorId, params) => {
    const { page = 0, size = 10 } = params || {}
    return api.get(`/appointments/doctor/${doctorId}`, {
      params: { page, size }
    })
  },
  
  // RDV d'un docteur (liste complète)
  getDoctorAppointmentsList: (doctorId) => api.get(`/appointments/doctor/${doctorId}/all`),
  
  // RDV d'un docteur pour une date
  getDoctorAppointmentsByDate: (doctorId, date) => {
    return api.get(`/appointments/doctor/${doctorId}/date`, {
      params: { date }
    })
  },
  
  // Calendrier par semaine
  getDoctorAppointmentsByWeek: (doctorId, weekStart) => {
    return api.get(`/appointments/doctor/${doctorId}/week`, {
      params: { weekStart }
    })
  },
  
  // Vérifier disponibilité
  checkAvailability: (doctorId, date, heure) => {
    return api.get('/appointments/check-availability', {
      params: { doctorId, date, heure }
    })
  },
  
  // Confirmer un rendez-vous
  confirmAppointment: (id) => api.put(`/appointments/${id}/confirm`),
  
  // Annuler un rendez-vous
  cancelAppointment: (id) => api.put(`/appointments/${id}/cancel`),
  
  // Marquer comme terminé
  completeAppointment: (id) => api.put(`/appointments/${id}/complete`),
  
  // Marquer comme absent
  markNoShow: (id) => api.put(`/appointments/${id}/no-show`),
  
  // Changer le statut
  updateAppointmentStatus: (id, status) => api.put(`/appointments/${id}/status`, null, {
    params: { status }
  }),
  
  // Reprogrammer
  rescheduleAppointment: (id, newDate, newHeure) => {
    return api.put(`/appointments/${id}/reschedule`, null, {
      params: { newDate, newHeure }
    })
  },
  
  // Modifier un rendez-vous
  updateAppointment: (id, data) => api.put(`/appointments/${id}`, data),
  
  // Supprimer un rendez-vous
  deleteAppointment: (id) => api.delete(`/appointments/${id}`),
  
  // Compter les RDV
  countDoctorAppointments: (doctorId) => api.get(`/appointments/doctor/${doctorId}/count`),
  countPatientAppointments: (patientId) => api.get(`/appointments/patient/${patientId}/count`)
}

