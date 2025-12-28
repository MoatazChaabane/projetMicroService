import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { doctorAPI } from '../services/doctorApi'
import { useAuth } from '../context/AuthContext'
import './DoctorDetail.css'

const DoctorDetail = () => {
  const { id } = useParams()
  const navigate = useNavigate()
  const { isAuthenticated, user } = useAuth()
  const [doctor, setDoctor] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    if (isAuthenticated && id) {
      fetchDoctor()
    }
  }, [id, isAuthenticated])

  const fetchDoctor = async () => {
    setLoading(true)
    setError('')
    try {
      const response = await doctorAPI.getDoctorById(id)
      setDoctor(response.data)
    } catch (err) {
      console.error('Error fetching doctor:', err)
      setError(err.response?.data?.message || 'Erreur lors du chargement du docteur')
    } finally {
      setLoading(false)
    }
  }

  const formatSpecialite = (specialite) => {
    if (!specialite) return '-'
    return specialite.replace('_', ' ')
  }

  const formatRating = (rating) => {
    if (!rating && rating !== 0) return 'N/A'
    return `${parseFloat(rating).toFixed(1)} ⭐`
  }

  const formatPrice = (price) => {
    if (!price) return '-'
    return `${parseFloat(price).toFixed(2)} €`
  }

  const formatDate = (dateString) => {
    if (!dateString) return '-'
    const date = new Date(dateString)
    return date.toLocaleDateString('fr-FR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  const formatTime = (timeString) => {
    if (!timeString) return '-'
    return timeString.substring(0, 5) // Format HH:mm
  }

  if (!isAuthenticated) {
    return (
      <div className="doctor-detail-container">
        <div className="error-message">Veuillez vous connecter pour accéder à cette page.</div>
      </div>
    )
  }

  if (loading) {
    return (
      <div className="doctor-detail-container">
        <div className="loading-container">
          <div className="spinner">Chargement...</div>
        </div>
      </div>
    )
  }

  if (error || !doctor) {
    return (
      <div className="doctor-detail-container">
        <div className="error-message">
          {error || 'Docteur non trouvé'}
        </div>
        <button onClick={() => navigate('/doctors')} className="btn-back">
          Retour à la liste
        </button>
      </div>
    )
  }

  return (
    <div className="doctor-detail-container">
      <div className="detail-header">
        <button onClick={() => navigate('/doctors')} className="btn-back">
          ← Retour
        </button>
        <h1>Détails du Docteur</h1>
      </div>

      <div className="doctor-detail-card">
        <div className="doctor-header-info">
          <div className="doctor-main-info">
            <h2>{doctor.nomComplet || 'Nom non disponible'}</h2>
            <div className="doctor-badges">
              <span className="badge badge-specialite">
                {formatSpecialite(doctor.specialite)}
              </span>
              {doctor.teleconsultation && (
                <span className="badge badge-success">Téléconsultation disponible</span>
              )}
            </div>
          </div>
          <div className="doctor-rating">
            <div className="rating-value">{formatRating(doctor.rating)}</div>
            {doctor.nombreAvis > 0 && (
              <div className="rating-count">{doctor.nombreAvis} avis</div>
            )}
          </div>
        </div>

        <div className="detail-sections">
          <div className="detail-section">
            <h3>📧 Contact</h3>
            <div className="detail-grid">
              <div className="detail-item">
                <label>Email</label>
                <div>{doctor.email || '-'}</div>
              </div>
              <div className="detail-item">
                <label>Téléphone</label>
                <div>{doctor.telephone || '-'}</div>
              </div>
            </div>
          </div>

          <div className="detail-section">
            <h3>📍 Localisation</h3>
            <div className="detail-grid">
              <div className="detail-item">
                <label>Clinique</label>
                <div>{doctor.nomClinique || '-'}</div>
              </div>
              <div className="detail-item">
                <label>Adresse</label>
                <div>{doctor.adresse || '-'}</div>
              </div>
              {doctor.latitude && doctor.longitude && (
                <>
                  <div className="detail-item">
                    <label>Latitude</label>
                    <div>{doctor.latitude}</div>
                  </div>
                  <div className="detail-item">
                    <label>Longitude</label>
                    <div>{doctor.longitude}</div>
                  </div>
                </>
              )}
            </div>
          </div>

          <div className="detail-section">
            <h3>💰 Tarifs</h3>
            <div className="detail-grid">
              <div className="detail-item">
                <label>Tarif de consultation</label>
                <div className="price-value">{formatPrice(doctor.tarifConsultation)}</div>
              </div>
            </div>
          </div>

          {doctor.langues && doctor.langues.length > 0 && (
            <div className="detail-section">
              <h3>🗣️ Langues parlées</h3>
              <div className="langues-list">
                {doctor.langues.map((langue, index) => (
                  <span key={index} className="langue-badge">{langue}</span>
                ))}
              </div>
            </div>
          )}

          {doctor.horaires && doctor.horaires.length > 0 && (
            <div className="detail-section">
              <h3>🕐 Horaires de disponibilité</h3>
              <div className="horaires-list">
                {doctor.horaires.map((horaire, index) => (
                  <div key={index} className="horaire-item">
                    <div className="horaire-jour">{horaire.jour}</div>
                    <div className="horaire-time">
                      {formatTime(horaire.heureDebut)} - {formatTime(horaire.heureFin)}
                    </div>
                    <div className="horaire-status">
                      {horaire.disponible !== false ? (
                        <span className="badge badge-success">Disponible</span>
                      ) : (
                        <span className="badge badge-secondary">Indisponible</span>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          <div className="detail-section">
            <h3>📅 Informations système</h3>
            <div className="detail-grid">
              <div className="detail-item">
                <label>Date de création</label>
                <div>{formatDate(doctor.createdAt)}</div>
              </div>
              <div className="detail-item">
                <label>Dernière mise à jour</label>
                <div>{formatDate(doctor.updatedAt)}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

export default DoctorDetail

