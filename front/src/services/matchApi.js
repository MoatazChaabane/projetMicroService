import api from './api'

export const matchAPI = {

  matchDoctors: (matchRequest) => {
    return api.post('/match/doctors', matchRequest)
  }
}

