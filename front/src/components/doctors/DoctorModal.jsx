import { useState, useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { doctorAPI } from '../../services/doctorApi'
import './DoctorModal.css'

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

const JOURS_SEMAINE = [
  'LUNDI',
  'MARDI',
  'MERCREDI',
  'JEUDI',
  'VENDREDI',
  'SAMEDI',
  'DIMANCHE'
]

const DoctorModal = ({ mode, doctor, onClose, onSave }) => {
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [horaires, setHoraires] = useState([])

  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
    watch,
  } = useForm({
    defaultValues: doctor || {
      userId: '',
      specialite: '',
      nomClinique: '',
      adresse: '',
      latitude: '',
      longitude: '',
      tarifConsultation: '',
      langues: [],
      teleconsultation: false,
    },
  })

  useEffect(() => {
    if (doctor) {
      reset({
        userId: doctor.userId || '',
        specialite: doctor.specialite || '',
        nomClinique: doctor.nomClinique || '',
        adresse: doctor.adresse || '',
        latitude: doctor.latitude || '',
        longitude: doctor.longitude || '',
        tarifConsultation: doctor.tarifConsultation || '',
        langues: Array.isArray(doctor.langues) ? doctor.langues.join(', ') : '',
        teleconsultation: doctor.teleconsultation || false,
      })
      setHoraires(doctor.horaires || [])
    } else {
      setHoraires([])
    }
  }, [doctor, reset])

  const addTimeSlot = () => {
    setHoraires([...horaires, {
      jour: 'LUNDI',
      heureDebut: '09:00',
      heureFin: '12:00',
      disponible: true
    }])
  }

  const removeTimeSlot = (index) => {
    setHoraires(horaires.filter((_, i) => i !== index))
  }

  const updateTimeSlot = (index, field, value) => {
    const updated = [...horaires]
    updated[index] = { ...updated[index], [field]: value }
    setHoraires(updated)
  }

  const onSubmit = async (data) => {
    setError('')
    setLoading(true)
    try {

      let languesArray = []
      if (data.langues) {
        if (typeof data.langues === 'string') {
          languesArray = data.langues.split(',').map(l => l.trim()).filter(l => l.length > 0)
        } else if (Array.isArray(data.langues)) {
          languesArray = data.langues
        }
      }

      const formData = {
        ...data,
        userId: Number(data.userId),
        latitude: data.latitude ? parseFloat(data.latitude) : null,
        longitude: data.longitude ? parseFloat(data.longitude) : null,
        tarifConsultation: data.tarifConsultation ? parseFloat(data.tarifConsultation) : null,
        langues: languesArray,
        teleconsultation: data.teleconsultation || false,
        horaires: horaires.map(ts => ({
          jour: ts.jour,
          heureDebut: ts.heureDebut,
          heureFin: ts.heureFin,
          disponible: ts.disponible !== false
        }))
      }

      if (mode === 'add') {
        await doctorAPI.createDoctor(formData)
      } else {
        await doctorAPI.updateDoctor(doctor.id, formData)
      }
      onSave()
    } catch (err) {
      setError(err.response?.data?.message || `Erreur lors de la ${mode === 'add' ? 'création' : 'modification'} du docteur`)
    } finally {
      setLoading(false)
    }
  }

  const languesInput = watch('langues')

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content doctor-modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>{mode === 'add' ? 'Ajouter un docteur' : 'Modifier le docteur'}</h2>
          <button className="modal-close" onClick={onClose}>×</button>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className="doctor-form">
          {error && (
            <div className="error-alert">
              {error}
            </div>
          )}

          <div className="form-section">
            <h3>Informations de base</h3>
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="userId">ID Utilisateur *</label>
                <input
                  id="userId"
                  type="number"
                  {...register('userId', { required: "L'ID utilisateur est obligatoire" })}
                  className={errors.userId ? 'error' : ''}
                />
                {errors.userId && <span className="error-message">{errors.userId.message}</span>}
              </div>

              <div className="form-group">
                <label htmlFor="specialite">Spécialité *</label>
                <select
                  id="specialite"
                  {...register('specialite', { required: 'La spécialité est obligatoire' })}
                  className={errors.specialite ? 'error' : ''}
                >
                  <option value="">Sélectionner une spécialité</option>
                  {SPECIALITES.map(spec => (
                    <option key={spec} value={spec}>
                      {spec.replace('_', ' ')}
                    </option>
                  ))}
                </select>
                {errors.specialite && <span className="error-message">{errors.specialite.message}</span>}
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label htmlFor="nomClinique">Nom de la clinique</label>
                <input
                  id="nomClinique"
                  type="text"
                  {...register('nomClinique')}
                />
              </div>

              <div className="form-group">
                <label htmlFor="tarifConsultation">Tarif consultation (€)</label>
                <input
                  id="tarifConsultation"
                  type="number"
                  step="0.01"
                  min="0"
                  {...register('tarifConsultation')}
                />
              </div>
            </div>

            <div className="form-group">
              <label htmlFor="adresse">Adresse</label>
              <input
                id="adresse"
                type="text"
                {...register('adresse')}
              />
            </div>

            <div className="form-row">
              <div className="form-group">
                <label htmlFor="latitude">Latitude</label>
                <input
                  id="latitude"
                  type="number"
                  step="any"
                  {...register('latitude')}
                  placeholder="Ex: 48.8566"
                />
              </div>

              <div className="form-group">
                <label htmlFor="longitude">Longitude</label>
                <input
                  id="longitude"
                  type="number"
                  step="any"
                  {...register('longitude')}
                  placeholder="Ex: 2.3522"
                />
              </div>
            </div>

            <div className="form-group">
              <label htmlFor="langues">Langues (séparées par des virgules)</label>
              <input
                id="langues"
                type="text"
                {...register('langues')}
                placeholder="Ex: Français, Anglais, Arabe"
                defaultValue={doctor?.langues ? doctor.langues.join(', ') : ''}
              />
              <small>Entrez les langues séparées par des virgules</small>
            </div>

            <div className="form-group checkbox-group">
              <label>
                <input
                  type="checkbox"
                  {...register('teleconsultation')}
                />
                Autorise la téléconsultation
              </label>
            </div>
          </div>

          <div className="form-section">
            <div className="section-header">
              <h3>Horaires de disponibilité</h3>
              <button type="button" onClick={addTimeSlot} className="btn-add-timeslot">
                + Ajouter un créneau
              </button>
            </div>

            {horaires.length === 0 && (
              <p className="no-timeslots">Aucun créneau horaire défini</p>
            )}

            {horaires.map((timeslot, index) => (
              <div key={index} className="timeslot-item">
                <div className="timeslot-fields">
                  <select
                    value={timeslot.jour || 'LUNDI'}
                    onChange={(e) => updateTimeSlot(index, 'jour', e.target.value)}
                  >
                    {JOURS_SEMAINE.map(jour => (
                      <option key={jour} value={jour}>{jour}</option>
                    ))}
                  </select>

                  <input
                    type="time"
                    value={timeslot.heureDebut || '09:00'}
                    onChange={(e) => updateTimeSlot(index, 'heureDebut', e.target.value)}
                  />

                  <span>à</span>

                  <input
                    type="time"
                    value={timeslot.heureFin || '12:00'}
                    onChange={(e) => updateTimeSlot(index, 'heureFin', e.target.value)}
                  />

                  <label className="checkbox-label">
                    <input
                      type="checkbox"
                      checked={timeslot.disponible !== false}
                      onChange={(e) => updateTimeSlot(index, 'disponible', e.target.checked)}
                    />
                    Disponible
                  </label>

                  <button
                    type="button"
                    onClick={() => removeTimeSlot(index)}
                    className="btn-remove-timeslot"
                  >
                    🗑️
                  </button>
                </div>
              </div>
            ))}
          </div>

          <div className="modal-actions">
            <button type="button" onClick={onClose} className="btn-cancel">
              Annuler
            </button>
            <button type="submit" disabled={loading} className="btn-submit">
              {loading ? 'Enregistrement...' : mode === 'add' ? 'Créer' : 'Modifier'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default DoctorModal

