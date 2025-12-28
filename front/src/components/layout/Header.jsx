import { useAuth } from '../../context/AuthContext'
import './Header.css'

const Header = ({ onMenuToggle }) => {
  const { user, logout } = useAuth()

  const getRoleLabel = (role) => {
    const roles = {
      'ADMIN': 'Administrateur',
      'DOCTOR': 'Médecin',
      'PATIENT': 'Patient'
    }
    return roles[role] || role
  }

  const getRoleColor = (role) => {
    const colors = {
      'ADMIN': '#E53935',
      'DOCTOR': '#43A047',
      'PATIENT': '#1E88E5'
    }
    return colors[role] || '#757575'
  }

  return (
    <header className="app-header">
      <div className="header-left">
        <button className="menu-toggle" onClick={onMenuToggle}>
          ☰
        </button>
        <h1 className="page-title">Système de Gestion Médicale</h1>
      </div>

      <div className="header-right">
        <div className="user-info">
          <div className="user-details">
            <span className="user-name">
              {user?.firstName} {user?.lastName}
            </span>
            <span 
              className="user-role"
              style={{ color: getRoleColor(user?.role) }}
            >
              {getRoleLabel(user?.role)}
            </span>
          </div>
          <div className="user-avatar">
            {user?.firstName?.[0]}{user?.lastName?.[0]}
          </div>
        </div>
        <button className="btn-logout" onClick={logout}>
          <span className="logout-text">🚪 Déconnexion</span>
        </button>
      </div>
    </header>
  )
}

export default Header

