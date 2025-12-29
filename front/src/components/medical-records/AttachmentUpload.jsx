import { useState, useEffect } from 'react'
import { useAuth } from '../../context/AuthContext'
import { doctorAPI } from '../../services/doctorApi'
import { medicalRecordAPI } from '../../services/medicalRecordApi'
import './AttachmentUpload.css'

const ATTACHMENT_TYPES = [
  { value: 'ANALYSE', label: 'Analyse' },
  { value: 'IMAGE', label: 'Image' },
  { value: 'DOCUMENT', label: 'Document' },
  { value: 'PRESCRIPTION', label: 'Prescription' },
  { value: 'CERTIFICAT', label: 'Certificat' },
  { value: 'AUTRE', label: 'Autre' }
]

const AttachmentUpload = ({ medicalRecordId, onUploadSuccess }) => {
  const { user } = useAuth()
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [file, setFile] = useState(null)
  const [attachmentType, setAttachmentType] = useState('ANALYSE')
  const [description, setDescription] = useState('')
  const [doctorId, setDoctorId] = useState(null)

  useEffect(() => {
    if (user?.role === 'DOCTOR' && user?.id) {
      fetchDoctorId()
    }
  }, [user])

  const fetchDoctorId = async () => {
    try {
      const response = await doctorAPI.getDoctorByUserId(user.id)
      setDoctorId(response.data.id)
    } catch (err) {
      console.error('Erreur lors de la récupération du doctorId:', err)
    }
  }

  const handleFileChange = (e) => {
    const selectedFile = e.target.files[0]
    if (selectedFile) {
      if (selectedFile.size > 10 * 1024 * 1024) { // 10MB
        setError('Le fichier dépasse 10MB')
        return
      }
      setFile(selectedFile)
      setError('')
    }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    
    if (!file) {
      setError('Veuillez sélectionner un fichier')
      return
    }

    setError('')
    setLoading(true)

    try {
      const formData = new FormData()
      formData.append('medicalRecordId', medicalRecordId)
      formData.append('attachmentType', attachmentType)
      formData.append('file', file)
      if (description) {
        formData.append('description', description)
      }
      if (doctorId) {
        formData.append('doctorId', doctorId)
      }

      await medicalRecordAPI.addAttachment(formData)

      setFile(null)
      setDescription('')
      setAttachmentType('ANALYSE')
      if (e.target.querySelector('input[type="file"]')) {
        e.target.querySelector('input[type="file"]').value = ''
      }
      
      if (onUploadSuccess) {
        onUploadSuccess()
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors de l\'upload du fichier')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="attachment-upload">
      <h3>Ajouter une pièce jointe</h3>
      
      {error && (
        <div className="error-alert">
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} className="upload-form">
        <div className="form-group">
          <label htmlFor="attachmentType">Type *</label>
          <select
            id="attachmentType"
            value={attachmentType}
            onChange={(e) => setAttachmentType(e.target.value)}
            required
          >
            {ATTACHMENT_TYPES.map(type => (
              <option key={type.value} value={type.value}>
                {type.label}
              </option>
            ))}
          </select>
        </div>

        <div className="form-group">
          <label htmlFor="file">Fichier *</label>
          <input
            id="file"
            type="file"
            onChange={handleFileChange}
            accept=".pdf,.jpg,.jpeg,.png,.gif,.doc,.docx"
            required
          />
          {file && (
            <div className="file-info">
              <span>📄 {file.name}</span>
              <span className="file-size">({(file.size / 1024).toFixed(2)} KB)</span>
            </div>
          )}
        </div>

        <div className="form-group">
          <label htmlFor="description">Description</label>
          <textarea
            id="description"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            rows="2"
            placeholder="Ex: Analyse de sang - NFS"
          />
        </div>

        <button type="submit" disabled={loading || !file} className="btn-upload">
          {loading ? 'Upload en cours...' : '📤 Uploader'}
        </button>
      </form>
    </div>
  )
}

export default AttachmentUpload

