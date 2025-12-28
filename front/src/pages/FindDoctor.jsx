import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { matchAPI } from '../services/matchApi'
import { appointmentAPI } from '../services/appointmentApi'
import AppointmentModal from '../components/appointments/AppointmentModal'
import './FindDoctor.css'

const SPECIALITES = [
  { value: '', label: 'Toutes les spécialités' },
  { value: 'CARDIOLOGIE', label: 'Cardiologie' },
  { value: 'DERMATOLOGIE', label: 'Dermatologie' },
  { value: 'ENDOCRINOLOGIE', label: 'Endocrinologie' },
  { value: 'GASTROENTEROLOGIE', label: 'Gastro-entérologie' },
  { value: 'GYNECOLOGIE', label: 'Gynécologie' },
  { value: 'MEDECINE_GENERALE', label: 'Médecine générale' },
  { value: 'NEUROLOGIE', label: 'Neurologie' },
  { value: 'ONCOLOGIE', label: 'Oncologie' },
  { value: 'OPHTALMOLOGIE', label: 'Ophtalmologie' },
  { value: 'ORTHOPEDIE', label: 'Orthopédie' },
  { value: 'PEDIATRIE', label: 'Pédiatrie' },
  { value: 'PSYCHIATRIE', label: 'Psychiatrie' },
  { value: 'PNEUMOLOGIE', label: 'Pneumologie' },
  { value: 'RHUMATOLOGIE', label: 'Rhumatologie' },
  { value: 'UROLOGIE', label: 'Urologie' }
]

