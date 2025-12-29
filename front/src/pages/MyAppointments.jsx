import { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'
import { appointmentAPI } from '../services/appointmentApi'
import { doctorAPI } from '../services/doctorApi'
import AppointmentTable from '../components/appointments/AppointmentTable'
import AppointmentModal from '../components/appointments/AppointmentModal'
import AppointmentCalendar from '../components/appointments/AppointmentCalendar'
import Pagination from '../components/common/Pagination'
import './MyAppointments.css'

const MyAppointments = () => {
  const { user } = useAuth()
  const [appointments, setAppointments] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [viewMode, setViewMode] = useState('list') // 'list' ou 'calendar'
  const [calendarView, setCalendarView] = useState('week') // 'week' ou 'month'
  const [doctorId, setDoctorId] = useState(null)
  const [patientId, setPatientId] = useState(null)

  const [currentPage, setCurrentPage] = useState(0)
  const [pageSize, setPageSize] = useState(10)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)

  const [showAddModal, setShowAddModal] = useState(false)
  const [showEditModal, setShowEditModal] = useState(false)
  const [selectedAppointment, setSelectedAppointment] = useState(null)

  const [statusFilter, setStatusFilter] = useState('ALL')
  const [dateFilter, setDateFilter] = useState('') // 'upcoming', 'past', 'all'

  const userRole = user?.role || ''
  const userId = user?.id

  useEffect(() => {
    if (userId && userRole) {
      fetchUserProfileIds()
    }
  }, [userId, userRole])

  useEffect(() => {
    if (doctorId || patientId) {
      fetchAppointments()
    }
  }, [currentPage, pageSize, statusFilter, dateFilter, doctorId, patientId])

  const fetchUserProfileIds = async () => {
    try {
      if (userRole === 'DOCTOR') {
        const response = await doctorAPI.getDoctorByUserId(userId)
        setDoctorId(response.data.id)
      } else if (userRole === 'PATIENT') {
        setPatientId(userId)
      }
    } catch (err) {
      console.error('Error fetching user profile:', err)
    }
  }

  const fetchAppointments = async () => {
    if (!doctorId && !patientId) {
      return
    }

    setLoading(true)
    setError('')
    try {
      let response
      
      if (userRole === 'DOCTOR' && doctorId) {
        response = await appointmentAPI.getDoctorAppointmentsList(doctorId)
      } else if (userRole === 'PATIENT' && patientId) {
        response = await appointmentAPI.getPatientAppointmentsList(patientId)
      } else {
        setAppointments([])
        setTotalPages(0)
        setTotalElements(0)
        setLoading(false)
        return
      }
      
      let appointmentsData = response.data || []

      const today = new Date()
      today.setHours(0, 0, 0, 0)
      
      if (dateFilter === 'upcoming') {
        appointmentsData = appointmentsData.filter(apt => {
          const aptDate = new Date(apt.date)
          aptDate.setHours(0, 0, 0, 0)
          return aptDate >= today
        })
      } else if (dateFilter === 'past') {
        appointmentsData = appointmentsData.filter(apt => {
          const aptDate = new Date(apt.date)
          aptDate.setHours(0, 0, 0, 0)
          return aptDate < today
        })
      }

      if (statusFilter !== 'ALL') {
        appointmentsData = appointmentsData.filter(apt => apt.status === statusFilter)
      }

      appointmentsData.sort((a, b) => {
        const dateA = new Date(`${a.date}T${a.heure}`)
        const dateB = new Date(`${b.date}T${b.heure}`)
        return dateB - dateA
      })

      const startIndex = currentPage * pageSize
      const endIndex = startIndex + pageSize
      const paginatedData = appointmentsData.slice(startIndex, endIndex)
      
      setAppointments(paginatedData)
      setTotalElements(appointmentsData.length)
      setTotalPages(Math.ceil(appointmentsData.length / pageSize))
    } catch (err) {
      console.error('Error fetching appointments:', err)
      setError(err.response?.data?.message || 'Erreur lors du chargement des rendez-vous')
      setAppointments([])
    } finally {
      setLoading(false)
    }
  }

  const handleAdd = () => {
    setSelectedAppointment(null)
    setShowAddModal(true)
  }

  const handleEdit = (appointment) => {
    setSelectedAppointment(appointment)
    setShowEditModal(true)
  }

  const handleStatusChange = async (id, newStatus) => {
    try {
      await appointmentAPI.updateAppointmentStatus(id, newStatus)
      fetchAppointments()
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors de la mise à jour du statut')
    }
  }

  const handleReschedule = async (id, newDate, newHeure) => {
    try {
      await appointmentAPI.rescheduleAppointment(id, newDate, newHeure)
      fetchAppointments()
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors de la reprogrammation')
    }
  }

  const handleModalClose = () => {
    setShowAddModal(false)
    setShowEditModal(false)
    setSelectedAppointment(null)
  }

  const handleSaveSuccess = () => {
    handleModalClose()
    fetchAppointments()
  }

  const getUpcomingCount = () => {
    const today = new Date()
    today.setHours(0, 0, 0, 0)
    return appointments.filter(apt => {
      const aptDate = new Date(apt.date)
      aptDate.setHours(0, 0, 0, 0)
      return aptDate >= today && (apt.status === 'PENDING' || apt.status === 'CONFIRMED')
    }).length
  }

  return (
    <div className="my-appointments-container">
      <div className="my-appointments-header">
        <div>
          <h1>📅 Mes Rendez-vous</h1>
          <p className="subtitle">
            {userRole === 'DOCTOR' ? 'Gérez vos rendez-vous avec vos patients' : 'Consultez et gérez vos rendez-vous'}
          </p>
        </div>
        {userRole === 'PATIENT' && (
          <button onClick={handleAdd} className="btn-add">
            + Prendre rendez-vous
          </button>
        )}
      </div>

      <div className="stats-cards">
        <div className="stat-card">
          <div className="stat-value">{appointments.length}</div>
          <div className="stat-label">Total</div>
        </div>
        <div className="stat-card upcoming">
          <div className="stat-value">{getUpcomingCount()}</div>
          <div className="stat-label">À venir</div>
        </div>
        <div className="stat-card confirmed">
          <div className="stat-value">
            {appointments.filter(apt => apt.status === 'CONFIRMED').length}
          </div>
          <div className="stat-label">Confirmés</div>
        </div>
        <div className="stat-card pending">
          <div className="stat-value">
            {appointments.filter(apt => apt.status === 'PENDING').length}
          </div>
          <div className="stat-label">En attente</div>
        </div>
      </div>

      <div className="view-controls">
        <div className="view-toggle">
          <button
            className={viewMode === 'list' ? 'active' : ''}
            onClick={() => setViewMode('list')}
          >
            📋 Liste
          </button>
          <button
            className={viewMode === 'calendar' ? 'active' : ''}
            onClick={() => setViewMode('calendar')}
          >
            📆 Calendrier
          </button>
        </div>
      </div>

      <div className="appointments-filters">
        <div className="filter-group">
          <label>Période:</label>
          <select value={dateFilter} onChange={(e) => {
            setDateFilter(e.target.value)
            setCurrentPage(0)
          }}>
            <option value="all">Toutes les dates</option>
            <option value="upcoming">À venir</option>
            <option value="past">Passés</option>
          </select>
        </div>
        <div className="filter-group">
          <label>Statut:</label>
          <select value={statusFilter} onChange={(e) => {
            setStatusFilter(e.target.value)
            setCurrentPage(0)
          }}>
            <option value="ALL">Tous</option>
            <option value="PENDING">En attente</option>
            <option value="CONFIRMED">Confirmé</option>
            <option value="CANCELLED">Annulé</option>
            <option value="COMPLETED">Terminé</option>
            <option value="NO_SHOW">Absent</option>
          </select>
        </div>
        {viewMode === 'list' && (
          <div className="page-size-selector">
            <label>Par page:</label>
            <select value={pageSize} onChange={(e) => {
              setPageSize(Number(e.target.value))
              setCurrentPage(0)
            }}>
              <option value={5}>5</option>
              <option value={10}>10</option>
              <option value={20}>20</option>
              <option value={50}>50</option>
            </select>
          </div>
        )}
      </div>

      {error && (
        <div className="error-alert">
          {error}
          <button onClick={() => setError('')} className="close-error">×</button>
        </div>
      )}

      {loading ? (
        <div className="loading-container">
          <div className="spinner">Chargement...</div>
        </div>
      ) : (
        <>
          {viewMode === 'list' ? (
            <>
              <AppointmentTable
                appointments={appointments}
                onEdit={handleEdit}
                onStatusChange={handleStatusChange}
                onReschedule={handleReschedule}
                userRole={userRole}
              />

              {totalElements > 0 && (
                <Pagination
                  currentPage={currentPage}
                  totalPages={totalPages}
                  totalElements={totalElements}
                  pageSize={pageSize}
                  onPageChange={setCurrentPage}
                  itemLabel="rendez-vous"
                />
              )}

              {totalElements === 0 && !loading && (
                <div className="no-data">
                  Aucun rendez-vous trouvé avec ces filtres.
                </div>
              )}
            </>
          ) : (
            <AppointmentCalendar
              appointments={appointments}
              onEdit={handleEdit}
              onStatusChange={handleStatusChange}
              userRole={userRole}
              doctorId={doctorId}
              patientId={patientId}
            />
          )}
        </>
      )}

      {showAddModal && (
        <AppointmentModal
          mode="add"
          onClose={handleModalClose}
          onSave={handleSaveSuccess}
        />
      )}

      {showEditModal && selectedAppointment && (
        <AppointmentModal
          mode="edit"
          appointment={selectedAppointment}
          onClose={handleModalClose}
          onSave={handleSaveSuccess}
        />
      )}
    </div>
  )
}

export default MyAppointments

