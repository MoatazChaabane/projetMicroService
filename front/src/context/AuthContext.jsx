import { createContext, useContext, useState, useEffect } from 'react'
import { authAPI } from '../services/api'

const AuthContext = createContext(null)

export const useAuth = () => {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)
  const [isAuthenticated, setIsAuthenticated] = useState(false)

  useEffect(() => {
    checkAuth()
  }, [])

  const checkAuth = async () => {
    try {
      const token = localStorage.getItem('token')
      const storedUser = localStorage.getItem('user')
      
      if (token) {
        // D'abord, utiliser les données stockées pour un affichage immédiat
        if (storedUser) {
          try {
            const parsedUser = JSON.parse(storedUser)
            setUser(parsedUser)
            setIsAuthenticated(true)
          } catch (e) {
            console.error('Error parsing stored user:', e)
          }
        }
        
        // Ensuite, essayer de valider le token avec l'API en arrière-plan
        try {
          const response = await authAPI.getCurrentUser()
          setUser(response.data)
          setIsAuthenticated(true)
          // Mettre à jour le localStorage avec les données fraîches
          localStorage.setItem('user', JSON.stringify(response.data))
        } catch (error) {
          // Si le token est invalide (401/403), nettoyer seulement si on n'a pas de données stockées
          if (!storedUser) {
            setUser(null)
            setIsAuthenticated(false)
            localStorage.removeItem('token')
            localStorage.removeItem('user')
          }
          // Sinon, on garde les données stockées pour permettre la navigation
          // L'utilisateur sera déconnecté lors de la prochaine action nécessitant l'API
        }
      } else {
        setUser(null)
        setIsAuthenticated(false)
        localStorage.removeItem('user')
      }
    } catch (error) {
      console.error('Auth check failed:', error)
      // En cas d'erreur, garder les données stockées si elles existent
      const storedUser = localStorage.getItem('user')
      if (!storedUser) {
        setUser(null)
        setIsAuthenticated(false)
        localStorage.removeItem('token')
        localStorage.removeItem('user')
      }
    } finally {
      setLoading(false)
    }
  }

  const login = async (email, password) => {
    try {
      const response = await authAPI.login({ email, password })
      
      // Si la réponse contient un token et un utilisateur
      if (response.data && response.data.token && response.data.user) {
        // Stocker le token dans localStorage
        localStorage.setItem('token', response.data.token)
        localStorage.setItem('user', JSON.stringify(response.data.user))
        
        // Mettre à jour l'état immédiatement
        setUser(response.data.user)
        setIsAuthenticated(true)
        
        return { success: true, data: response.data }
      }
      
      // Si pas de token, quelque chose ne va pas
      return {
        success: false,
        error: 'Connexion réussie mais aucun token reçu',
      }
    } catch (error) {
      console.error('Login error:', error)
      setUser(null)
      setIsAuthenticated(false)
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      return {
        success: false,
        error: error.response?.data?.message || error.message || 'Erreur de connexion',
      }
    }
  }

  const register = async (userData) => {
    try {
      const response = await authAPI.register(userData)
      return { success: true, data: response.data }
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Erreur d\'inscription',
      }
    }
  }

  const logout = async () => {
    try {
      await authAPI.logout()
    } catch (error) {
      console.error('Logout error:', error)
    } finally {
      setUser(null)
      setIsAuthenticated(false)
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
  }

  const updateUser = (userData) => {
    setUser(userData)
  }

  const value = {
    user,
    loading,
    isAuthenticated,
    login,
    register,
    logout,
    updateUser,
    checkAuth,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

