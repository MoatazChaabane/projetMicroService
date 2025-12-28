import { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'
import { doctorAPI } from '../services/doctorApi'
import { patientAPI } from '../services/patientApi'
import { Link } from 'react-router-dom'
import './Dashboard.css'

const Dashboard = () => {
  const { user } = useAuth()
  const [stats, setStats] = useState({
    doctors: 0,
    patients: 0,
    loading: true
  })

  useEffect(() => {
    fetchStats()
  }, [])

  const fetchStats = async () => {
    try {
      const [doctorsRes, patientsRes] = await Promise.allSettled([
        doctorAPI.countDoctors(),
        patientAPI.countPatients()
      ])
      
      setStats({
        doctors: doctorsRes.status === 'fulfilled' ? (doctorsRes.value.data || 0) : 0,
        patients: patientsRes.status === 'fulfilled' ? (patientsRes.value.data || 0) : 0,
        loading: false
      })
    } catch (error) {
      console.error('Error fetching stats:', error)
      setStats({ doctors: 0, patients: 0, loading: false })
    }
  }

  const userRole = user?.role || ''
  const userName = `${user?.firstName || ''} ${user?.lastName || ''}`.trim()

  return (
    <div className="dashboard-container">
      <div className="dashboard-header">
        <h1>Bienvenue, {userName || 'Utilisateur'} 👋</h1>
        <p className="dashboard-subtitle">Tableau de bord de votre système médical</p>
      </div>

      <div className="dashboard-stats">
        <div className="stat-card stat-card-blue">
          <div className="stat-icon">👨‍⚕️</div>
          <div className="stat-content">
            <h3>{stats.loading ? '...' : stats.doctors}</h3>
            <p>Docteurs</p>
          </div>
          <Link to="/doctors" className="stat-link">
            Voir tous →
          </Link>
        </div>

        {(userRole === 'ADMIN' || userRole === 'DOCTOR') && (
          <div className="stat-card stat-card-green">
            <div className="stat-icon">👥</div>
            <div className="stat-content">
              <h3>{stats.loading ? '...' : stats.patients}</h3>
              <p>Patients</p>
            </div>
            <Link to="/patients" className="stat-link">
              Voir tous →
            </Link>
          </div>
        )}

        <div className="stat-card stat-card-purple">
          <div className="stat-icon">📊</div>
          <div className="stat-content">
            <h3>100%</h3>
            <p>Disponibilité</p>
          </div>
          <div className="stat-link">Système opérationnel</div>
        </div>

        <div className="stat-card stat-card-orange">
          <div className="stat-icon">⚡</div>
          <div className="stat-content">
            <h3>24/7</h3>
            <p>Support</p>
          </div>
          <div className="stat-link">Toujours disponible</div>
        </div>
      </div>

      <div className="dashboard-actions">
        <h2>Actions rapides</h2>
        <div className="action-grid">
          <Link to="/doctors" className="action-card">
            <div className="action-icon">🔍</div>
            <h3>Rechercher un docteur</h3>
            <p>Trouvez un médecin par spécialité, localisation ou disponibilité</p>
          </Link>

          {(userRole === 'ADMIN' || userRole === 'DOCTOR') && (
            <Link to="/patients" className="action-card">
              <div className="action-icon">➕</div>
              <h3>Ajouter un patient</h3>
              <p>Enregistrez un nouveau patient dans le système</p>
            </Link>
          )}

          {userRole === 'ADMIN' && (
            <Link to="/doctors" className="action-card">
              <div className="action-icon">👨‍⚕️</div>
              <h3>Gérer les docteurs</h3>
              <p>Créer, modifier ou supprimer des profils de médecins</p>
            </Link>
          )}

          <Link to="/profile" className="action-card">
            <div className="action-icon">👤</div>
            <h3>Mon profil</h3>
            <p>Consultez et modifiez vos informations personnelles</p>
          </Link>
        </div>
      </div>

      <div className="dashboard-info">
        <div className="info-card">
          <h3>📋 À propos du système</h3>
          <p>
            MediCare est un système complet de gestion médicale qui permet de gérer
            les docteurs, les patients et leurs rendez-vous de manière efficace et sécurisée.
          </p>
        </div>
        <div className="info-card">
          <h3>🔒 Sécurité</h3>
          <p>
            Toutes vos données sont protégées et chiffrées. Le système respecte
            les normes de confidentialité médicale.
          </p>
        </div>
      </div>
    </div>
  )
}

export default Dashboard

