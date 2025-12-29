import { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'
import { useNavigate } from 'react-router-dom'
import { doctorAPI } from '../services/doctorApi'
import { patientAPI } from '../services/patientApi'
import { appointmentAPI } from '../services/appointmentApi'
import { prescriptionAPI } from '../services/prescriptionApi'
import { medicalRecordAPI } from '../services/medicalRecordApi'
import './Dashboard.css'

const Dashboard = () => {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [stats, setStats] = useState({
    totalDoctors: 0,
    totalPatients: 0,
    totalAppointments: 0,
    upcomingAppointments: 0,
    pendingAppointments: 0,
    totalPrescriptions: 0,
    totalMedicalRecords: 0,
    myAppointments: 0,
    myPrescriptions: 0,
    myMedicalRecords: 0
  })
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    fetchStats()
  }, [user])

  const fetchStats = async () => {
    setLoading(true)
    try {
      const role = user?.role

      if (role === 'ADMIN') {

        const [doctorsRes, patientsRes, appointmentsRes, prescriptionsRes, medicalRecordsRes] = await Promise.allSettled([
          doctorAPI.countDoctors(),
          patientAPI.countPatients(),
          appointmentAPI.getAllAppointments({ page: 0, size: 1 }),
          prescriptionAPI.getAllPrescriptions(),
          Promise.resolve({ data: { total: 0 } }) // TODO: Ajouter count medical records si endpoint existe
        ])

        const appointments = appointmentsRes.status === 'fulfilled' ? appointmentsRes.value.data : { totalElements: 0, content: [] }
        const prescriptions = prescriptionsRes.status === 'fulfilled' ? prescriptionsRes.value.data : []

        setStats({
          totalDoctors: doctorsRes.status === 'fulfilled' ? doctorsRes.value.data : 0,
          totalPatients: patientsRes.status === 'fulfilled' ? patientsRes.value.data : 0,
          totalAppointments: appointments.totalElements || appointments.length || 0,
          upcomingAppointments: 0,
          pendingAppointments: 0,
          totalPrescriptions: Array.isArray(prescriptions) ? prescriptions.length : 0,
          totalMedicalRecords: 0,
          myAppointments: 0,
          myPrescriptions: 0,
          myMedicalRecords: 0
        })
      } else if (role === 'DOCTOR') {

        const doctorId = await getDoctorId()
        if (doctorId) {
          const [appointmentsRes, prescriptionsRes] = await Promise.allSettled([
            appointmentAPI.getDoctorAppointments(doctorId, { page: 0, size: 1000 }),
            prescriptionAPI.getDoctorPrescriptions(doctorId, 0, 1000)
          ])

          const appointments = appointmentsRes.status === 'fulfilled' ? appointmentsRes.value.data.content || appointmentsRes.value.data : []
          const prescriptions = prescriptionsRes.status === 'fulfilled' ? prescriptionsRes.value.data.content || prescriptionsRes.value.data : []

          const today = new Date()
          const upcoming = appointments.filter(apt => {
            const aptDate = new Date(apt.date + 'T' + apt.heure)
            return aptDate >= today && apt.status === 'CONFIRMED'
          })
          const pending = appointments.filter(apt => apt.status === 'PENDING')

          setStats({
            totalDoctors: 0,
            totalPatients: 0,
            totalAppointments: appointments.length,
            upcomingAppointments: upcoming.length,
            pendingAppointments: pending.length,
            totalPrescriptions: Array.isArray(prescriptions) ? prescriptions.length : 0,
            totalMedicalRecords: 0,
            myAppointments: appointments.length,
            myPrescriptions: Array.isArray(prescriptions) ? prescriptions.length : 0,
            myMedicalRecords: 0
          })
        }
      } else if (role === 'PATIENT') {

        const patientId = user?.id
        if (patientId) {
          const [appointmentsRes, prescriptionsRes, medicalRecordRes] = await Promise.allSettled([
            appointmentAPI.getPatientAppointments(patientId, { page: 0, size: 1000 }),
            prescriptionAPI.getPatientPrescriptions(patientId, 0, 1000),
            medicalRecordAPI.getMedicalRecordByPatientId(patientId).catch(() => null)
          ])

          const appointments = appointmentsRes.status === 'fulfilled' ? appointmentsRes.value.data.content || appointmentsRes.value.data : []
          const prescriptions = prescriptionsRes.status === 'fulfilled' ? prescriptionsRes.value.data.content || prescriptionsRes.value.data : []

          const today = new Date()
          const upcoming = appointments.filter(apt => {
            const aptDate = new Date(apt.date + 'T' + apt.heure)
            return aptDate >= today && apt.status === 'CONFIRMED'
          })

          setStats({
            totalDoctors: 0,
            totalPatients: 0,
            totalAppointments: appointments.length,
            upcomingAppointments: upcoming.length,
            pendingAppointments: 0,
            totalPrescriptions: Array.isArray(prescriptions) ? prescriptions.length : 0,
            totalMedicalRecords: medicalRecordRes.status === 'fulfilled' && medicalRecordRes.value ? 1 : 0,
            myAppointments: appointments.length,
            myPrescriptions: Array.isArray(prescriptions) ? prescriptions.length : 0,
            myMedicalRecords: medicalRecordRes.status === 'fulfilled' && medicalRecordRes.value ? 1 : 0
          })
        }
      }
    } catch (error) {
      console.error('Erreur lors de la récupération des statistiques:', error)
    } finally {
      setLoading(false)
    }
  }

  const getDoctorId = async () => {
    try {
      const response = await doctorAPI.getDoctorByUserId(user.id)
      return response.data.id
    } catch (err) {
      console.error('Erreur lors de la récupération du doctorId:', err)
      return null
    }
  }

  if (loading) {
    return (
      <div className="dashboard-container">
        <div className="loading-spinner">Chargement...</div>
      </div>
    )
  }

  const role = user?.role

  return (
    <div className="dashboard-container">
      <h1>📊 Tableau de bord</h1>
      <p className="welcome-message">Bienvenue, {user?.firstName} {user?.lastName} !</p>

      {role === 'ADMIN' && (
        <div className="dashboard-grid">
          <div className="stat-card primary">
            <div className="stat-icon">👨‍⚕️</div>
            <div className="stat-content">
              <h3>Docteurs</h3>
              <p className="stat-value">{stats.totalDoctors}</p>
              <button onClick={() => navigate('/doctors')} className="stat-link">
                Voir tous →
              </button>
            </div>
          </div>

          <div className="stat-card primary">
            <div className="stat-icon">👥</div>
            <div className="stat-content">
              <h3>Patients</h3>
              <p className="stat-value">{stats.totalPatients}</p>
              <button onClick={() => navigate('/patients')} className="stat-link">
                Voir tous →
              </button>
            </div>
          </div>

          <div className="stat-card info">
            <div className="stat-icon">📅</div>
            <div className="stat-content">
              <h3>Rendez-vous</h3>
              <p className="stat-value">{stats.totalAppointments}</p>
              <button onClick={() => navigate('/appointments')} className="stat-link">
                Gérer →
              </button>
            </div>
          </div>

          <div className="stat-card success">
            <div className="stat-icon">📋</div>
            <div className="stat-content">
              <h3>Ordonnances</h3>
              <p className="stat-value">{stats.totalPrescriptions}</p>
              <button onClick={() => navigate('/prescriptions')} className="stat-link">
                Voir toutes →
              </button>
            </div>
          </div>
        </div>
      )}

      {role === 'DOCTOR' && (
        <div className="dashboard-grid">
          <div className="stat-card info">
            <div className="stat-icon">📅</div>
            <div className="stat-content">
              <h3>Mes Rendez-vous</h3>
              <p className="stat-value">{stats.myAppointments}</p>
              <div className="stat-details">
                <span>À venir: {stats.upcomingAppointments}</span>
                <span>En attente: {stats.pendingAppointments}</span>
              </div>
              <button onClick={() => navigate('/my-appointments')} className="stat-link">
                Voir mes RDV →
              </button>
            </div>
          </div>

          <div className="stat-card success">
            <div className="stat-icon">📋</div>
            <div className="stat-content">
              <h3>Mes Ordonnances</h3>
              <p className="stat-value">{stats.myPrescriptions}</p>
              <button onClick={() => navigate('/prescriptions')} className="stat-link">
                Voir mes ordonnances →
              </button>
            </div>
          </div>

          <div className="stat-card primary">
            <div className="stat-icon">👥</div>
            <div className="stat-content">
              <h3>Mes Patients</h3>
              <p className="stat-value">{stats.totalPatients}</p>
              <button onClick={() => navigate('/patients')} className="stat-link">
                Voir mes patients →
              </button>
            </div>
          </div>

          <div className="stat-card warning">
            <div className="stat-icon">📁</div>
            <div className="stat-content">
              <h3>Dossiers Médicaux</h3>
              <p className="stat-value">{stats.myMedicalRecords}</p>
              <button onClick={() => navigate('/medical-record')} className="stat-link">
                Gérer les dossiers →
              </button>
            </div>
          </div>
        </div>
      )}

      {role === 'PATIENT' && (
        <div className="dashboard-grid">
          <div className="stat-card info">
            <div className="stat-icon">📅</div>
            <div className="stat-content">
              <h3>Mes Rendez-vous</h3>
              <p className="stat-value">{stats.myAppointments}</p>
              <div className="stat-details">
                <span>À venir: {stats.upcomingAppointments}</span>
              </div>
              <button onClick={() => navigate('/my-appointments')} className="stat-link">
                Voir mes RDV →
              </button>
            </div>
          </div>

          <div className="stat-card success">
            <div className="stat-icon">📋</div>
            <div className="stat-content">
              <h3>Mes Ordonnances</h3>
              <p className="stat-value">{stats.myPrescriptions}</p>
              <button onClick={() => navigate('/prescriptions')} className="stat-link">
                Voir mes ordonnances →
              </button>
            </div>
          </div>

          <div className="stat-card primary">
            <div className="stat-icon">📁</div>
            <div className="stat-content">
              <h3>Mon Dossier Médical</h3>
              <p className="stat-value">{stats.myMedicalRecords > 0 ? '✓' : '✗'}</p>
              <button onClick={() => navigate('/medical-record')} className="stat-link">
                {stats.myMedicalRecords > 0 ? 'Consulter →' : 'Créer →'}
              </button>
            </div>
          </div>

          <div className="stat-card warning">
            <div className="stat-icon">🔍</div>
            <div className="stat-content">
              <h3>Trouver un Docteur</h3>
              <p className="stat-value">-</p>
              <button onClick={() => navigate('/find-doctor')} className="stat-link">
                Rechercher →
              </button>
            </div>
          </div>
        </div>
      )}

      <div className="quick-actions">
        <h2>Actions rapides</h2>
        <div className="actions-grid">
          {role === 'ADMIN' && (
            <>
              <button onClick={() => navigate('/doctors')} className="action-btn">
                + Ajouter un docteur
              </button>
              <button onClick={() => navigate('/patients')} className="action-btn">
                + Ajouter un patient
              </button>
              <button onClick={() => navigate('/appointments')} className="action-btn">
                + Nouveau rendez-vous
              </button>
            </>
          )}
          {role === 'DOCTOR' && (
            <>
              <button onClick={() => navigate('/my-appointments')} className="action-btn">
                + Nouveau rendez-vous
              </button>
              <button onClick={() => navigate('/prescriptions')} className="action-btn">
                + Nouvelle ordonnance
              </button>
              <button onClick={() => navigate('/medical-record')} className="action-btn">
                + Ajouter consultation
              </button>
            </>
          )}
          {role === 'PATIENT' && (
            <>
              <button onClick={() => navigate('/find-doctor')} className="action-btn">
                🔍 Trouver un docteur
              </button>
              <button onClick={() => navigate('/my-appointments')} className="action-btn">
                📅 Mes rendez-vous
              </button>
              <button onClick={() => navigate('/prescriptions')} className="action-btn">
                📋 Mes ordonnances
              </button>
            </>
          )}
        </div>
      </div>
    </div>
  )
}

export default Dashboard
