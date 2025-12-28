import { useState, useEffect } from 'react'
import { useForm, useFieldArray } from 'react-hook-form'
import { prescriptionAPI } from '../../services/prescriptionApi'
import { patientAPI } from '../../services/patientApi'
import { doctorAPI } from '../../services/doctorApi'
import { useAuth } from '../../context/AuthContext'
import './PrescriptionModal.css'

const PrescriptionModal = ({ mode, prescription, onClose, onSave, defaultPatientId }) => {
  const { user } = useAuth()
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [patients, setPatients] = useState([])
  const [doctorId, setDoctorId] = useState(null)

  useEffect(() => {
    if (user?.role === 'DOCTOR' && user?.id) {
      fetchDoctorId()
    }
    fetchPatients()
  }, [user])

  const fetchDoctorId = async () => {
    try {
      const response = await doctorAPI.getDoctorByUserId(user.id)
      setDoctorId(response.data.id)
    } catch (err) {
      console.error('Erreur lors de la récupération du doctorId:', err)
    }
  }

  const {
    register,
    handleSubmit,
    control,
    formState: { errors },
    reset,
    watch,
  } = useForm({
    defaultValues: prescription || {
      patientId: defaultPatientId || '',
      doctorId: doctorId || '',
      medications: [{ name: '', dosage: '', frequency: '', duration: '', instructions: '' }],
      instructions: '',
      date: new Date().toISOString().split('T')[0],
    },
  })

  const { fields, append, remove } = useFieldArray({
    control,
    name: 'medications',
  })

  useEffect(() => {
    if (prescription) {
      reset({
        patientId: prescription.patientId,
        doctorId: prescription.doctorId,
        medications: prescription.medications || [{ name: '', dosage: '', frequency: '', duration: '', instructions: '' }],
        instructions: prescription.instructions || '',
        date: prescription.date || new Date().toISOString().split('T')[0],
      })
    } else if (doctorId) {
      reset({
        patientId: defaultPatientId || '',
        doctorId: doctorId,
        medications: [{ name: '', dosage: '', frequency: '', duration: '', instructions: '' }],
        instructions: '',
        date: new Date().toISOString().split('T')[0],
      })
    }
  }, [prescription, doctorId, defaultPatientId, reset])

  const fetchPatients = async () => {
    try {
      const response = await patientAPI.getAllPatients({ page: 0, size: 1000 })
      const patientsData = response.data.content || response.data || []
      setPatients(patientsData)
    } catch (err) {
      console.error('Erreur lors de la récupération des patients:', err)
    }
  }

  const onSubmit = async (data) => {
    setError('')
    setLoading(true)
    try {
      // Filtrer les médicaments vides
      const validMedications = data.medications.filter(
        med => med.name.trim() !== '' && med.dosage.trim() !== ''
      )

      if (validMedications.length === 0) {
        setError('Au moins un médicament est requis')
        setLoading(false)
        return
      }

      const prescriptionData = {
        ...data,
        patientId: Number(data.patientId),
        doctorId: Number(doctorId || data.doctorId),
        medications: validMedications,
        date: data.date || new Date().toISOString().split('T')[0],
      }
      
      if (!prescriptionData.doctorId) {
        setError('ID du docteur manquant')
        setLoading(false)
        return
      }

      await prescriptionAPI.createPrescription(prescriptionData)
      onSave()
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors de la création de l\'ordonnance')
    } finally {
      setLoading(false)
    }
  }

  const addMedication = () => {
    append({ name: '', dosage: '', frequency: '', duration: '', instructions: '' })
  }

  const removeMedication = (index) => {
    if (fields.length > 1) {
      remove(index)
    }
  }

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content prescription-modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>Nouvelle ordonnance</h2>
          <button className="modal-close" onClick={onClose}>×</button>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className="prescription-form">
          {error && (
            <div className="error-alert">
              {error}
            </div>
          )}

          {/* Champ caché pour doctorId */}
          <input type="hidden" {...register('doctorId')} value={doctorId || ''} />

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="patientId">
                <span className="label-icon">👤</span>
                Patient *
              </label>
              <select
                id="patientId"
                {...register('patientId', { required: 'Le patient est obligatoire' })}
                className={errors.patientId ? 'error' : ''}
              >
                <option value="">-- Sélectionner un patient --</option>
                {patients.map(patient => (
                  <option key={patient.id} value={patient.id}>
                    {patient.prenom} {patient.nom}
                    {patient.telephone ? ` - ${patient.telephone}` : ''}
                  </option>
                ))}
              </select>
              {errors.patientId && <span className="error-message">{errors.patientId.message}</span>}
            </div>

            <div className="form-group">
              <label htmlFor="date">Date *</label>
              <input
                id="date"
                type="date"
                {...register('date', { required: 'La date est obligatoire' })}
                className={errors.date ? 'error' : ''}
                max={new Date().toISOString().split('T')[0]}
              />
              {errors.date && <span className="error-message">{errors.date.message}</span>}
            </div>
          </div>

          <div className="medications-section">
            <div className="section-header">
              <h3>📋 Médicaments *</h3>
              <button type="button" onClick={addMedication} className="btn-add-medication">
                + Ajouter un médicament
              </button>
            </div>

            {fields.map((field, index) => (
              <div key={field.id} className="medication-row">
                <div className="medication-header">
                  <span className="medication-number">Médicament {index + 1}</span>
                  {fields.length > 1 && (
                    <button
                      type="button"
                      onClick={() => removeMedication(index)}
                      className="btn-remove-medication"
                    >
                      ✕ Supprimer
                    </button>
                  )}
                </div>

                <div className="medication-fields">
                  <div className="form-group">
                    <label>Nom du médicament *</label>
                    <input
                      {...register(`medications.${index}.name`, { required: 'Le nom est obligatoire' })}
                      placeholder="Ex: Paracétamol"
                      className={errors.medications?.[index]?.name ? 'error' : ''}
                    />
                    {errors.medications?.[index]?.name && (
                      <span className="error-message">{errors.medications[index].name.message}</span>
                    )}
                  </div>

                  <div className="form-group">
                    <label>Dosage *</label>
                    <input
                      {...register(`medications.${index}.dosage`, { required: 'Le dosage est obligatoire' })}
                      placeholder="Ex: 500mg"
                      className={errors.medications?.[index]?.dosage ? 'error' : ''}
                    />
                    {errors.medications?.[index]?.dosage && (
                      <span className="error-message">{errors.medications[index].dosage.message}</span>
                    )}
                  </div>

                  <div className="form-group">
                    <label>Fréquence *</label>
                    <input
                      {...register(`medications.${index}.frequency`, { required: 'La fréquence est obligatoire' })}
                      placeholder="Ex: 3 fois par jour"
                      className={errors.medications?.[index]?.frequency ? 'error' : ''}
                    />
                    {errors.medications?.[index]?.frequency && (
                      <span className="error-message">{errors.medications[index].frequency.message}</span>
                    )}
                  </div>

                  <div className="form-group">
                    <label>Durée *</label>
                    <input
                      {...register(`medications.${index}.duration`, { required: 'La durée est obligatoire' })}
                      placeholder="Ex: 7 jours"
                      className={errors.medications?.[index]?.duration ? 'error' : ''}
                    />
                    {errors.medications?.[index]?.duration && (
                      <span className="error-message">{errors.medications[index].duration.message}</span>
                    )}
                  </div>

                  <div className="form-group full-width">
                    <label>Instructions spéciales</label>
                    <input
                      {...register(`medications.${index}.instructions`)}
                      placeholder="Ex: Pendant les repas"
                    />
                  </div>
                </div>
              </div>
            ))}
          </div>

          <div className="form-group">
            <label htmlFor="instructions">Instructions générales</label>
            <textarea
              id="instructions"
              {...register('instructions')}
              rows="4"
              placeholder="Ex: Repos recommandé. Éviter l'alcool pendant le traitement."
            />
          </div>

          <div className="modal-actions">
            <button type="button" onClick={onClose} className="btn-cancel">
              Annuler
            </button>
            <button type="submit" disabled={loading} className="btn-submit">
              {loading ? 'Création...' : 'Créer l\'ordonnance'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default PrescriptionModal