const FindDoctor = () => {
  const navigate = useNavigate()
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [doctors, setDoctors] = useState([])
  
  // Form fields
  const [symptomes, setSymptomes] = useState('')
  const [specialite, setSpecialite] = useState('')
  const [rayonKm, setRayonKm] = useState(10)
  const [dateSouhaitee, setDateSouhaitee] = useState('')
  const [heureSouhaitee, setHeureSouhaitee] = useState('')
  
  // Geolocation
  const [latitude, setLatitude] = useState(null)
  const [longitude, setLongitude] = useState(null)
  const [locationError, setLocationError] = useState('')
  const [locationLoading, setLocationLoading] = useState(false)
  
  // Modal
  const [showAppointmentModal, setShowAppointmentModal] = useState(false)
  const [selectedDoctor, setSelectedDoctor] = useState(null)
  
  useEffect(() => {
    // Essayer de récupérer la position depuis le localStorage ou demander la géolocalisation
    const savedLat = localStorage.getItem('userLatitude')
    const savedLng = localStorage.getItem('userLongitude')
    
    if (savedLat && savedLng) {
      setLatitude(parseFloat(savedLat))
      setLongitude(parseFloat(savedLng))
    } else {
      requestGeolocation()
    }
  }, [])
  
  const requestGeolocation = () => {
    setLocationLoading(true)
    setLocationError('')
    
    if (!navigator.geolocation) {
      setLocationError('La géolocalisation n\'est pas supportée par votre navigateur')
      setLocationLoading(false)
      return
    }
    
    navigator.geolocation.getCurrentPosition(
      (position) => {
        const lat = position.coords.latitude
        const lng = position.coords.longitude
        setLatitude(lat)
        setLongitude(lng)
        // Sauvegarder pour les prochaines fois
        localStorage.setItem('userLatitude', lat.toString())
        localStorage.setItem('userLongitude', lng.toString())
        setLocationLoading(false)
      },
      (err) => {
        console.error('Erreur géolocalisation:', err)
        setLocationError('Impossible de récupérer votre position. Veuillez entrer manuellement les coordonnées.')
        setLocationLoading(false)
        // Coordonnées par défaut (Tunis)
        setLatitude(36.8065)
        setLongitude(10.1815)
      }
    )
  }
  
  const handleManualLocation = () => {
    const lat = prompt('Entrez votre latitude:')
    const lng = prompt('Entrez votre longitude:')
    
    if (lat && lng) {
      const latNum = parseFloat(lat)
      const lngNum = parseFloat(lng)
      if (!isNaN(latNum) && !isNaN(lngNum)) {
        setLatitude(latNum)
        setLongitude(lngNum)
        localStorage.setItem('userLatitude', latNum.toString())
        localStorage.setItem('userLongitude', lngNum.toString())
        setLocationError('')
      } else {
        alert('Coordonnées invalides')
      }
    }
  }
  
  const handleSearch = async (e) => {
    e.preventDefault()
    
    if (!symptomes.trim()) {
      setError('Veuillez entrer au moins vos symptômes')
      return
    }
    
    if (!latitude || !longitude) {
      setError('Veuillez activer la géolocalisation ou entrer manuellement votre position')
      return
    }
    
    setLoading(true)
    setError('')
    
    try {
      const matchRequest = {
        symptomes: symptomes.trim(),
        latitude: latitude,
        longitude: longitude,
        rayonKm: rayonKm,
        limit: 20
      }
      
      if (specialite) {
        matchRequest.specialite = specialite
      }
      
      if (dateSouhaitee) {
        matchRequest.dateSouhaitee = dateSouhaitee
      }
      
      const response = await matchAPI.matchDoctors(matchRequest)
      setDoctors(response.data || [])
      
      if (response.data.length === 0) {
        setError('Aucun docteur trouvé avec ces critères. Essayez d\'élargir le rayon de recherche.')
      }
    } catch (err) {
      console.error('Erreur recherche:', err)
      setError(err.response?.data?.message || 'Erreur lors de la recherche de docteurs')
      setDoctors([])
    } finally {
      setLoading(false)
    }
  }
  
  const handleTakeAppointment = (doctor) => {
    setSelectedDoctor(doctor)
    setShowAppointmentModal(true)
  }
  
  const handleModalClose = () => {
    setShowAppointmentModal(false)
    setSelectedDoctor(null)
  }
  
  const handleAppointmentSuccess = () => {
    handleModalClose()
    navigate('/my-appointments')
  }
  
  const formatScore = (score) => {
    return (score * 100).toFixed(0)
  }
  
  const getScoreColor = (score) => {
    if (score >= 0.8) return '#43A047' // Vert
    if (score >= 0.6) return '#FB8C00' // Orange
    return '#E53935' // Rouge
  }
  
  return (
    <div className="find-doctor-container">
      <div className="find-doctor-header">
        <h1>🔍 Trouver un Docteur</h1>
        <p className="subtitle">Trouvez le médecin le plus adapté à vos besoins</p>
      </div>
      
      <form onSubmit={handleSearch} className="search-form">
        <div className="form-section">
          <h3>📝 Décrivez vos symptômes</h3>
          <textarea
            value={symptomes}
            onChange={(e) => setSymptomes(e.target.value)}
            placeholder="Ex: douleur thoracique, essoufflement, maux de tête..."
            rows={4}
            className="symptomes-input"
            required
          />
        </div>
        
        <div className="form-row">
          <div className="form-group">
            <label>
              <span className="label-icon">📍</span>
              Spécialité (optionnel)
            </label>
            <select
              value={specialite}
              onChange={(e) => setSpecialite(e.target.value)}
              className="specialite-select"
            >
              {SPECIALITES.map(spec => (
                <option key={spec.value} value={spec.value}>{spec.label}</option>
              ))}
            </select>
          </div>
          
          <div className="form-group">
            <label>
              <span className="label-icon">📏</span>
              Rayon de recherche (km)
            </label>
            <input
              type="number"
              value={rayonKm}
              onChange={(e) => setRayonKm(parseFloat(e.target.value) || 10)}
              min={1}
              max={100}
              className="rayon-input"
            />
          </div>
        </div>
        
        <div className="form-section location-section">
          <h3>📍 Position</h3>
          <div className="location-info">
            {locationLoading ? (
              <div className="location-loading">Récupération de votre position...</div>
            ) : latitude && longitude ? (
              <div className="location-success">
                <span>✅ Position: {latitude.toFixed(4)}, {longitude.toFixed(4)}</span>
                <button type="button" onClick={requestGeolocation} className="btn-refresh-location">
                  🔄 Actualiser
                </button>
              </div>
            ) : (
              <div className="location-error">
                <span>⚠️ Position non disponible</span>
                <button type="button" onClick={requestGeolocation} className="btn-request-location">
                  📍 Activer la géolocalisation
                </button>
                <button type="button" onClick={handleManualLocation} className="btn-manual-location">
                  ✏️ Entrer manuellement
                </button>
              </div>
            )}
            {locationError && (
              <div className="location-error-message">{locationError}</div>
            )}
          </div>
        </div>
        
        <div className="form-row">
          <div className="form-group">
            <label>
              <span className="label-icon">📅</span>
              Date souhaitée (optionnel)
            </label>
            <input
              type="date"
              value={dateSouhaitee}
              onChange={(e) => setDateSouhaitee(e.target.value)}
              min={new Date().toISOString().split('T')[0]}
              className="date-input"
            />
          </div>
          
          <div className="form-group">
            <label>
              <span className="label-icon">⏰</span>
              Heure souhaitée (optionnel)
            </label>
            <input
              type="time"
              value={heureSouhaitee}
              onChange={(e) => setHeureSouhaitee(e.target.value)}
              className="time-input"
            />
          </div>
        </div>
        
        <button type="submit" disabled={loading || !latitude || !longitude} className="btn-search">
          {loading ? 'Recherche en cours...' : '🔍 Rechercher'}
        </button>
      </form>
      
      {error && (
        <div className="error-alert">
          {error}
          <button onClick={() => setError('')} className="close-error">×</button>
        </div>
      )}
      
      {doctors.length > 0 && (
        <div className="results-section">
          <h2>Résultats ({doctors.length} docteur{doctors.length > 1 ? 's' : ''})</h2>
          <div className="doctors-list">
            {doctors.map((match) => {
              const doctor = match.doctor
              return (
                <div key={doctor.id} className="doctor-card">
                  <div className="doctor-card-header">
                    <div className="doctor-info-main">
                      <h3>{doctor.nomComplet}</h3>
                      <div className="doctor-specialite">
                        {doctor.specialite?.replace(/_/g, ' ')}
                      </div>
                    </div>
                    <div className="score-badge" style={{ backgroundColor: getScoreColor(match.scoreTotal) }}>
                      <div className="score-value">{formatScore(match.scoreTotal)}%</div>
                      <div className="score-label">Pertinence</div>
                    </div>
                  </div>
                  
                  <div className="doctor-details">
                    <div className="detail-row">
                      <span className="detail-icon">📍</span>
                      <span>{doctor.adresse || 'Adresse non renseignée'}</span>
                      {match.distanceKm && (
                        <span className="distance-badge">{match.distanceKm.toFixed(1)} km</span>
                      )}
                    </div>
                    
                    {doctor.nomClinique && (
                      <div className="detail-row">
                        <span className="detail-icon">🏥</span>
                        <span>{doctor.nomClinique}</span>
                      </div>
                    )}
                    
                    <div className="detail-row">
                      <span className="detail-icon">⭐</span>
                      <span>Note: {doctor.rating?.toFixed(1) || '0.0'}/5 ({doctor.nombreAvis || 0} avis)</span>
                    </div>
                    
                    {doctor.teleconsultation && (
                      <div className="detail-row">
                        <span className="detail-icon">💻</span>
                        <span className="teleconsultation-badge">Téléconsultation disponible</span>
                      </div>
                    )}
                    
                    <div className="detail-row">
                      <span className="detail-icon">💰</span>
                      <span>{doctor.tarifConsultation ? `${doctor.tarifConsultation} €` : 'Tarif non renseigné'}</span>
                    </div>
                    
                    {match.disponible !== undefined && (
                      <div className="detail-row">
                        <span className="detail-icon">{match.disponible ? '✅' : '❌'}</span>
                        <span>{match.disponible ? 'Disponible' : 'Non disponible'} à la date souhaitée</span>
                      </div>
                    )}
                  </div>
                  
                  <div className="score-breakdown">
                    <div className="score-item">
                      <span className="score-label">Symptômes:</span>
                      <span className="score-value-small">{formatScore(match.scoreSymptomes)}%</span>
                    </div>
                    <div className="score-item">
                      <span className="score-label">Distance:</span>
                      <span className="score-value-small">{formatScore(match.scoreDistance)}%</span>
                    </div>
                    <div className="score-item">
                      <span className="score-label">Disponibilité:</span>
                      <span className="score-value-small">{formatScore(match.scoreDisponibilite)}%</span>
                    </div>
                  </div>
                  
                  {match.message && (
                    <div className="match-message">{match.message}</div>
                  )}
                  
                  <button
                    onClick={() => handleTakeAppointment(doctor)}
                    className="btn-take-appointment"
                  >
                    📅 Prendre rendez-vous
                  </button>
                </div>
              )
            })}
          </div>
        </div>
      )}
      
      {showAppointmentModal && selectedDoctor && (
        <AppointmentModal
          mode="add"
          onClose={handleModalClose}
          onSave={handleAppointmentSuccess}
          defaultDoctorId={selectedDoctor.id}
          defaultDate={dateSouhaitee}
          defaultHeure={heureSouhaitee}
        />
      )}
    </div>
  )
}

export default FindDoctor

