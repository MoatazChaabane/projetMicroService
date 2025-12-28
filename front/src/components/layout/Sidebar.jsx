import { NavLink } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import './Sidebar.css'

const Sidebar = ({ className = '' }) => {
  const { user } = useAuth()
  const userRole = user?.role || ''

  const menuItems = [
    {
      path: '/dashboard',
      icon: '📊',
      label: 'Tableau de bord',
      roles: ['ADMIN', 'DOCTOR', 'PATIENT']
    },
    {
      path: '/doctors',
      icon: '👨‍⚕️',
      label: 'Docteurs',
      roles: ['ADMIN', 'DOCTOR', 'PATIENT']
    },
    {
      path: '/patients',
      icon: '👥',
      label: 'Patients',
      roles: ['ADMIN', 'DOCTOR']
    },
    {
      path: '/appointments',
      icon: '📅',
      label: 'Rendez-vous',
      roles: ['ADMIN']
    },
    {
      path: '/my-appointments',
      icon: '📅',
      label: 'Mes Rendez-vous',
      roles: ['DOCTOR', 'PATIENT']
    },
    {
      path: '/prescriptions',
      icon: '📋',
      label: 'Ordonnances',
      roles: ['ADMIN', 'DOCTOR', 'PATIENT']
    },
    {
      path: '/medical-record',
      icon: '📁',
      label: 'Dossier Médical',
      roles: ['ADMIN', 'DOCTOR', 'PATIENT']
    },
    {
      path: '/find-doctor',
      icon: '🔍',
      label: 'Trouver un Docteur',
      roles: ['PATIENT']
    },
    {
      path: '/profile',
      icon: '👤',
      label: 'Mon Profil',
      roles: ['ADMIN', 'DOCTOR', 'PATIENT']
    }
  ]

  const filteredMenuItems = menuItems.filter(item => 
    item.roles.includes(userRole)
  )

  return (
    <aside className={`sidebar ${className}`}>
      <div className="sidebar-header">
        <div className="sidebar-logo">
          <span className="logo-icon">🏥</span>
          <span className="logo-text">MediCare</span>
        </div>
      </div>

      <nav className="sidebar-nav">
        <ul className="nav-menu">
          {filteredMenuItems.map((item) => (
            <li key={item.path}>
              <NavLink
                to={item.path}
                className={({ isActive }) =>
                  `nav-item ${isActive ? 'active' : ''}`
                }
              >
                <span className="nav-icon">{item.icon}</span>
                <span className="nav-label">{item.label}</span>
              </NavLink>
            </li>
          ))}
        </ul>
      </nav>

      <div className="sidebar-footer">
        <div className="sidebar-info">
          <p className="app-version">v1.0.0</p>
          <p className="app-tagline">Système de gestion médicale</p>
        </div>
      </div>
    </aside>
  )
}

export default Sidebar

