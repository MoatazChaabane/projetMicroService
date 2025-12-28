import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { prescriptionAPI } from '../services/prescriptionApi'
import { doctorAPI } from '../services/doctorApi'
import PrescriptionModal from '../components/prescriptions/PrescriptionModal'
import Pagination from '../components/common/Pagination'
import './Prescriptions.css'

const Prescriptions = () => {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [prescriptions, setPrescriptions] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [showModal, setShowModal] = useState(false)
  const [currentPage, setCurrentPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [doctorId, setDoctorId] = useState(null)
  const pageSize = 10

  useEffect(() => {
    if (user?.role === 'DOCTOR' && user?.id) {
      fetchDoctorId()
    }
  }, [user])

  useEffect(() => {
    if (user?.role === 'ADMIN' || doctorId || user?.id) {
      fetchPrescriptions()
    }
  }, [currentPage, user, doctorId])

  const fetchDoctorId = async () => {
    try {
      const response = await doctorAPI.getDoctorByUserId(user.id)
      setDoctorId(response.data.id)
    } catch (err) {
      console.error('Erreur lors de la récupération du doctorId:', err)
    }
  }

  const fetchPrescriptions = async () => {
    setLoading(true)
    setError('')
    try {
      let response
      
      if (user?.role === 'ADMIN') {
        // Admin voit toutes les ordonnances
        response = await prescriptionAPI.getAllPrescriptions()
        setPrescriptions(response.data || [])
        setTotalPages(1)
        setTotalElements(response.data?.length || 0)
      } else if (user?.role === 'DOCTOR' && doctorId) {
        // Docteur voit ses propres ordonnances
        response = await prescriptionAPI.getDoctorPrescriptions(doctorId, currentPage, pageSize)
        setPrescriptions(response.data.content || [])
        setTotalPages(response.data.totalPages || 0)
        setTotalElements(response.data.totalElements || 0)
      } else if (user?.role === 'PATIENT') {
        // Patient voit ses propres ordonnances
        response = await prescriptionAPI.getPatientPrescriptions(user.id, currentPage, pageSize)
        setPrescriptions(response.data.content || [])
        setTotalPages(response.data.totalPages || 0)
        setTotalElements(response.data.totalElements || 0)
      }
    } catch (err) {
      console.error('Erreur lors de la récupération des ordonnances:', err)
      setError('Erreur lors de la récupération des ordonnances')
    } finally {
      setLoading(false)
    }
  }

  const handleCreateSuccess = () => {
    setShowModal(false)
    fetchPrescriptions()
  }

  const handleDownloadPDF = async (pdfUrl) => {
    try {
      const response = await prescriptionAPI.downloadPDF(pdfUrl)
      const blob = new Blob([response.data], { type: 'application/pdf' })
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `ordonnance-${Date.now()}.pdf`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
    } catch (err) {
      console.error('Erreur lors du téléchargement du PDF:', err)
      alert('Erreur lors du téléchargement du PDF')
    }
  }

  const formatDate = (dateString) => {
    const date = new Date(dateString)
    return date.toLocaleDateString('fr-FR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    })
  }

  if (loading && prescriptions.length === 0) {
    return (
      <div className="prescriptions-container">
        <div className="loading-spinner">Chargement...</div>
      </div>
    )
  }

  return (
    <div className="prescriptions-container">
      <div className="prescriptions-header">
        <h1>📋 Ordonnances</h1>
        {user?.role === 'DOCTOR' && (
          <button onClick={() => setShowModal(true)} className="btn-create">
            + Nouvelle ordonnance
          </button>
        )}
      </div>

      {error && (
        <div className="error-alert">
          {error}
        </div>
      )}

      {prescriptions.length === 0 ? (
        <div className="empty-state">
          <p>Aucune ordonnance trouvée</p>
        </div>
      ) : (
        <>
          <div className="prescriptions-stats">
            <span>Total: {totalElements} ordonnance(s)</span>
          </div>

          <div className="prescriptions-list">
            {prescriptions.map((prescription) => (
              <div key={prescription.id} className="prescription-card">
                <div className="prescription-card-header">
                  <div className="prescription-info">
                    <h3>Ordonnance #{prescription.id}</h3>
                    <span className="prescription-date">{formatDate(prescription.date)}</span>
                  </div>
                  {prescription.pdfUrl && (
                    <button
                      onClick={() => handleDownloadPDF(prescription.pdfUrl)}
                      className="btn-download-pdf"
                    >
                      📥 Télécharger PDF
                    </button>
                  )}
                </div>

                <div className="prescription-details">
                  <div className="detail-row">
                    <span className="detail-label">Docteur:</span>
                    <span className="detail-value">{prescription.doctorName}</span>
                  </div>
                  {user?.role !== 'PATIENT' && (
                    <div className="detail-row">
                      <span className="detail-label">Patient:</span>
                      <span className="detail-value">{prescription.patientName}</span>
                    </div>
                  )}
                  <div className="detail-row">
                    <span className="detail-label">Médicaments:</span>
                    <span className="detail-value">{prescription.medications?.length || 0} médicament(s)</span>
                  </div>
                </div>

                {prescription.instructions && (
                  <div className="prescription-instructions">
                    <strong>Instructions:</strong> {prescription.instructions}
                  </div>
                )}

                <div className="prescription-actions">
                  <button
                    onClick={() => navigate(`/prescriptions/${prescription.id}`)}
                    className="btn-view"
                  >
                    Voir les détails
                  </button>
                </div>
              </div>
            ))}
          </div>

          {totalPages > 1 && (
            <Pagination
              currentPage={currentPage}
              totalPages={totalPages}
              onPageChange={setCurrentPage}
            />
          )}
        </>
      )}

      {showModal && (
        <PrescriptionModal
          mode="add"
          onClose={() => setShowModal(false)}
          onSave={handleCreateSuccess}
        />
      )}
    </div>
  )
}

export default Prescriptions

