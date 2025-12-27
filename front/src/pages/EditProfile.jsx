import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { useAuth } from '../context/AuthContext'
import { profileAPI } from '../services/api'
import './EditProfile.css'

const EditProfile = () => {
  const navigate = useNavigate()
  const { user, updateUser } = useAuth()
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [loading, setLoading] = useState(false)

  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
  } = useForm({
    defaultValues: {
      email: user?.email || '',
      firstName: user?.firstName || '',
      lastName: user?.lastName || '',
      phoneNumber: user?.phoneNumber || '',
    },
  })

  useEffect(() => {
    if (user) {
      reset({
        email: user.email || '',
        firstName: user.firstName || '',
        lastName: user.lastName || '',
        phoneNumber: user.phoneNumber || '',
      })
    }
  }, [user, reset])

  const onSubmit = async (data) => {
    setError('')
    setSuccess('')
    setLoading(true)
    try {
      const response = await profileAPI.updateProfile(data)
      updateUser(response.data.user)
      setSuccess('Profil mis à jour avec succès !')
      setTimeout(() => {
        navigate('/profile')
      }, 1500)
    } catch (err) {
      setError(
        err.response?.data?.message || 'Erreur lors de la mise à jour du profil'
      )
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="edit-profile-container">
      <div className="edit-profile-card">
        <h1>Modifier le profil</h1>
        <form onSubmit={handleSubmit(onSubmit)}>
          <div className="form-group">
            <label htmlFor="email">Email</label>
            <input
              id="email"
              type="email"
              {...register('email', {
                pattern: {
                  value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i,
                  message: 'Email invalide',
                },
              })}
              className={errors.email ? 'error' : ''}
            />
            {errors.email && (
              <span className="error-message">{errors.email.message}</span>
            )}
          </div>

          <div className="form-group">
            <label htmlFor="firstName">Prénom</label>
            <input
              id="firstName"
              type="text"
              {...register('firstName')}
              className={errors.firstName ? 'error' : ''}
            />
            {errors.firstName && (
              <span className="error-message">{errors.firstName.message}</span>
            )}
          </div>

          <div className="form-group">
            <label htmlFor="lastName">Nom</label>
            <input
              id="lastName"
              type="text"
              {...register('lastName')}
              className={errors.lastName ? 'error' : ''}
            />
            {errors.lastName && (
              <span className="error-message">{errors.lastName.message}</span>
            )}
          </div>

          <div className="form-group">
            <label htmlFor="phoneNumber">Numéro de téléphone</label>
            <input
              id="phoneNumber"
              type="tel"
              {...register('phoneNumber')}
            />
          </div>

          {error && <div className="error-alert">{error}</div>}
          {success && <div className="success-alert">{success}</div>}

          <div className="form-actions">
            <button type="submit" disabled={loading} className="btn-primary">
              {loading ? 'Enregistrement...' : 'Enregistrer'}
            </button>
            <button
              type="button"
              onClick={() => navigate('/profile')}
              className="btn-secondary"
            >
              Annuler
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default EditProfile

