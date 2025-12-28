import './TimelineItem.css'

const TimelineItem = ({ item, onDownloadAttachment }) => {
  const formatDateTime = (dateTime) => {
    const date = new Date(dateTime)
    return date.toLocaleDateString('fr-FR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  const formatDate = (date) => {
    const d = new Date(date)
    return d.toLocaleDateString('fr-FR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    })
  }

  if (item.type === 'VISIT') {
    return (
      <div className="timeline-item visit-item">
        <div className="timeline-marker visit-marker">
          <span className="marker-icon">👨‍⚕️</span>
        </div>
        <div className="timeline-content">
          <div className="timeline-header">
            <h4>Consultation</h4>
            <span className="timeline-date">{formatDateTime(item.dateTime)}</span>
          </div>
          <div className="timeline-body">
            <p className="doctor-name">{item.doctorName}</p>
            {item.reason && (
              <div className="visit-detail">
                <strong>Motif:</strong> {item.reason}
              </div>
            )}
            {item.diagnosis && (
              <div className="visit-detail">
                <strong>Diagnostic:</strong> {item.diagnosis}
              </div>
            )}
          </div>
        </div>
      </div>
    )
  } else if (item.type === 'ATTACHMENT') {
    const isImage = item.attachmentType === 'IMAGE' || 
                    item.fileName?.toLowerCase().match(/\.(jpg|jpeg|png|gif)$/)
    
    return (
      <div className="timeline-item attachment-item">
        <div className="timeline-marker attachment-marker">
          <span className="marker-icon">
            {isImage ? '🖼️' : item.attachmentType === 'ANALYSE' ? '📊' : '📄'}
          </span>
        </div>
        <div className="timeline-content">
          <div className="timeline-header">
            <h4>{item.fileName || item.title}</h4>
            <span className="timeline-date">{formatDateTime(item.dateTime)}</span>
          </div>
          <div className="timeline-body">
            <p className="doctor-name">{item.doctorName}</p>
            {item.description && (
              <div className="attachment-detail">
                <strong>Description:</strong> {item.description}
              </div>
            )}
            <div className="attachment-type-badge">
              {item.attachmentType}
            </div>
            {onDownloadAttachment && (
              <button 
                onClick={() => onDownloadAttachment(item.id)}
                className="btn-download"
              >
                📥 Télécharger
              </button>
            )}
          </div>
        </div>
      </div>
    )
  }

  return null
}

export default TimelineItem

