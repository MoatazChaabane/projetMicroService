import { useState, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { profileAPI } from '../services/api'
import './Profile.css'

const Profile = () => {
  const { user, updateUser } = useAuth()
  const navigate = useNavigate()
  const [loading, setLoading] = useState(true)
  const [profile, setProfile] = useState(null)

  useEffect(() => {
    fetchProfile()
  }, [])

  const fetchProfile = async () => {
    try {
      const response = await profileAPI.getProfile()
      setProfile(response.data)
      updateUser(response.data)
    } catch (error) {
      console.error('Error fetching profile:', error)
      if (error.response?.status === 403 || error.response?.status === 401) {
        // Rediriger vers login si non authentifié
        navigate('/login')
      }
    } finally {
      setLoading(false)
    }
  }

  if (loading) {
    return (
      <div className="profile-container">
        <div className="loading">Chargement...</div>
      </div>
    )
  }

  return (
    <div className="profile-container">
      <div className="profile-header">
        <h1>Mon Profil</h1>
      </div>

      <div className="profile-card">
        {profile?.photoUrl && (
          <div className="profile-photo">
            <img
              src={`http://localhost:8081${profile.photoUrl}`}
              alt="Photo de profil"
            />
          </div>
        )}

        <div className="profile-info">
          <div className="info-row">
            <span className="label">Email:</span>
            <span className="value">{profile?.email}</span>
          </div>
          <div className="info-row">
            <span className="label">Prénom:</span>
            <span className="value">{profile?.firstName}</span>
          </div>
          <div className="info-row">
            <span className="label">Nom:</span>
            <span className="value">{profile?.lastName}</span>
          </div>
          <div className="info-row">
            <span className="label">Téléphone:</span>
            <span className="value">{profile?.phoneNumber || 'Non renseigné'}</span>
          </div>
          <div className="info-row">
            <span className="label">Rôle:</span>
            <span className="value badge">{profile?.role}</span>
          </div>
          {profile?.createdAt && (
            <div className="info-row">
              <span className="label">Membre depuis:</span>
              <span className="value">
                {new Date(profile.createdAt).toLocaleDateString('fr-FR')}
              </span>
            </div>
          )}
        </div>

        <div className="profile-actions">
          <Link to="/profile/edit" className="btn-primary">
            Modifier le profil
          </Link>
          <Link to="/profile/change-password" className="btn-secondary">
            Changer le mot de passe
          </Link>
        </div>
      </div>
    </div>
  )
}

export default Profile

