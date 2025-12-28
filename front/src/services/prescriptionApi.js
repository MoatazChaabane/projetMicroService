import api from './api'

export const prescriptionAPI = {
  // Créer une ordonnance
  createPrescription: (data) => {
    return api.post('/prescriptions', data)
  },
  
  // Récupérer une ordonnance par ID
  getPrescriptionById: (id) => {
    return api.get(`/prescriptions/${id}`)
  },
  
  // Récupérer les ordonnances d'un patient (paginé)
  getPatientPrescriptions: (patientId, page = 0, size = 10) => {
    return api.get(`/prescriptions/patient/${patientId}`, {
      params: { page, size }
    })
  },
  
  // Récupérer les ordonnances d'un docteur (paginé)
  getDoctorPrescriptions: (doctorId, page = 0, size = 10) => {
    return api.get(`/prescriptions/doctor/${doctorId}`, {
      params: { page, size }
    })
  },
  
  // Récupérer toutes les ordonnances (admin)
  getAllPrescriptions: () => {
    return api.get('/prescriptions/all')
  },
  
  // Télécharger le PDF d'une ordonnance
  downloadPDF: (pdfUrl) => {
    // Si pdfUrl est relatif, on le préfixe avec l'URL de base
    const url = pdfUrl.startsWith('http') ? pdfUrl : `${process.env.REACT_APP_API_URL || 'http://localhost:8081'}${pdfUrl}`
    return api.get(url, {
      responseType: 'blob'
    })
  }
}

