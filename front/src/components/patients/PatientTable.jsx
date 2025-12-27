import './PatientTable.css'

const PatientTable = ({ patients, onEdit, onView, onDelete, onSort, sortBy, sortDir }) => {
  const getSortIcon = (field) => {
    if (sortBy !== field) return '↕️'
    return sortDir === 'asc' ? '↑' : '↓'
  }

  const formatDate = (dateString) => {
    if (!dateString) return '-'
    const date = new Date(dateString)
    return date.toLocaleDateString('fr-FR')
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

  if (patients.length === 0) {
    return null
  }

  return (
    <div className="table-container">
      <table className="patient-table">
        <thead>
          <tr>
            <th onClick={() => onSort('nom')} className="sortable">
              Nom {getSortIcon('nom')}
            </th>
            <th onClick={() => onSort('prenom')} className="sortable">
              Prénom {getSortIcon('prenom')}
            </th>
            <th onClick={() => onSort('dateNaissance')} className="sortable">
              Date de naissance {getSortIcon('dateNaissance')}
            </th>
            <th>Âge</th>
            <th>Sexe</th>
            <th>Téléphone</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {patients.map((patient) => (
            <tr key={patient.id}>
              <td>{patient.nom}</td>
              <td>{patient.prenom}</td>
              <td>{formatDate(patient.dateNaissance)}</td>
              <td>{calculateAge(patient.dateNaissance)} ans</td>
              <td>
                <span className={`badge badge-${patient.sexe === 'M' ? 'male' : 'female'}`}>
                  {patient.sexe === 'M' ? 'M' : 'F'}
                </span>
              </td>
              <td>{patient.telephone || '-'}</td>
              <td>
                <div className="action-buttons">
                  <button
                    onClick={() => onView(patient)}
                    className="btn-action btn-view"
                    title="Voir les détails"
                  >
                    👁️
                  </button>
                  <button
                    onClick={() => onEdit(patient)}
                    className="btn-action btn-edit"
                    title="Modifier"
                  >
                    ✏️
                  </button>
                  <button
                    onClick={() => onDelete(patient)}
                    className="btn-action btn-delete"
                    title="Supprimer"
                  >
                    🗑️
                  </button>
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

export default PatientTable

