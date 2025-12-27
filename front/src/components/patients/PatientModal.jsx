import { useState, useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { patientAPI } from '../../services/patientApi'
import './PatientModal.css'

const PatientModal = ({ mode, patient, onClose, onSave }) => {
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
  } = useForm({
    defaultValues: patient || {
      nom: '',
      prenom: '',
      dateNaissance: '',
      sexe: '',
      telephone: '',
      adresse: '',
      allergies: '',
      antecedents: '',
      contactUrgenceNom: '',
      contactUrgenceTelephone: '',
      contactUrgenceRelation: '',
    },
  })

  useEffect(() => {
    if (patient) {
      reset({
        nom: patient.nom || '',
        prenom: patient.prenom || '',
        dateNaissance: patient.dateNaissance || '',
        sexe: patient.sexe || '',
        telephone: patient.telephone || '',
        adresse: patient.adresse || '',
        allergies: patient.allergies || '',
        antecedents: patient.antecedents || '',
        contactUrgenceNom: patient.contactUrgenceNom || '',
        contactUrgenceTelephone: patient.contactUrgenceTelephone || '',
        contactUrgenceRelation: patient.contactUrgenceRelation || '',
      })
    }
  }, [patient, reset])

  const onSubmit = async (data) => {
    setError('')
    setLoading(true)
    try {
      if (mode === 'add') {
        await patientAPI.createPatient(data)
      } else {
        await patientAPI.updatePatient(patient.id, data)
      }
      onSave()
    } catch (err) {
      setError(err.response?.data?.message || `Erreur lors de la ${mode === 'add' ? 'création' : 'modification'} du patient`)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>{mode === 'add' ? 'Ajouter un patient' : 'Modifier le patient'}</h2>
          <button className="modal-close" onClick={onClose}>×</button>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className="patient-form">
          <div className="form-row">
            <div className="form-group">
              <label htmlFor="nom">Nom *</label>
              <input
                id="nom"
                type="text"
                {...register('nom', { required: 'Le nom est obligatoire' })}
                className={errors.nom ? 'error' : ''}
              />
              {errors.nom && <span className="error-message">{errors.nom.message}</span>}
            </div>

            <div className="form-group">
              <label htmlFor="prenom">Prénom *</label>
              <input
                id="prenom"
                type="text"
                {...register('prenom', { required: 'Le prénom est obligatoire' })}
                className={errors.prenom ? 'error' : ''}
              />
              {errors.prenom && <span className="error-message">{errors.prenom.message}</span>}
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="dateNaissance">Date de naissance *</label>
              <input
                id="dateNaissance"
                type="date"
                {...register('dateNaissance', { required: 'La date de naissance est obligatoire' })}
                className={errors.dateNaissance ? 'error' : ''}
                max={new Date().toISOString().split('T')[0]}
              />
              {errors.dateNaissance && (
                <span className="error-message">{errors.dateNaissance.message}</span>
              )}
            </div>

            <div className="form-group">
              <label htmlFor="sexe">Sexe *</label>
              <select
                id="sexe"
                {...register('sexe', { required: 'Le sexe est obligatoire' })}
                className={errors.sexe ? 'error' : ''}
              >
                <option value="">Sélectionner</option>
                <option value="M">Masculin</option>
                <option value="F">Féminin</option>
              </select>
              {errors.sexe && <span className="error-message">{errors.sexe.message}</span>}
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="telephone">Téléphone</label>
              <input
                id="telephone"
                type="tel"
                {...register('telephone', {
                  pattern: {
                    value: /^[+]?[0-9]{8,15}$/,
                    message: 'Format de téléphone invalide',
                  },
                })}
                className={errors.telephone ? 'error' : ''}
                placeholder="+33123456789"
              />
              {errors.telephone && (
                <span className="error-message">{errors.telephone.message}</span>
              )}
            </div>
          </div>

          <div className="form-group">
            <label htmlFor="adresse">Adresse</label>
            <textarea
              id="adresse"
              rows="2"
              {...register('adresse')}
              placeholder="123 Rue de la Paix, 75001 Paris"
            />
          </div>

          <div className="form-group">
            <label htmlFor="allergies">Allergies</label>
            <textarea
              id="allergies"
              rows="2"
              {...register('allergies')}
              placeholder="Pénicilline, Pollen..."
            />
          </div>

          <div className="form-group">
            <label htmlFor="antecedents">Antécédents médicaux</label>
            <textarea
              id="antecedents"
              rows="3"
              {...register('antecedents')}
              placeholder="Hypertension, Diabète type 2..."
            />
          </div>

          <div className="section-title">Contact d'urgence</div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="contactUrgenceNom">Nom</label>
              <input
                id="contactUrgenceNom"
                type="text"
                {...register('contactUrgenceNom')}
                placeholder="Marie Dupont"
              />
            </div>

            <div className="form-group">
              <label htmlFor="contactUrgenceTelephone">Téléphone</label>
              <input
                id="contactUrgenceTelephone"
                type="tel"
                {...register('contactUrgenceTelephone', {
                  pattern: {
                    value: /^[+]?[0-9]{8,15}$/,
                    message: 'Format de téléphone invalide',
                  },
                })}
                className={errors.contactUrgenceTelephone ? 'error' : ''}
                placeholder="+33987654321"
              />
              {errors.contactUrgenceTelephone && (
                <span className="error-message">{errors.contactUrgenceTelephone.message}</span>
              )}
            </div>
          </div>

          <div className="form-group">
            <label htmlFor="contactUrgenceRelation">Relation</label>
            <input
              id="contactUrgenceRelation"
              type="text"
              {...register('contactUrgenceRelation')}
              placeholder="Épouse, Père, Mère..."
            />
          </div>

          {error && <div className="error-alert">{error}</div>}

          <div className="modal-actions">
            <button type="button" onClick={onClose} className="btn-secondary">
              Annuler
            </button>
            <button type="submit" disabled={loading} className="btn-primary">
              {loading ? 'Enregistrement...' : mode === 'add' ? 'Créer' : 'Enregistrer'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default PatientModal

