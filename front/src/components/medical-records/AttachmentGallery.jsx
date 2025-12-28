import { useState } from 'react'
import { medicalRecordAPI } from '../../services/medicalRecordApi'
import './AttachmentGallery.css'

const AttachmentGallery = ({ attachments, onDownload }) => {
  const [selectedType, setSelectedType] = useState('ALL')
  const [imagePreview, setImagePreview] = useState(null)

  const attachmentTypes = ['ALL', 'ANALYSE', 'IMAGE', 'DOCUMENT', 'PRESCRIPTION', 'CERTIFICAT', 'AUTRE']

  const filteredAttachments = selectedType === 'ALL'
    ? attachments
    : attachments.filter(att => att.attachmentType === selectedType)

  const images = filteredAttachments.filter(att => 
    att.attachmentType === 'IMAGE' || att.fileName?.toLowerCase().match(/\.(jpg|jpeg|png|gif)$/)
  )

  const handleImageClick = async (attachment) => {
    try {
      const response = await medicalRecordAPI.downloadAttachment(attachment.id)
      const blob = new Blob([response.data])
      const url = window.URL.createObjectURL(blob)
      setImagePreview(url)
    } catch (err) {
      console.error('Erreur lors du chargement de l\'image:', err)
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

  return (
    <div className="attachment-gallery">
      <div className="gallery-header">
        <h3>📎 Pièces jointes ({attachments.length})</h3>
        <div className="filter-tabs">
          {attachmentTypes.map(type => (
            <button
              key={type}
              onClick={() => setSelectedType(type)}
              className={`filter-tab ${selectedType === type ? 'active' : ''}`}
            >
              {type === 'ALL' ? 'Tous' : type}
            </button>
          ))}
        </div>
      </div>

      {images.length > 0 && (
        <div className="gallery-grid">
          {images.map(attachment => (
            <div key={attachment.id} className="gallery-item">
              <div className="gallery-item-image" onClick={() => handleImageClick(attachment)}>
                <span className="image-icon">🖼️</span>
                <span className="image-label">{attachment.fileName}</span>
              </div>
              <div className="gallery-item-info">
                <p className="gallery-item-name">{attachment.fileName}</p>
                {attachment.description && (
                  <p className="gallery-item-description">{attachment.description}</p>
                )}
                <p className="gallery-item-date">{formatDate(attachment.createdAt)}</p>
                <button
                  onClick={() => onDownload(attachment.id)}
                  className="btn-download-small"
                >
                  📥 Télécharger
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {filteredAttachments.length > 0 && (
        <div className="attachment-list">
          {filteredAttachments.map(attachment => (
            <div key={attachment.id} className="attachment-list-item">
              <div className="attachment-icon">
                {attachment.attachmentType === 'ANALYSE' ? '📊' :
                 attachment.attachmentType === 'IMAGE' ? '🖼️' :
                 attachment.attachmentType === 'PRESCRIPTION' ? '💊' :
                 attachment.attachmentType === 'CERTIFICAT' ? '📜' : '📄'}
              </div>
              <div className="attachment-info">
                <p className="attachment-name">{attachment.fileName}</p>
                {attachment.description && (
                  <p className="attachment-description">{attachment.description}</p>
                )}
                <div className="attachment-meta">
                  <span className="attachment-type">{attachment.attachmentType}</span>
                  <span className="attachment-date">{formatDate(attachment.createdAt)}</span>
                </div>
              </div>
              <button
                onClick={() => onDownload(attachment.id)}
                className="btn-download-small"
              >
                📥
              </button>
            </div>
          ))}
        </div>
      )}

      {filteredAttachments.length === 0 && (
        <div className="empty-gallery">
          <p>Aucune pièce jointe trouvée</p>
        </div>
      )}

      {imagePreview && (
        <div className="image-preview-overlay" onClick={() => setImagePreview(null)}>
          <div className="image-preview-content" onClick={(e) => e.stopPropagation()}>
            <button className="close-preview" onClick={() => setImagePreview(null)}>×</button>
            <img src={imagePreview} alt="Preview" />
          </div>
        </div>
      )}
    </div>
  )
}

export default AttachmentGallery

