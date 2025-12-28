import api from './api'

export const matchAPI = {
  // Trouver des docteurs par matching intelligent
  matchDoctors: (matchRequest) => {
    return api.post('/match/doctors', matchRequest)
  }
}

