import api from './api'

export const prescriptionAPI = {
  createPrescription: (data) => {
    return api.post('/prescriptions', data)
  },
  
  getPrescriptionById: (id) => {
    return api.get(`/prescriptions/${id}`)
  },
  
  getPatientPrescriptions: (patientId, page = 0, size = 10) => {
    return api.get(`/prescriptions/patient/${patientId}`, {
      params: { page, size }
    })
  },
  
  getDoctorPrescriptions: (doctorId, page = 0, size = 10) => {
    return api.get(`/prescriptions/doctor/${doctorId}`, {
      params: { page, size }
    })
  },
  
  getAllPrescriptions: () => {
    return api.get('/prescriptions/all')
  },
  
  downloadPDF: (pdfUrl) => {
    const baseUrl = 'http://localhost:8081'
    const url = pdfUrl.startsWith('http') ? pdfUrl : `${baseUrl}${pdfUrl}`
    
    const token = localStorage.getItem('token')
    return fetch(url, {
      headers: {
        'Authorization': token ? `Bearer ${token}` : ''
      }
    }).then(response => {
      if (!response.ok) {
        throw new Error(`Erreur ${response.status}: ${response.statusText}`)
      }
      return response.blob()
    })
  }
}

