import './PatientDetailsModal.css'

const PatientDetailsModal = ({ patient, onClose, onEdit }) => {
  const formatDate = (dateString) => {
    if (!dateString) return 'Non renseigné'
    const date = new Date(dateString)
    return date.toLocaleDateString('fr-FR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    })
  }

  const calculateAge = (dateNaissance) => {
    if (!dateNaissance) return '-'
    const today = new Date()
    const birthDate = new Date(dateNaissance)
    let age = today.getFullYear() - birthDate.getFullYear()
    const monthDiff = today.getMonth() - birthDate.getMonth()
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
      age--
    }
    return age
  }

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content details-modal" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>Détails du patient</h2>
          <button className="modal-close" onClick={onClose}>×</button>
        </div>

        <div className="patient-details">
          <div className="details-section">
            <h3>Informations personnelles</h3>
            <div className="detail-row">
              <span className="detail-label">Nom:</span>
              <span className="detail-value">{patient.nom}</span>
            </div>
            <div className="detail-row">
              <span className="detail-label">Prénom:</span>
              <span className="detail-value">{patient.prenom}</span>
            </div>
            <div className="detail-row">
              <span className="detail-label">Date de naissance:</span>
              <span className="detail-value">{formatDate(patient.dateNaissance)}</span>
            </div>
            <div className="detail-row">
              <span className="detail-label">Âge:</span>
              <span className="detail-value">{calculateAge(patient.dateNaissance)} ans</span>
            </div>
            <div className="detail-row">
              <span className="detail-label">Sexe:</span>
              <span className="detail-value">
                <span className={`badge badge-${patient.sexe === 'M' ? 'male' : 'female'}`}>
                  {patient.sexe === 'M' ? 'Masculin' : 'Féminin'}
                </span>
              </span>
            </div>
            <div className="detail-row">
              <span className="detail-label">Téléphone:</span>
              <span className="detail-value">{patient.telephone || 'Non renseigné'}</span>
            </div>
            <div className="detail-row">
              <span className="detail-label">Adresse:</span>
              <span className="detail-value">{patient.adresse || 'Non renseigné'}</span>
            </div>
          </div>

          <div className="details-section">
            <h3>Informations médicales</h3>
            <div className="detail-row">
              <span className="detail-label">Allergies:</span>
              <span className="detail-value">{patient.allergies || 'Aucune allergie connue'}</span>
            </div>
            <div className="detail-row">
              <span className="detail-label">Antécédents:</span>
              <span className="detail-value">{patient.antecedents || 'Aucun antécédent'}</span>
            </div>
          </div>

          <div className="details-section">
            <h3>Contact d'urgence</h3>
            <div className="detail-row">
              <span className="detail-label">Nom:</span>
              <span className="detail-value">{patient.contactUrgenceNom || 'Non renseigné'}</span>
            </div>
            <div className="detail-row">
              <span className="detail-label">Téléphone:</span>
              <span className="detail-value">{patient.contactUrgenceTelephone || 'Non renseigné'}</span>
            </div>
            <div className="detail-row">
              <span className="detail-label">Relation:</span>
              <span className="detail-value">{patient.contactUrgenceRelation || 'Non renseigné'}</span>
            </div>
          </div>

          {patient.createdAt && (
            <div className="details-section">
              <h3>Informations système</h3>
              <div className="detail-row">
                <span className="detail-label">Créé le:</span>
                <span className="detail-value">
                  {new Date(patient.createdAt).toLocaleString('fr-FR')}
                </span>
              </div>
              {patient.updatedAt && (
                <div className="detail-row">
                  <span className="detail-label">Modifié le:</span>
                  <span className="detail-value">
                    {new Date(patient.updatedAt).toLocaleString('fr-FR')}
                  </span>
                </div>
              )}
            </div>
          )}
        </div>

        <div className="modal-actions">
          <button onClick={onClose} className="btn-secondary">
            Fermer
          </button>
          <button onClick={onEdit} className="btn-primary">
            Modifier
          </button>
        </div>
      </div>
    </div>
  )
}

export default PatientDetailsModal

