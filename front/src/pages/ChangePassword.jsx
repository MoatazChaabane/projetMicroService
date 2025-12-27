import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { profileAPI } from '../services/api'
import './ChangePassword.css'

const ChangePassword = () => {
  const navigate = useNavigate()
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [loading, setLoading] = useState(false)

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors },
  } = useForm()

  const newPassword = watch('newPassword')

  const onSubmit = async (data) => {
    setError('')
    setSuccess('')
    setLoading(true)
    try {
      await profileAPI.changePassword({
        currentPassword: data.currentPassword,
        newPassword: data.newPassword,
      })
      setSuccess('Mot de passe modifié avec succès !')
      setTimeout(() => {
        navigate('/profile')
      }, 1500)
    } catch (err) {
      setError(
        err.response?.data?.message || 'Erreur lors du changement de mot de passe'
      )
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="change-password-container">
      <div className="change-password-card">
        <h1>Changer le mot de passe</h1>
        <form onSubmit={handleSubmit(onSubmit)}>
          <div className="form-group">
            <label htmlFor="currentPassword">Mot de passe actuel</label>
            <input
              id="currentPassword"
              type="password"
              {...register('currentPassword', {
                required: 'Le mot de passe actuel est obligatoire',
              })}
              className={errors.currentPassword ? 'error' : ''}
            />
            {errors.currentPassword && (
              <span className="error-message">
                {errors.currentPassword.message}
              </span>
            )}
          </div>

          <div className="form-group">
            <label htmlFor="newPassword">Nouveau mot de passe</label>
            <input
              id="newPassword"
              type="password"
              {...register('newPassword', {
                required: 'Le nouveau mot de passe est obligatoire',
                minLength: {
                  value: 6,
                  message: 'Le mot de passe doit contenir au moins 6 caractères',
                },
              })}
              className={errors.newPassword ? 'error' : ''}
            />
            {errors.newPassword && (
              <span className="error-message">{errors.newPassword.message}</span>
            )}
          </div>

          <div className="form-group">
            <label htmlFor="confirmPassword">Confirmer le nouveau mot de passe</label>
            <input
              id="confirmPassword"
              type="password"
              {...register('confirmPassword', {
                required: 'La confirmation est obligatoire',
                validate: (value) =>
                  value === newPassword || 'Les mots de passe ne correspondent pas',
              })}
              className={errors.confirmPassword ? 'error' : ''}
            />
            {errors.confirmPassword && (
              <span className="error-message">
                {errors.confirmPassword.message}
              </span>
            )}
          </div>

          {error && <div className="error-alert">{error}</div>}
          {success && <div className="success-alert">{success}</div>}

          <div className="form-actions">
            <button type="submit" disabled={loading} className="btn-primary">
              {loading ? 'Modification...' : 'Modifier le mot de passe'}
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

export default ChangePassword

