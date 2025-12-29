import { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'
import { appointmentAPI } from '../services/appointmentApi'
import { doctorAPI } from '../services/doctorApi'
import AppointmentTable from '../components/appointments/AppointmentTable'
import AppointmentModal from '../components/appointments/AppointmentModal'
import AppointmentCalendar from '../components/appointments/AppointmentCalendar'
import Pagination from '../components/common/Pagination'
import './Appointments.css'

const Appointments = () => {
  const { user } = useAuth()
  const [appointments, setAppointments] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [viewMode, setViewMode] = useState('list') // 'list' ou 'calendar'
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

  const userRole = user?.role || ''
  const userId = user?.id

  useEffect(() => {
    if (userId && userRole) {
      fetchUserProfileIds()
    }
  }, [userId, userRole])

  useEffect(() => {
    if (doctorId || patientId || userRole === 'ADMIN') {
      fetchAppointments()
    }
  }, [currentPage, pageSize, statusFilter, doctorId, patientId, userRole])

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
    if (!doctorId && !patientId && userRole !== 'ADMIN') {
      return
    }

    setLoading(true)
    setError('')
    try {
      let response
      
      if (userRole === 'DOCTOR' && doctorId) {
        response = await appointmentAPI.getDoctorAppointments(doctorId, {
          page: currentPage,
          size: pageSize
        })
      } else if (userRole === 'PATIENT' && patientId) {
        response = await appointmentAPI.getPatientAppointments(patientId, {
          page: currentPage,
          size: pageSize
        })
      } else if (userRole === 'ADMIN') {


        if (doctorId) {
          response = await appointmentAPI.getDoctorAppointments(doctorId, {
            page: currentPage,
            size: pageSize
          })
        } else {

          const doctorsRes = await doctorAPI.getAllDoctors({ page: 0, size: 1 })
          if (doctorsRes.data.content && doctorsRes.data.content.length > 0) {
            const firstDoctorId = doctorsRes.data.content[0].id
            response = await appointmentAPI.getDoctorAppointments(firstDoctorId, {
              page: currentPage,
              size: pageSize
            })
          } else {
            setAppointments([])
            setTotalPages(0)
            setTotalElements(0)
            setLoading(false)
            return
          }
        }
      } else {
        setAppointments([])
        setTotalPages(0)
        setTotalElements(0)
        setLoading(false)
        return
      }
      
      let appointmentsData = response.data.content || []

      if (statusFilter !== 'ALL') {
        appointmentsData = appointmentsData.filter(apt => apt.status === statusFilter)

        setTotalElements(appointmentsData.length)
      } else {
        setTotalElements(response.data.totalElements || 0)
      }
      
      setAppointments(appointmentsData)
      setTotalPages(response.data.totalPages || 0)
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

  return (
    <div className="appointments-container">
      <div className="appointments-header">
        <h1>📅 Gestion des Rendez-vous</h1>
        <div className="header-actions">
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
          {(userRole === 'ADMIN' || userRole === 'PATIENT') && (
            <button onClick={handleAdd} className="btn-add">
              + Nouveau rendez-vous
            </button>
          )}
        </div>
      </div>

      <div className="appointments-filters">
        <div className="filter-group">
          <label>Statut:</label>
          <select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
            <option value="ALL">Tous</option>
            <option value="PENDING">En attente</option>
            <option value="CONFIRMED">Confirmé</option>
            <option value="CANCELLED">Annulé</option>
            <option value="COMPLETED">Terminé</option>
            <option value="NO_SHOW">Absent</option>
          </select>
        </div>
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
                  Aucun rendez-vous trouvé.
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

export default Appointments
