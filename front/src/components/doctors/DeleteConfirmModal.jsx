import { useState } from 'react'
import { doctorAPI } from '../../services/doctorApi'
import './DeleteConfirmModal.css'

const DeleteConfirmModal = ({ doctor, onClose, onConfirm }) => {
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const handleDelete = async () => {
    setError('')
    setLoading(true)
    try {
      await doctorAPI.deleteDoctor(doctor.id)
      onConfirm()
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors de la suppression du docteur')
      setLoading(false)
    }
  }

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content delete-modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>Confirmer la suppression</h2>
          <button className="modal-close" onClick={onClose}>×</button>
        </div>

        <div className="delete-modal-body">
          {error && (
            <div className="error-alert">
              {error}
            </div>
          )}

          <p>
            Êtes-vous sûr de vouloir supprimer le docteur <strong>{doctor.nomComplet}</strong> ?
          </p>
          <p className="warning-text">
            ⚠️ Cette action est irréversible. Le docteur sera marqué comme supprimé (soft delete).
          </p>

          <div className="modal-actions">
            <button type="button" onClick={onClose} className="btn-cancel">
              Annuler
            </button>
            <button
              type="button"
              onClick={handleDelete}
              disabled={loading}
              className="btn-delete"
            >
              {loading ? 'Suppression...' : 'Supprimer'}
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}

export default DeleteConfirmModal

