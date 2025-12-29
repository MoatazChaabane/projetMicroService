import { useState, useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { appointmentAPI } from '../../services/appointmentApi'
import { doctorAPI } from '../../services/doctorApi'
import { patientAPI } from '../../services/patientApi'
import SymptomAssistant from '../symptoms/SymptomAssistant'
import './AppointmentModal.css'

const AppointmentModal = ({ mode, appointment, onClose, onSave, defaultDoctorId, defaultDate, defaultHeure }) => {
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [checkingAvailability, setCheckingAvailability] = useState(false)
  const [availabilityMessage, setAvailabilityMessage] = useState('')
  const [doctors, setDoctors] = useState([])
  const [patients, setPatients] = useState([])
  const [showSymptomAssistant, setShowSymptomAssistant] = useState(false)
  const [symptomAnalysis, setSymptomAnalysis] = useState(null)

  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
    watch,
  } = useForm({
    defaultValues: appointment || {
      doctorId: defaultDoctorId || '',
      patientId: '',
      date: defaultDate || '',
      heure: defaultHeure || '',
      motif: '',
      notes: '',
    },
  })

  const watchedDoctorId = watch('doctorId')
  const watchedDate = watch('date')
  const watchedHeure = watch('heure')

  useEffect(() => {
    if (appointment) {
      reset({
        doctorId: appointment.doctorId || '',
        patientId: appointment.patientId || '',
        date: appointment.date || '',
        heure: appointment.heure || '',
        motif: appointment.motif || '',
        notes: appointment.notes || '',
      })
    } else if (defaultDoctorId || defaultDate || defaultHeure) {
      reset({
        doctorId: defaultDoctorId || '',
        patientId: '',
        date: defaultDate || '',
        heure: defaultHeure || '',
        motif: '',
        notes: '',
      })
    }
    fetchDoctorsAndPatients()
  }, [appointment, defaultDoctorId, defaultDate, defaultHeure, reset])

  useEffect(() => {
    if (watchedDoctorId && watchedDate && watchedHeure) {
      checkAvailability()
    } else {
      setAvailabilityMessage('')
    }
  }, [watchedDoctorId, watchedDate, watchedHeure])

  const fetchDoctorsAndPatients = async () => {
    try {
      const [doctorsRes, patientsRes] = await Promise.allSettled([
        doctorAPI.getAllDoctors({ page: 0, size: 1000 }),
        patientAPI.getAllPatients({ page: 0, size: 1000 })
      ])
      
      if (doctorsRes.status === 'fulfilled') {
        const doctorsData = doctorsRes.value.data.content || doctorsRes.value.data || []
        setDoctors(doctorsData)
      }
      if (patientsRes.status === 'fulfilled') {
        const patientsData = patientsRes.value.data.content || patientsRes.value.data || []
        setPatients(patientsData)
      }
    } catch (err) {
      console.error('Error fetching doctors/patients:', err)
    }
  }

  const checkAvailability = async () => {
    if (!watchedDoctorId || !watchedDate || !watchedHeure) return
    
    setCheckingAvailability(true)
    try {
      const response = await appointmentAPI.checkAvailability(
        Number(watchedDoctorId),
        watchedDate,
        watchedHeure
      )
      const availability = response.data
      setAvailabilityMessage(availability.message)
    } catch (err) {
      setAvailabilityMessage('Erreur lors de la vérification')
    } finally {
      setCheckingAvailability(false)
    }
  }

  const handleAnalysisComplete = (analysis) => {
    setSymptomAnalysis(analysis)
  }

  const onSubmit = async (data) => {
    setError('')
    setLoading(true)
    try {
      let appointmentData = { ...data }

      if (symptomAnalysis && symptomAnalysis.summary) {
        appointmentData.notes = appointmentData.notes 
          ? `${appointmentData.notes}\n\n--- Résumé Symptômes ---\n${symptomAnalysis.summary}`
          : `--- Résumé Symptômes ---\n${symptomAnalysis.summary}`
      }
      
      if (mode === 'add') {
        await appointmentAPI.createAppointment(appointmentData)
      } else {
        await appointmentAPI.updateAppointment(appointment.id, appointmentData)
      }
      onSave()
    } catch (err) {
      setError(err.response?.data?.message || `Erreur lors de la ${mode === 'add' ? 'création' : 'modification'} du rendez-vous`)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content appointment-modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>{mode === 'add' ? 'Nouveau rendez-vous' : 'Modifier le rendez-vous'}</h2>
          <button className="modal-close" onClick={onClose}>×</button>
        </div>

        {showSymptomAssistant && mode === 'add' ? (
          <div className="symptom-assistant-wrapper">
            <div className="assistant-close-header">
              <h3>Assistant Symptômes</h3>
              <button 
                type="button" 
                onClick={() => setShowSymptomAssistant(false)}
                className="btn-close-assistant"
              >
                ✕
              </button>
            </div>
            <SymptomAssistant
              onAnalysisComplete={(analysis) => {
                handleAnalysisComplete(analysis)
                setShowSymptomAssistant(false) // Fermer l'assistant après l'analyse
              }}
              patientId={watch('patientId') || undefined}
            />
          </div>
        ) : (
          <form onSubmit={handleSubmit(onSubmit)} className="appointment-form">
            {error && (
              <div className="error-alert">
                {error}
              </div>
            )}

            {symptomAnalysis && (
              <div className="analysis-summary-box">
                <div className="analysis-summary-header">
                  <span>✅ Analyse de symptômes disponible</span>
                  <button
                    type="button"
                    onClick={() => setSymptomAnalysis(null)}
                    className="btn-remove-analysis"
                  >
                    ✕
                  </button>
                </div>
                <div className="analysis-summary-preview">
                  <strong>Symptômes:</strong> {symptomAnalysis.symptoms?.join(', ') || 'N/A'}
                  {symptomAnalysis.urgentRecommendation && (
                    <span className="urgent-indicator">⚠️ URGENT</span>
                  )}
                </div>
              </div>
            )}

            {mode === 'add' && !showSymptomAssistant && (
              <div className="assistant-toggle">
                <button
                  type="button"
                  onClick={() => setShowSymptomAssistant(true)}
                  className="btn-open-assistant"
                >
                  🤖 Utiliser l'Assistant Symptômes
                </button>
              </div>
            )}

            <div className="form-row">
            <div className="form-group">
              <label htmlFor="doctorId">
                <span className="label-icon">👨‍⚕️</span>
                Docteur *
              </label>
              <select
                id="doctorId"
                {...register('doctorId', { required: 'Le docteur est obligatoire' })}
                className={errors.doctorId ? 'error' : ''}
                disabled={mode === 'edit'}
              >
                <option value="">-- Sélectionner un docteur --</option>
                {doctors.map(doctor => (
                  <option key={doctor.id} value={doctor.id}>
                    Dr. {doctor.nomComplet} - {doctor.specialite?.replace(/_/g, ' ') || 'Non spécifiée'}
                    {doctor.teleconsultation ? ' (Téléconsultation ✓)' : ''}
                  </option>
                ))}
              </select>
              {errors.doctorId && <span className="error-message">{errors.doctorId.message}</span>}
              {watchedDoctorId && doctors.find(d => d.id === Number(watchedDoctorId)) && (
                <div className="selected-info">
                  <strong>Docteur sélectionné:</strong> {doctors.find(d => d.id === Number(watchedDoctorId))?.nomComplet}
                </div>
              )}
            </div>

            <div className="form-group">
              <label htmlFor="patientId">
                <span className="label-icon">👤</span>
                Patient *
              </label>
              <select
                id="patientId"
                {...register('patientId', { required: 'Le patient est obligatoire' })}
                className={errors.patientId ? 'error' : ''}
                disabled={mode === 'edit'}
              >
                <option value="">-- Sélectionner un patient --</option>
                {patients.map(patient => (
                  <option key={patient.id} value={patient.id}>
                    {patient.prenom} {patient.nom}
                    {patient.telephone ? ` - ${patient.telephone}` : ''}
                    {patient.email ? ` (${patient.email})` : ''}
                  </option>
                ))}
              </select>
              {errors.patientId && <span className="error-message">{errors.patientId.message}</span>}
              {watch('patientId') && patients.find(p => p.id === Number(watch('patientId'))) && (
                <div className="selected-info">
                  <strong>Patient sélectionné:</strong> {patients.find(p => p.id === Number(watch('patientId')))?.prenom} {patients.find(p => p.id === Number(watch('patientId')))?.nom}
                </div>
              )}
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="date">Date *</label>
              <input
                id="date"
                type="date"
                {...register('date', { required: 'La date est obligatoire' })}
                className={errors.date ? 'error' : ''}
                min={new Date().toISOString().split('T')[0]}
              />
              {errors.date && <span className="error-message">{errors.date.message}</span>}
            </div>

            <div className="form-group">
              <label htmlFor="heure">Heure *</label>
              <input
                id="heure"
                type="time"
                {...register('heure', { required: "L'heure est obligatoire" })}
                className={errors.heure ? 'error' : ''}
              />
              {errors.heure && <span className="error-message">{errors.heure.message}</span>}
            </div>
          </div>

          {watchedDoctorId && watchedDate && watchedHeure && (
            <div className={`availability-check ${availabilityMessage.includes('disponible') ? 'available' : 'unavailable'}`}>
              {checkingAvailability ? (
                <span>Vérification de la disponibilité...</span>
              ) : (
                <span>{availabilityMessage}</span>
              )}
            </div>
          )}

          <div className="form-group">
            <label htmlFor="motif">Motif</label>
            <input
              id="motif"
              type="text"
              {...register('motif')}
              placeholder="Ex: Consultation de routine"
            />
          </div>

          <div className="form-group">
            <label htmlFor="notes">Notes</label>
            <textarea
              id="notes"
              {...register('notes')}
              rows="3"
              placeholder="Notes additionnelles..."
            />
          </div>

            <div className="modal-actions">
              <button type="button" onClick={onClose} className="btn-cancel">
                Annuler
              </button>
              <button type="submit" disabled={loading || checkingAvailability} className="btn-submit">
                {loading ? 'Enregistrement...' : mode === 'add' ? 'Créer' : 'Modifier'}
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  )
}

export default AppointmentModal

