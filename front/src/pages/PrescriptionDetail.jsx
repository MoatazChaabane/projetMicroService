import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { prescriptionAPI } from '../services/prescriptionApi'
import './PrescriptionDetail.css'

const PrescriptionDetail = () => {
  const { id } = useParams()
  const navigate = useNavigate()
  const [prescription, setPrescription] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    fetchPrescription()
  }, [id])

  const fetchPrescription = async () => {
    setLoading(true)
    setError('')
    try {
      const response = await prescriptionAPI.getPrescriptionById(id)
      setPrescription(response.data)
    } catch (err) {
      console.error('Erreur lors de la récupération de l\'ordonnance:', err)
      setError('Erreur lors de la récupération de l\'ordonnance')
    } finally {
      setLoading(false)
    }
  }

  const handleDownloadPDF = async () => {
    if (!prescription?.pdfUrl) return
    
    try {
      const response = await prescriptionAPI.downloadPDF(prescription.pdfUrl)
      const blob = new Blob([response.data], { type: 'application/pdf' })
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `ordonnance-${prescription.id}.pdf`
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

  if (loading) {
    return (
      <div className="prescription-detail-container">
        <div className="loading-spinner">Chargement...</div>
      </div>
    )
  }

  if (error || !prescription) {
    return (
      <div className="prescription-detail-container">
        <div className="error-alert">
          {error || 'Ordonnance non trouvée'}
        </div>
        <button onClick={() => navigate('/prescriptions')} className="btn-back">
          Retour à la liste
        </button>
      </div>
    )
  }

  return (
    <div className="prescription-detail-container">
      <div className="detail-header">
        <button onClick={() => navigate('/prescriptions')} className="btn-back">
          ← Retour
        </button>
        <h1>📋 Ordonnance #{prescription.id}</h1>
        {prescription.pdfUrl && (
          <button onClick={handleDownloadPDF} className="btn-download-pdf">
            📥 Télécharger PDF
          </button>
        )}
      </div>

      <div className="prescription-detail-card">
        <div className="detail-section">
          <h2>Informations générales</h2>
          <div className="info-grid">
            <div className="info-item">
              <span className="info-label">Date:</span>
              <span className="info-value">{formatDate(prescription.date)}</span>
            </div>
            <div className="info-item">
              <span className="info-label">Docteur:</span>
              <span className="info-value">{prescription.doctorName}</span>
            </div>
            <div className="info-item">
              <span className="info-label">Spécialité:</span>
              <span className="info-value">{prescription.doctorSpeciality?.replace(/_/g, ' ')}</span>
            </div>
            <div className="info-item">
              <span className="info-label">Patient:</span>
              <span className="info-value">{prescription.patientName}</span>
            </div>
          </div>
        </div>

        <div className="detail-section">
          <h2>Médicaments</h2>
          <div className="medications-list">
            {prescription.medications?.map((medication, index) => (
              <div key={index} className="medication-item">
                <div className="medication-header">
                  <span className="medication-number">Médicament {index + 1}</span>
                  <span className="medication-name">{medication.name}</span>
                </div>
                <div className="medication-details">
                  <div className="medication-detail">
                    <span className="detail-label">Dosage:</span>
                    <span className="detail-value">{medication.dosage}</span>
                  </div>
                  <div className="medication-detail">
                    <span className="detail-label">Fréquence:</span>
                    <span className="detail-value">{medication.frequency}</span>
                  </div>
                  <div className="medication-detail">
                    <span className="detail-label">Durée:</span>
                    <span className="detail-value">{medication.duration}</span>
                  </div>
                  {medication.instructions && (
                    <div className="medication-detail full-width">
                      <span className="detail-label">Instructions:</span>
                      <span className="detail-value">{medication.instructions}</span>
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>
        </div>

        {prescription.instructions && (
          <div className="detail-section">
            <h2>Instructions générales</h2>
            <div className="instructions-box">
              {prescription.instructions}
            </div>
          </div>
        )}

        {prescription.signatureHash && (
          <div className="detail-section">
            <h2>Signature numérique</h2>
            <div className="signature-box">
              <div className="signature-info">
                <span className="signature-label">Hash SHA-256:</span>
                <span className="signature-value">{prescription.signatureHash}</span>
              </div>
              <div className="signature-note">
                ⚠️ Cette signature garantit l'intégrité de l'ordonnance
              </div>
            </div>
          </div>
        )}

        <div className="detail-section">
          <div className="metadata">
            <span>Créée le: {formatDate(prescription.createdAt)}</span>
            {prescription.updatedAt && prescription.updatedAt !== prescription.createdAt && (
              <span>Modifiée le: {formatDate(prescription.updatedAt)}</span>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}

export default PrescriptionDetail

