import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { useAuth } from '../context/AuthContext'
import './Register.css'

const Register = () => {
  const navigate = useNavigate()
  const { register: registerUser } = useAuth()
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors },
  } = useForm()

  const password = watch('password')

  const onSubmit = async (data) => {
    setError('')
    setLoading(true)
    try {
      const result = await registerUser(data)
      if (result.success) {
        navigate('/login', {
          state: { message: 'Inscription réussie ! Connectez-vous maintenant.' },
        })
      } else {
        setError(result.error)
      }
    } catch (err) {
      setError('Une erreur est survenue. Veuillez réessayer.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="register-container">
      <div className="register-card">
        <h1>Inscription</h1>
        <form onSubmit={handleSubmit(onSubmit)}>
          <div className="form-group">
            <label htmlFor="email">Email</label>
            <input
              id="email"
              type="email"
              {...register('email', {
                required: 'L\'email est obligatoire',
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
            <label htmlFor="password">Mot de passe</label>
            <input
              id="password"
              type="password"
              {...register('password', {
                required: 'Le mot de passe est obligatoire',
                minLength: {
                  value: 6,
                  message: 'Le mot de passe doit contenir au moins 6 caractères',
                },
              })}
              className={errors.password ? 'error' : ''}
            />
            {errors.password && (
              <span className="error-message">{errors.password.message}</span>
            )}
          </div>

          <div className="form-group">
            <label htmlFor="firstName">Prénom</label>
            <input
              id="firstName"
              type="text"
              {...register('firstName', {
                required: 'Le prénom est obligatoire',
              })}
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
              {...register('lastName', {
                required: 'Le nom est obligatoire',
              })}
              className={errors.lastName ? 'error' : ''}
            />
            {errors.lastName && (
              <span className="error-message">{errors.lastName.message}</span>
            )}
          </div>

          <div className="form-group">
            <label htmlFor="phoneNumber">Numéro de téléphone (optionnel)</label>
            <input
              id="phoneNumber"
              type="tel"
              {...register('phoneNumber')}
            />
          </div>

          <div className="form-group">
            <label htmlFor="role">Rôle</label>
            <select
              id="role"
              {...register('role', {
                required: 'Le rôle est obligatoire',
              })}
              className={errors.role ? 'error' : ''}
            >
              <option value="">Sélectionner un rôle</option>
              <option value="PATIENT">Patient</option>
              <option value="DOCTOR">Médecin</option>
              <option value="ADMIN">Administrateur</option>
            </select>
            {errors.role && (
              <span className="error-message">{errors.role.message}</span>
            )}
          </div>

          {error && <div className="error-alert">{error}</div>}

          <button type="submit" disabled={loading} className="btn-primary">
            {loading ? 'Inscription...' : 'S\'inscrire'}
          </button>
        </form>

        <p className="login-link">
          Déjà un compte ? <Link to="/login">Se connecter</Link>
        </p>
      </div>
    </div>
  )
}

export default Register

