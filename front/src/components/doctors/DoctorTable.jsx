import { useNavigate } from 'react-router-dom'
import './DoctorTable.css'

const DoctorTable = ({ doctors, onEdit, onDelete, onSort, sortBy, sortDir, userRole }) => {
  const navigate = useNavigate()
  
  const getSortIcon = (field) => {
    if (sortBy !== field) return '↕️'
    return sortDir === 'asc' ? '↑' : '↓'
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

  const isAdmin = userRole === 'ADMIN'

  if (doctors.length === 0) {
    return null
  }

  return (
    <div className="table-container">
      <table className="doctor-table">
        <thead>
          <tr>
            <th onClick={() => onSort('nomComplet')} className="sortable">
              Docteur {getSortIcon('nomComplet')}
            </th>
            <th onClick={() => onSort('specialite')} className="sortable">
              Spécialité {getSortIcon('specialite')}
            </th>
            <th>Clinique</th>
            <th onClick={() => onSort('rating')} className="sortable">
              Note {getSortIcon('rating')}
            </th>
            <th onClick={() => onSort('tarifConsultation')} className="sortable">
              Tarif {getSortIcon('tarifConsultation')}
            </th>
            <th>Téléconsultation</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {doctors.map((doctor) => (
            <tr key={doctor.id}>
              <td>
                <div className="doctor-name">
                  <strong>{doctor.nomComplet || '-'}</strong>
                  {doctor.email && (
                    <div className="doctor-email">{doctor.email}</div>
                  )}
                </div>
              </td>
              <td>
                <span className="badge badge-specialite">
                  {formatSpecialite(doctor.specialite)}
                </span>
              </td>
              <td>{doctor.nomClinique || '-'}</td>
              <td>
                <div className="rating-cell">
                  {formatRating(doctor.rating)}
                  {doctor.nombreAvis > 0 && (
                    <span className="review-count">({doctor.nombreAvis})</span>
                  )}
                </div>
              </td>
              <td>{formatPrice(doctor.tarifConsultation)}</td>
              <td>
                {doctor.teleconsultation ? (
                  <span className="badge badge-success">✓ Oui</span>
                ) : (
                  <span className="badge badge-secondary">Non</span>
                )}
              </td>
              <td>
                <div className="action-buttons">
                  <button
                    onClick={() => navigate(`/doctors/${doctor.id}`)}
                    className="btn-action btn-view"
                    title="Voir les détails"
                  >
                    👁️
                  </button>
                  {isAdmin && (
                    <>
                      <button
                        onClick={() => onEdit(doctor)}
                        className="btn-action btn-edit"
                        title="Modifier"
                      >
                        ✏️
                      </button>
                      <button
                        onClick={() => onDelete(doctor)}
                        className="btn-action btn-delete"
                        title="Supprimer"
                      >
                        🗑️
                      </button>
                    </>
                  )}
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

export default DoctorTable

