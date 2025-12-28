import { useState } from 'react'
import './DoctorFilters.css'

const SPECIALITES = [
  'CARDIOLOGIE',
  'DERMATOLOGIE',
  'ENDOCRINOLOGIE',
  'GASTROENTEROLOGIE',
  'GYNECOLOGIE',
  'MEDECINE_GENERALE',
  'NEUROLOGIE',
  'ONCOLOGIE',
  'OPHTALMOLOGIE',
  'ORTHOPEDIE',
  'PEDIATRIE',
  'PSYCHIATRIE',
  'PNEUMOLOGIE',
  'RHUMATOLOGIE',
  'UROLOGIE'
]

const DoctorFilters = ({ onFilterChange, onClearFilters }) => {
  const [filters, setFilters] = useState({
    specialite: '',
    latitude: '',
    longitude: '',
    rayonKm: '',
    date: '',
    heure: '',
    teleconsultation: false,
    ratingMin: ''
  })

  const handleChange = (field, value) => {
    const newFilters = { ...filters, [field]: value }
    setFilters(newFilters)
  }

  const handleApply = () => {
    const searchDTO = {}
    
    if (filters.specialite) {
      searchDTO.specialite = filters.specialite
    }
    
    if (filters.latitude && filters.longitude && filters.rayonKm) {
      searchDTO.latitude = parseFloat(filters.latitude)
      searchDTO.longitude = parseFloat(filters.longitude)
      searchDTO.rayonKm = parseFloat(filters.rayonKm)
    }
    
    if (filters.date && filters.heure) {
      searchDTO.date = filters.date
      searchDTO.heure = filters.heure
    }
    
    if (filters.teleconsultation) {
      searchDTO.teleconsultation = true
    }
    
    if (filters.ratingMin) {
      searchDTO.ratingMin = parseFloat(filters.ratingMin)
    }
    
    onFilterChange(searchDTO)
  }

  const handleClear = () => {
    const clearedFilters = {
      specialite: '',
      latitude: '',
      longitude: '',
      rayonKm: '',
      date: '',
      heure: '',
      teleconsultation: false,
      ratingMin: ''
    }
    setFilters(clearedFilters)
    onClearFilters()
  }

  const hasActiveFilters = () => {
    return filters.specialite || 
           (filters.latitude && filters.longitude && filters.rayonKm) ||
           (filters.date && filters.heure) ||
           filters.teleconsultation ||
           filters.ratingMin
  }

  return (
    <div className="doctor-filters">
      <h3>🔍 Filtres de recherche</h3>
      
      <div className="filters-grid">
        <div className="filter-group">
          <label>Spécialité</label>
          <select
            value={filters.specialite}
            onChange={(e) => handleChange('specialite', e.target.value)}
          >
            <option value="">Toutes les spécialités</option>
            {SPECIALITES.map(spec => (
              <option key={spec} value={spec}>
                {spec.replace('_', ' ')}
              </option>
            ))}
          </select>
        </div>

        <div className="filter-group">
          <label>Note minimum</label>
          <input
            type="number"
            min="0"
            max="5"
            step="0.1"
            value={filters.ratingMin}
            onChange={(e) => handleChange('ratingMin', e.target.value)}
            placeholder="Ex: 4.0"
          />
        </div>

        <div className="filter-group checkbox-group">
          <label>
            <input
              type="checkbox"
              checked={filters.teleconsultation}
              onChange={(e) => handleChange('teleconsultation', e.target.checked)}
            />
            Téléconsultation uniquement
          </label>
        </div>
      </div>

      <div className="filters-section">
        <h4>📍 Recherche par distance</h4>
        <div className="filters-grid">
          <div className="filter-group">
            <label>Latitude</label>
            <input
              type="number"
              step="any"
              value={filters.latitude}
              onChange={(e) => handleChange('latitude', e.target.value)}
              placeholder="Ex: 48.8566"
            />
          </div>
          <div className="filter-group">
            <label>Longitude</label>
            <input
              type="number"
              step="any"
              value={filters.longitude}
              onChange={(e) => handleChange('longitude', e.target.value)}
              placeholder="Ex: 2.3522"
            />
          </div>
          <div className="filter-group">
            <label>Rayon (km)</label>
            <input
              type="number"
              min="0"
              step="0.1"
              value={filters.rayonKm}
              onChange={(e) => handleChange('rayonKm', e.target.value)}
              placeholder="Ex: 10"
            />
          </div>
        </div>
      </div>

      <div className="filters-section">
        <h4>📅 Recherche par disponibilité</h4>
        <div className="filters-grid">
          <div className="filter-group">
            <label>Date</label>
            <input
              type="date"
              value={filters.date}
              onChange={(e) => handleChange('date', e.target.value)}
            />
          </div>
          <div className="filter-group">
            <label>Heure</label>
            <input
              type="time"
              value={filters.heure}
              onChange={(e) => handleChange('heure', e.target.value)}
            />
          </div>
        </div>
      </div>

      <div className="filters-actions">
        <button onClick={handleApply} className="btn-apply">
          Appliquer les filtres
        </button>
        {hasActiveFilters() && (
          <button onClick={handleClear} className="btn-clear">
            Effacer les filtres
          </button>
        )}
      </div>
    </div>
  )
}

export default DoctorFilters

