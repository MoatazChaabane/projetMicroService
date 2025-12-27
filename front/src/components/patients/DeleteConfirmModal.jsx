import { useState } from 'react'
import { patientAPI } from '../../services/patientApi'
import './DeleteConfirmModal.css'

const DeleteConfirmModal = ({ patient, onClose, onConfirm }) => {
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const handleDelete = async () => {
    setError('')
    setLoading(true)
    try {
      await patientAPI.deletePatient(patient.id)
      onConfirm()
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors de la suppression du patient')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content delete-modal" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>Confirmer la suppression</h2>
          <button className="modal-close" onClick={onClose}>×</button>
        </div>

        <div className="delete-content">
          <p>
            Êtes-vous sûr de vouloir supprimer le patient{' '}
            <strong>{patient.prenom} {patient.nom}</strong> ?
          </p>
          <p className="warning-text">
            ⚠️ Cette action est irréversible. Le patient sera marqué comme supprimé.
          </p>
        </div>

        {error && <div className="error-alert">{error}</div>}

        <div className="modal-actions">
          <button onClick={onClose} className="btn-secondary" disabled={loading}>
            Annuler
          </button>
          <button onClick={handleDelete} disabled={loading} className="btn-danger">
            {loading ? 'Suppression...' : 'Supprimer'}
          </button>
        </div>
      </div>
    </div>
  )
}

export default DeleteConfirmModal

