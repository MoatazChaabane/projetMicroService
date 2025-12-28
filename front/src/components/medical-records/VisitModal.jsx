import { useState, useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { useAuth } from '../../context/AuthContext'
import { doctorAPI } from '../../services/doctorApi'
import { medicalRecordAPI } from '../../services/medicalRecordApi'
import './VisitModal.css'

const VisitModal = ({ medicalRecordId, onClose, onSave }) => {
  const { user } = useAuth()
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [doctorId, setDoctorId] = useState(null)

  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
  } = useForm({
    defaultValues: {
      medicalRecordId: medicalRecordId || '',
      doctorId: '',
      visitDate: new Date().toISOString().split('T')[0],
      visitTime: new Date().toTimeString().slice(0, 5),
      reason: '',
      symptoms: '',
      diagnosis: '',
      treatment: '',
      notes: '',
    },
  })

  useEffect(() => {
    if (user?.role === 'DOCTOR' && user?.id) {
      fetchDoctorId()
    }
  }, [user])

  const fetchDoctorId = async () => {
    try {
      const response = await doctorAPI.getDoctorByUserId(user.id)
      setDoctorId(response.data.id)
      reset({
        ...reset().defaultValues,
        doctorId: response.data.id
      })
    } catch (err) {
      console.error('Erreur lors de la récupération du doctorId:', err)
    }
  }

  const onSubmit = async (data) => {
    setError('')
    setLoading(true)
    try {
      const visitData = {
        ...data,
        medicalRecordId: Number(medicalRecordId),
        doctorId: Number(doctorId || data.doctorId),
        visitDate: data.visitDate || new Date().toISOString().split('T')[0],
      }
      
      await medicalRecordAPI.addVisit(visitData)
      onSave()
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors de la création de la consultation')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content visit-modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>Nouvelle consultation</h2>
          <button className="modal-close" onClick={onClose}>×</button>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className="visit-form">
          {error && (
            <div className="error-alert">
              {error}
            </div>
          )}

          <input type="hidden" {...register('medicalRecordId')} value={medicalRecordId || ''} />
          <input type="hidden" {...register('doctorId')} value={doctorId || ''} />

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="visitDate">Date *</label>
              <input
                id="visitDate"
                type="date"
                {...register('visitDate', { required: 'La date est obligatoire' })}
                className={errors.visitDate ? 'error' : ''}
                max={new Date().toISOString().split('T')[0]}
              />
              {errors.visitDate && <span className="error-message">{errors.visitDate.message}</span>}
            </div>

            <div className="form-group">
              <label htmlFor="visitTime">Heure</label>
              <input
                id="visitTime"
                type="time"
                {...register('visitTime')}
              />
            </div>
          </div>

          <div className="form-group">
            <label htmlFor="reason">Motif de consultation</label>
            <textarea
              id="reason"
              {...register('reason')}
              rows="2"
              placeholder="Ex: Consultation de routine"
            />
          </div>

          <div className="form-group">
            <label htmlFor="symptoms">Symptômes observés</label>
            <textarea
              id="symptoms"
              {...register('symptoms')}
              rows="3"
              placeholder="Décrire les symptômes..."
            />
          </div>

          <div className="form-group">
            <label htmlFor="diagnosis">Diagnostic</label>
            <textarea
              id="diagnosis"
              {...register('diagnosis')}
              rows="3"
              placeholder="Diagnostic posé..."
            />
          </div>

          <div className="form-group">
            <label htmlFor="treatment">Traitement prescrit</label>
            <textarea
              id="treatment"
              {...register('treatment')}
              rows="3"
              placeholder="Traitement recommandé..."
            />
          </div>

          <div className="form-group">
            <label htmlFor="notes">Notes additionnelles</label>
            <textarea
              id="notes"
              {...register('notes')}
              rows="3"
              placeholder="Notes complémentaires..."
            />
          </div>

          <div className="modal-actions">
            <button type="button" onClick={onClose} className="btn-cancel">
              Annuler
            </button>
            <button type="submit" disabled={loading} className="btn-submit">
              {loading ? 'Enregistrement...' : 'Enregistrer'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default VisitModal

