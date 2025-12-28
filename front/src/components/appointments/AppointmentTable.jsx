import { useState } from 'react'
import './AppointmentTable.css'

const STATUS_LABELS = {
  PENDING: { label: 'En attente', color: '#FB8C00', bg: '#FFF3E0' },
  CONFIRMED: { label: 'Confirmé', color: '#43A047', bg: '#E8F5E9' },
  CANCELLED: { label: 'Annulé', color: '#E53935', bg: '#FFEBEE' },
  COMPLETED: { label: 'Terminé', color: '#1E88E5', bg: '#E3F2FD' },
  NO_SHOW: { label: 'Absent', color: '#757575', bg: '#F5F5F5' }
}

const AppointmentTable = ({ appointments, onEdit, onStatusChange, onReschedule, userRole }) => {
  const [rescheduleId, setRescheduleId] = useState(null)
  const [newDate, setNewDate] = useState('')
  const [newHeure, setNewHeure] = useState('')

  const formatDate = (dateString) => {
    if (!dateString) return '-'
    const date = new Date(dateString)
    return date.toLocaleDateString('fr-FR', {
      weekday: 'short',
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    })
  }

  const formatTime = (timeString) => {
    if (!timeString) return '-'
    return timeString.substring(0, 5)
  }

  const handleRescheduleSubmit = (id) => {
    if (newDate && newHeure) {
      onReschedule(id, newDate, newHeure)
      setRescheduleId(null)
      setNewDate('')
      setNewHeure('')
    }
  }

  const canEdit = (appointment) => {
    return appointment.status === 'PENDING' || appointment.status === 'CONFIRMED'
  }

  const canCancel = (appointment) => {
    return appointment.status === 'PENDING' || appointment.status === 'CONFIRMED'
  }

  if (appointments.length === 0) {
    return null
  }

  return (
    <div className="table-container">
      <table className="appointment-table">
        <thead>
          <tr>
            <th>Date</th>
            <th>Heure</th>
            <th>Docteur</th>
            <th>Patient</th>
            <th>Motif</th>
            <th>Statut</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {appointments.map((appointment) => (
            <tr key={appointment.id}>
              <td>{formatDate(appointment.date)}</td>
              <td>{formatTime(appointment.heure)}</td>
              <td>
                <div className="doctor-info">
                  <strong>{appointment.doctorNomComplet}</strong>
                  <span className="specialite">{appointment.doctorSpecialite}</span>
                </div>
              </td>
              <td>{appointment.patientNomComplet}</td>
              <td>{appointment.motif || '-'}</td>
              <td>
                <span
                  className="status-badge"
                  style={{
                    color: STATUS_LABELS[appointment.status]?.color || '#616161',
                    backgroundColor: STATUS_LABELS[appointment.status]?.bg || '#F5F5F5'
                  }}
                >
                  {STATUS_LABELS[appointment.status]?.label || appointment.status}
                </span>
              </td>
              <td>
                <div className="action-buttons">
                  {rescheduleId === appointment.id ? (
                    <div className="reschedule-form">
                      <input
                        type="date"
                        value={newDate}
                        onChange={(e) => setNewDate(e.target.value)}
                        className="reschedule-input"
                      />
                      <input
                        type="time"
                        value={newHeure}
                        onChange={(e) => setNewHeure(e.target.value)}
                        className="reschedule-input"
                      />
                      <button
                        onClick={() => handleRescheduleSubmit(appointment.id)}
                        className="btn-save-reschedule"
                      >
                        ✓
                      </button>
                      <button
                        onClick={() => {
                          setRescheduleId(null)
                          setNewDate('')
                          setNewHeure('')
                        }}
                        className="btn-cancel-reschedule"
                      >
                        ✕
                      </button>
                    </div>
                  ) : (
                    <>
                      {canEdit(appointment) && (
                        <button
                          onClick={() => onEdit(appointment)}
                          className="btn-action btn-edit"
                          title="Modifier"
                        >
                          ✏️
                        </button>
                      )}
                      
                      {appointment.status === 'PENDING' && (
                        <button
                          onClick={() => onStatusChange(appointment.id, 'CONFIRMED')}
                          className="btn-action btn-confirm"
                          title="Confirmer"
                        >
                          ✓
                        </button>
                      )}
                      
                      {canCancel(appointment) && (
                        <button
                          onClick={() => onStatusChange(appointment.id, 'CANCELLED')}
                          className="btn-action btn-cancel"
                          title="Annuler"
                        >
                          🚫
                        </button>
                      )}
                      
                      {appointment.status === 'CONFIRMED' && (
                        <>
                          <button
                            onClick={() => onStatusChange(appointment.id, 'COMPLETED')}
                            className="btn-action btn-complete"
                            title="Marquer comme terminé"
                          >
                            ✅
                          </button>
                          <button
                            onClick={() => {
                              setRescheduleId(appointment.id)
                              setNewDate(appointment.date)
                              setNewHeure(appointment.heure)
                            }}
                            className="btn-action btn-reschedule"
                            title="Reprogrammer"
                          >
                            📅
                          </button>
                        </>
                      )}
                      
                      {appointment.status === 'CONFIRMED' && (
                        <button
                          onClick={() => onStatusChange(appointment.id, 'NO_SHOW')}
                          className="btn-action btn-no-show"
                          title="Marquer comme absent"
                        >
                          👤
                        </button>
                      )}
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

export default AppointmentTable

