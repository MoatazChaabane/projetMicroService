import { useState, useEffect } from 'react'
import { appointmentAPI } from '../../services/appointmentApi'
import './AppointmentCalendar.css'

const STATUS_COLORS = {
  PENDING: '#FB8C00',
  CONFIRMED: '#43A047',
  CANCELLED: '#E53935',
  COMPLETED: '#1E88E5',
  NO_SHOW: '#757575'
}

const AppointmentCalendar = ({ appointments, onEdit, onStatusChange, userRole, doctorId, patientId }) => {
  const [currentDate, setCurrentDate] = useState(new Date())
  const [viewMode, setViewMode] = useState('week') // 'week' ou 'month'
  const [monthAppointments, setMonthAppointments] = useState([])

  useEffect(() => {
    if (viewMode === 'month' && doctorId) {
      fetchMonthAppointments()
    }
  }, [viewMode, currentDate, doctorId])

  const fetchMonthAppointments = async () => {
    try {
      const year = currentDate.getFullYear()
      const month = currentDate.getMonth()
      const firstDay = new Date(year, month, 1)
      const response = await appointmentAPI.getDoctorAppointmentsByWeek(doctorId, firstDay.toISOString().split('T')[0])
      setMonthAppointments(response.data || [])
    } catch (err) {
      console.error('Error fetching month appointments:', err)
      setMonthAppointments([])
    }
  }

  const getStartOfWeek = (date) => {
    const d = new Date(date)
    const day = d.getDay()
    const diff = d.getDate() - day + (day === 0 ? -6 : 1) // Ajuster pour lundi
    return new Date(d.setDate(diff))
  }

  const getWeekDays = () => {
    const start = getStartOfWeek(currentDate)
    const days = []
    for (let i = 0; i < 7; i++) {
      const day = new Date(start)
      day.setDate(start.getDate() + i)
      days.push(day)
    }
    return days
  }

  const getMonthDays = () => {
    const year = currentDate.getFullYear()
    const month = currentDate.getMonth()
    const firstDay = new Date(year, month, 1)
    const lastDay = new Date(year, month + 1, 0)
    const daysInMonth = lastDay.getDate()
    const startingDayOfWeek = firstDay.getDay() === 0 ? 6 : firstDay.getDay() - 1 // Lundi = 0
    
    const days = []
    
    // Jours du mois précédent
    const prevMonth = new Date(year, month - 1, 0)
    for (let i = startingDayOfWeek - 1; i >= 0; i--) {
      days.push({
        date: new Date(year, month - 1, prevMonth.getDate() - i),
        isCurrentMonth: false
      })
    }
    
    // Jours du mois actuel
    for (let i = 1; i <= daysInMonth; i++) {
      days.push({
        date: new Date(year, month, i),
        isCurrentMonth: true
      })
    }
    
    // Jours du mois suivant pour compléter la grille
    const remainingDays = 42 - days.length // 6 semaines * 7 jours
    for (let i = 1; i <= remainingDays; i++) {
      days.push({
        date: new Date(year, month + 1, i),
        isCurrentMonth: false
      })
    }
    
    return days
  }

  const getAppointmentsForDay = (date) => {
    const dateStr = date.toISOString().split('T')[0]
    const allAppointments = viewMode === 'month' ? monthAppointments : appointments
    return allAppointments.filter(apt => apt.date === dateStr)
  }

  const formatTime = (timeString) => {
    if (!timeString) return ''
    return timeString.substring(0, 5)
  }

  const previousPeriod = () => {
    const newDate = new Date(currentDate)
    if (viewMode === 'week') {
      newDate.setDate(newDate.getDate() - 7)
    } else {
      newDate.setMonth(newDate.getMonth() - 1)
    }
    setCurrentDate(newDate)
  }

  const nextPeriod = () => {
    const newDate = new Date(currentDate)
    if (viewMode === 'week') {
      newDate.setDate(newDate.getDate() + 7)
    } else {
      newDate.setMonth(newDate.getMonth() + 1)
    }
    setCurrentDate(newDate)
  }

  const goToToday = () => {
    setCurrentDate(new Date())
  }

  const getMonthName = () => {
    return currentDate.toLocaleDateString('fr-FR', { month: 'long', year: 'numeric' })
  }

  const weekDays = getWeekDays()
  const monthDays = getMonthDays()
  const dayNames = ['Lun', 'Mar', 'Mer', 'Jeu', 'Ven', 'Sam', 'Dim']

  return (
    <div className="calendar-container">
      <div className="calendar-header">
        <div className="calendar-controls">
          <button onClick={previousPeriod} className="btn-nav">‹ {viewMode === 'week' ? 'Précédent' : 'Mois précédent'}</button>
          <button onClick={goToToday} className="btn-today">Aujourd'hui</button>
          <button onClick={nextPeriod} className="btn-nav">{viewMode === 'week' ? 'Suivant' : 'Mois suivant'} ›</button>
        </div>
        
        <div className="calendar-title">
          {viewMode === 'week' ? (
            <h3>
              Semaine du {weekDays[0].toLocaleDateString('fr-FR', { day: 'numeric', month: 'long' })} au{' '}
              {weekDays[6].toLocaleDateString('fr-FR', { day: 'numeric', month: 'long', year: 'numeric' })}
            </h3>
          ) : (
            <h3>{getMonthName()}</h3>
          )}
        </div>

        <div className="view-mode-toggle">
          <button
            className={viewMode === 'week' ? 'active' : ''}
            onClick={() => setViewMode('week')}
          >
            Semaine
          </button>
          <button
            className={viewMode === 'month' ? 'active' : ''}
            onClick={() => setViewMode('month')}
          >
            Mois
          </button>
        </div>
      </div>

      {viewMode === 'week' ? (
        <div className="calendar-grid week-view">
          {weekDays.map((day, index) => {
            const dayAppointments = getAppointmentsForDay(day)
            const isToday = day.toDateString() === new Date().toDateString()

            return (
              <div key={index} className={`calendar-day ${isToday ? 'today' : ''}`}>
                <div className="day-header">
                  <div className="day-name">{dayNames[index]}</div>
                  <div className="day-number">{day.getDate()}</div>
                </div>
                <div className="day-appointments">
                  {dayAppointments.length === 0 ? (
                    <div className="no-appointments">Aucun rendez-vous</div>
                  ) : (
                    dayAppointments.map(apt => (
                      <div
                        key={apt.id}
                        className="appointment-item"
                        style={{ borderLeftColor: STATUS_COLORS[apt.status] || '#616161' }}
                        onClick={() => onEdit(apt)}
                      >
                        <div className="appointment-time">{formatTime(apt.heure)}</div>
                        <div className="appointment-info">
                          <div className="appointment-name">
                            {userRole === 'DOCTOR' ? apt.patientNomComplet : apt.doctorNomComplet}
                          </div>
                          {apt.motif && (
                            <div className="appointment-motif">{apt.motif}</div>
                          )}
                        </div>
                        <div
                          className="appointment-status-dot"
                          style={{ backgroundColor: STATUS_COLORS[apt.status] || '#616161' }}
                        />
                      </div>
                    ))
                  )}
                </div>
              </div>
            )
          })}
        </div>
      ) : (
        <div className="calendar-grid month-view">
          <div className="month-header">
            {dayNames.map(dayName => (
              <div key={dayName} className="month-day-header">{dayName}</div>
            ))}
          </div>
          <div className="month-days">
            {monthDays.map((dayObj, index) => {
              const dayAppointments = getAppointmentsForDay(dayObj.date)
              const isToday = dayObj.date.toDateString() === new Date().toDateString()
              const isCurrentMonth = dayObj.isCurrentMonth

              return (
                <div
                  key={index}
                  className={`month-day ${isToday ? 'today' : ''} ${!isCurrentMonth ? 'other-month' : ''}`}
                >
                  <div className="day-number">{dayObj.date.getDate()}</div>
                  <div className="day-appointments-month">
                    {dayAppointments.slice(0, 3).map(apt => (
                      <div
                        key={apt.id}
                        className="appointment-item-month"
                        style={{ backgroundColor: STATUS_COLORS[apt.status] || '#616161' }}
                        onClick={() => onEdit(apt)}
                        title={`${formatTime(apt.heure)} - ${userRole === 'DOCTOR' ? apt.patientNomComplet : apt.doctorNomComplet}`}
                      >
                        {formatTime(apt.heure)}
                      </div>
                    ))}
                    {dayAppointments.length > 3 && (
                      <div className="more-appointments">
                        +{dayAppointments.length - 3}
                      </div>
                    )}
                  </div>
                </div>
              )
            })}
          </div>
        </div>
      )}

      <div className="calendar-legend">
        <div className="legend-item">
          <span className="legend-dot" style={{ backgroundColor: STATUS_COLORS.PENDING }} />
          <span>En attente</span>
        </div>
        <div className="legend-item">
          <span className="legend-dot" style={{ backgroundColor: STATUS_COLORS.CONFIRMED }} />
          <span>Confirmé</span>
        </div>
        <div className="legend-item">
          <span className="legend-dot" style={{ backgroundColor: STATUS_COLORS.COMPLETED }} />
          <span>Terminé</span>
        </div>
        <div className="legend-item">
          <span className="legend-dot" style={{ backgroundColor: STATUS_COLORS.CANCELLED }} />
          <span>Annulé</span>
        </div>
        <div className="legend-item">
          <span className="legend-dot" style={{ backgroundColor: STATUS_COLORS.NO_SHOW }} />
          <span>Absent</span>
        </div>
      </div>
    </div>
  )
}

export default AppointmentCalendar
