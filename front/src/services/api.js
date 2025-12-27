import axios from 'axios'

const API_BASE_URL = 'http://localhost:8081/api'

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Intercepteur pour ajouter le token JWT dans les requêtes
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Intercepteur pour gérer les erreurs
api.interceptors.response.use(
  (response) => {
    return response
  },
  (error) => {
    // Ne pas rediriger si on est déjà sur la page de login ou register
    const currentPath = window.location.pathname
    if (currentPath === '/login' || currentPath === '/register') {
      return Promise.reject(error)
    }
    
    if (error.response?.status === 401 || error.response?.status === 403) {
      // Déconnexion automatique en cas d'erreur 401 ou 403
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      // Utiliser navigate au lieu de window.location pour éviter les rechargements
      if (currentPath !== '/login') {
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

export const authAPI = {
  register: (data) => api.post('/auth/register', data),
  login: (data) => api.post('/auth/login', data),
  logout: () => api.post('/auth/logout'),
  getCurrentUser: () => api.get('/auth/me'),
}

export const profileAPI = {
  getProfile: () => api.get('/profile'),
  updateProfile: (data) => api.put('/profile', data),
  changePassword: (data) => api.put('/profile/password', data),
  uploadPhoto: (file) => {
    const formData = new FormData()
    formData.append('file', file)
    return api.post('/profile/photo', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })
  },
  deletePhoto: () => api.delete('/profile/photo'),
}

export default api

