import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { medicalRecordAPI } from '../services/medicalRecordApi'
import { patientAPI } from '../services/patientApi'
import VisitModal from '../components/medical-records/VisitModal'
import AttachmentUpload from '../components/medical-records/AttachmentUpload'
import AttachmentGallery from '../components/medical-records/AttachmentGallery'
import TimelineItem from '../components/medical-records/TimelineItem'
import './MedicalRecord.css'

const MedicalRecord = () => {
  const { patientId: paramPatientId } = useParams()
  const navigate = useNavigate()
  const { user } = useAuth()
  const [medicalRecord, setMedicalRecord] = useState(null)
  const [patient, setPatient] = useState(null)
  const [timeline, setTimeline] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [showVisitModal, setShowVisitModal] = useState(false)
  const [searchTerm, setSearchTerm] = useState('')
  const [viewMode, setViewMode] = useState('timeline') // 'timeline' ou 'gallery'

  const patientId = paramPatientId ? Number(paramPatientId) : (user?.role === 'PATIENT' ? user.id : null)

  useEffect(() => {
    if (patientId) {
      fetchMedicalRecord()
      fetchTimeline()
      if (user?.role !== 'PATIENT') {
        fetchPatient()
      }
    } else if (!loading) {
      setLoading(false)
      setError('ID patient manquant')
    }
  }, [patientId, user])

  const fetchMedicalRecord = async () => {
    setLoading(true)
    setError('')
    try {
      const response = await medicalRecordAPI.getMedicalRecordByPatientId(patientId)
      setMedicalRecord(response.data)
    } catch (err) {
      if (err.response?.status === 404) {
        // Le dossier n'existe pas encore, on peut le créer si c'est un docteur/admin
        if (user?.role === 'DOCTOR' || user?.role === 'ADMIN') {
          try {
            const created = await medicalRecordAPI.createMedicalRecord(patientId)
            setMedicalRecord(created.data)
          } catch (createErr) {
            setError('Erreur lors de la création du dossier médical')
          }
        } else {
          setError('Dossier médical non trouvé')
        }
      } else {
        setError('Erreur lors de la récupération du dossier médical')
      }
    } finally {
      setLoading(false)
    }
  }

  const fetchPatient = async () => {
    try {
      const response = await patientAPI.getPatientById(patientId)
      setPatient(response.data)
    } catch (err) {
      console.error('Erreur lors de la récupération du patient:', err)
    }
  }

  const fetchTimeline = async () => {
    try {
      let response
      if (searchTerm.trim()) {
        response = await medicalRecordAPI.searchInHistory(patientId, searchTerm.trim())
      } else {
        response = await medicalRecordAPI.getTimeline(patientId)
      }
      setTimeline(response.data || [])
    } catch (err) {
      console.error('Erreur lors de la récupération de la timeline:', err)
      setTimeline([])
    }
  }

  useEffect(() => {
    if (patientId) {
      fetchTimeline()
    }
  }, [searchTerm, patientId])

  const handleVisitSave = () => {
    setShowVisitModal(false)
    fetchTimeline()
    fetchMedicalRecord()
  }

  const handleAttachmentUpload = () => {
    fetchTimeline()
    fetchMedicalRecord()
  }

  const handleDownloadAttachment = async (attachmentId) => {
    try {
      const response = await medicalRecordAPI.downloadAttachment(attachmentId)
      const blob = new Blob([response.data])
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      
      // Récupérer le nom du fichier depuis les headers ou utiliser un nom par défaut
      const contentDisposition = response.headers['content-disposition']
      let filename = 'attachment'
      if (contentDisposition) {
        const filenameMatch = contentDisposition.match(/filename="?(.+)"?/i)
        if (filenameMatch) {
          filename = filenameMatch[1]
        }
      }
      
      link.download = filename
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
    } catch (err) {
      console.error('Erreur lors du téléchargement:', err)
      alert('Erreur lors du téléchargement du fichier')
    }
  }

  const handleExportPDF = async () => {
    try {
      const response = await medicalRecordAPI.exportPDF(patientId)
      const blob = new Blob([response.data], { type: 'application/pdf' })
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `dossier-medical-${patientId}.pdf`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
    } catch (err) {
      console.error('Erreur lors de l\'export PDF:', err)
      alert('Erreur lors de l\'export du dossier en PDF')
    }
  }

  if (loading && !medicalRecord) {
    return (
      <div className="medical-record-container">
        <div className="loading-spinner">Chargement...</div>
      </div>
    )
  }

  if (error && !medicalRecord) {
    return (
      <div className="medical-record-container">
        <div className="error-alert">
          {error}
        </div>
      </div>
    )
  }

  const attachments = timeline.filter(item => item.type === 'ATTACHMENT')

  return (
    <div className="medical-record-container">
      <div className="medical-record-header">
        <div>
          <button onClick={() => navigate(-1)} className="btn-back">
            ← Retour
          </button>
          <h1>📋 Dossier Médical</h1>
          {patient && (
            <p className="patient-info">
              {patient.prenom} {patient.nom}
            </p>
          )}
        </div>
        <div className="header-actions">
          {(user?.role === 'DOCTOR' || user?.role === 'ADMIN') && (
            <>
              <button onClick={() => setShowVisitModal(true)} className="btn-add-visit">
                + Nouvelle consultation
              </button>
            </>
          )}
          <button onClick={handleExportPDF} className="btn-export-pdf">
            📄 Exporter PDF
          </button>
        </div>
      </div>

      {medicalRecord && (
        <div className="medical-record-stats">
          <div className="stat-item">
            <span className="stat-label">Consultations:</span>
            <span className="stat-value">{medicalRecord.visitsCount || 0}</span>
          </div>
          <div className="stat-item">
            <span className="stat-label">Pièces jointes:</span>
            <span className="stat-value">{medicalRecord.attachmentsCount || 0}</span>
          </div>
        </div>
      )}

      <div className="view-mode-tabs">
        <button
          onClick={() => setViewMode('timeline')}
          className={`view-tab ${viewMode === 'timeline' ? 'active' : ''}`}
        >
          📅 Timeline
        </button>
        <button
          onClick={() => setViewMode('gallery')}
          className={`view-tab ${viewMode === 'gallery' ? 'active' : ''}`}
        >
          🖼️ Galerie
        </button>
      </div>

      {(user?.role === 'DOCTOR' || user?.role === 'ADMIN') && (
        <AttachmentUpload
          medicalRecordId={medicalRecord?.id}
          onUploadSuccess={handleAttachmentUpload}
        />
      )}

      <div className="search-bar">
        <input
          type="text"
          placeholder="🔍 Rechercher dans l'historique..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className="search-input"
        />
      </div>

      {viewMode === 'timeline' ? (
        <div className="timeline-container">
          {timeline.length === 0 ? (
            <div className="empty-timeline">
              <p>Aucun élément dans l'historique</p>
            </div>
          ) : (
            timeline.map((item) => (
              <TimelineItem
                key={`${item.type}-${item.id}`}
                item={item}
                onDownloadAttachment={handleDownloadAttachment}
              />
            ))
          )}
        </div>
      ) : (
        <AttachmentGallery
          attachments={attachments}
          onDownload={handleDownloadAttachment}
        />
      )}

      {showVisitModal && medicalRecord && (
        <VisitModal
          medicalRecordId={medicalRecord.id}
          onClose={() => setShowVisitModal(false)}
          onSave={handleVisitSave}
        />
      )}
    </div>
  )
}

export default MedicalRecord

