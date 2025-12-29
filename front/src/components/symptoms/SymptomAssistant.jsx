import { useState, useEffect, useRef } from 'react'
import { useAuth } from '../../context/AuthContext'
import { symptomAnalysisAPI } from '../../services/symptomAnalysisApi'
import './SymptomAssistant.css'

const SymptomAssistant = ({ onAnalysisComplete, appointmentId, patientId, onClose }) => {
  const { user } = useAuth()
  const [messages, setMessages] = useState([])
  const [inputText, setInputText] = useState('')
  const [loading, setLoading] = useState(false)
  const [analysisResult, setAnalysisResult] = useState(null)
  const [error, setError] = useState('')
  const messagesEndRef = useRef(null)
  const chatContainerRef = useRef(null)

  useEffect(() => {

    setMessages([
      {
        type: 'assistant',
        text: "Bonjour ! Je suis votre assistant symptômes. 📋\n\nDécrivez-moi vos symptômes en détail (localisation, intensité, durée, etc.) et je vous aiderai à les structurer pour votre consultation.",
        timestamp: new Date()
      }
    ])
  }, [])

  useEffect(() => {
    scrollToBottom()
  }, [messages, analysisResult])

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }

  const handleSendMessage = async () => {
    if (!inputText.trim() || loading) return

    const userMessage = {
      type: 'user',
      text: inputText.trim(),
      timestamp: new Date()
    }

    setMessages(prev => [...prev, userMessage])
    setInputText('')
    setLoading(true)
    setError('')

    const loadingMessage = {
      type: 'assistant',
      text: '⏳ Analyse en cours...',
      timestamp: new Date(),
      loading: true
    }
    setMessages(prev => [...prev, loadingMessage])

    try {
      const currentPatientId = patientId || user?.id
      if (!currentPatientId) {
        throw new Error('ID patient non disponible')
      }

      const response = await symptomAnalysisAPI.analyzeSymptoms({
        description: userMessage.text,
        patientId: currentPatientId,
        appointmentId: appointmentId || null
      })

      const analysis = response.data
      setAnalysisResult(analysis)

      setMessages(prev => {
        const newMessages = [...prev]
        const lastIndex = newMessages.length - 1
        if (newMessages[lastIndex]?.loading) {
          newMessages[lastIndex] = {
            type: 'assistant',
            text: generateResponseMessage(analysis),
            timestamp: new Date(),
            analysis: analysis
          }
        }
        return newMessages
      })

      if (onAnalysisComplete) {
        onAnalysisComplete(analysis)
      }
    } catch (err) {
      console.error('Erreur analyse:', err)
      setError(err.response?.data?.message || 'Erreur lors de l\'analyse des symptômes')

      setMessages(prev => {
        const newMessages = [...prev]
        const lastIndex = newMessages.length - 1
        if (newMessages[lastIndex]?.loading) {
          newMessages[lastIndex] = {
            type: 'assistant',
            text: '❌ Désolé, une erreur est survenue lors de l\'analyse. Veuillez réessayer.',
            timestamp: new Date(),
            error: true
          }
        }
        return newMessages
      })
    } finally {
      setLoading(false)
    }
  }

  const generateResponseMessage = (analysis) => {
    let message = "✅ Analyse terminée !\n\n"
    
    if (analysis.symptoms && analysis.symptoms.length > 0) {
      message += `📋 Symptômes identifiés : ${analysis.symptoms.join(', ')}\n\n`
    }
    
    if (analysis.severity) {
      message += `📊 Sévérité : ${analysis.severity}/10\n`
    }
    
    if (analysis.duration) {
      message += `⏱️ Durée : ${analysis.duration} jour(s)\n`
    }
    
    if (analysis.urgentRecommendation) {
      message += `\n⚠️ ${analysis.recommendationMessage}\n`
    }
    
    if (analysis.suggestedSpecialties && analysis.suggestedSpecialties.length > 0) {
      message += `\n👨‍⚕️ Spécialités suggérées : ${analysis.suggestedSpecialties.map(s => s.replace(/_/g, ' ')).join(', ')}`
    }

    return message
  }

  const handleKeyPress = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      handleSendMessage()
    }
  }

  const handleStartNewAnalysis = () => {
    setAnalysisResult(null)
    setMessages([
      {
        type: 'assistant',
        text: "Bonjour ! Décrivez-moi vos nouveaux symptômes en détail.",
        timestamp: new Date()
      }
    ])
  }

  return (
    <div className="symptom-assistant-container">
      <div className="assistant-header">
        <h3>🤖 Assistant Symptômes</h3>
        {analysisResult && (
          <button onClick={handleStartNewAnalysis} className="btn-new-analysis">
            Nouvelle analyse
          </button>
        )}
      </div>

      <div className="assistant-content">
        <div className="chat-container" ref={chatContainerRef}>
          <div className="messages-list">
            {messages.map((message, index) => (
              <div key={index} className={`message ${message.type}`}>
                <div className="message-content">
                  {message.loading ? (
                    <div className="loading-dots">
                      <span></span>
                      <span></span>
                      <span></span>
                    </div>
                  ) : (
                    <div className="message-text">
                      {message.text.split('\n').map((line, i) => (
                        <div key={i}>{line}</div>
                      ))}
                    </div>
                  )}
                  <div className="message-time">
                    {message.timestamp.toLocaleTimeString('fr-FR', { 
                      hour: '2-digit', 
                      minute: '2-digit' 
                    })}
                  </div>
                </div>
              </div>
            ))}
            <div ref={messagesEndRef} />
          </div>

          <div className="chat-input-container">
            {error && (
              <div className="error-message">{error}</div>
            )}
            <div className="input-wrapper">
              <textarea
                value={inputText}
                onChange={(e) => setInputText(e.target.value)}
                onKeyPress={handleKeyPress}
                placeholder="Décrivez vos symptômes..."
                rows={3}
                disabled={loading}
                className="chat-input"
              />
              <button
                onClick={handleSendMessage}
                disabled={!inputText.trim() || loading}
                className="btn-send"
              >
                {loading ? '⏳' : '📤'}
              </button>
            </div>
            {analysisResult?.safetyWarning && (
              <div className="safety-warning">
                {analysisResult.safetyWarning}
              </div>
            )}
          </div>
        </div>

        {analysisResult && (
          <div className="summary-panel">
            <div className="summary-header">
              <h4>📄 Résumé Généré</h4>
              {analysisResult.urgentRecommendation && (
                <span className="urgent-badge">⚠️ URGENT</span>
              )}
            </div>

            <div className="summary-content">
              <div className="summary-section">
                <h5>Symptômes</h5>
                <div className="symptoms-list">
                  {analysisResult.symptoms.map((symptom, index) => (
                    <span key={index} className="symptom-tag">{symptom}</span>
                  ))}
                </div>
              </div>

              {(analysisResult.severity || analysisResult.duration) && (
                <div className="summary-section">
                  <h5>Informations</h5>
                  {analysisResult.severity && (
                    <div className="info-item">
                      <span className="info-label">Sévérité:</span>
                      <span className="severity-badge">{analysisResult.severity}/10</span>
                    </div>
                  )}
                  {analysisResult.duration && (
                    <div className="info-item">
                      <span className="info-label">Durée:</span>
                      <span>{analysisResult.duration} jour(s)</span>
                    </div>
                  )}
                </div>
              )}

              {analysisResult.redFlags && analysisResult.redFlags.length > 0 && (
                <div className="summary-section red-flags">
                  <h5>⚠️ Indicateurs d'Urgence</h5>
                  <ul>
                    {analysisResult.redFlags.map((flag, index) => (
                      <li key={index}>{flag}</li>
                    ))}
                  </ul>
                </div>
              )}

              {analysisResult.suggestedSpecialties && analysisResult.suggestedSpecialties.length > 0 && (
                <div className="summary-section">
                  <h5>Spécialités Suggérées</h5>
                  <div className="specialties-list">
                    {analysisResult.suggestedSpecialties.map((specialty, index) => (
                      <span key={index} className="specialty-tag">
                        {specialty.replace(/_/g, ' ')}
                      </span>
                    ))}
                  </div>
                </div>
              )}

              {analysisResult.questions && analysisResult.questions.length > 0 && (
                <div className="summary-section">
                  <h5>Questions de Clarification</h5>
                  <ul className="questions-list">
                    {analysisResult.questions.map((question, index) => (
                      <li key={index}>{question}</li>
                    ))}
                  </ul>
                </div>
              )}

              {analysisResult.recommendationMessage && (
                <div className="summary-section recommendation">
                  <h5>Recommandation</h5>
                  <p>{analysisResult.recommendationMessage}</p>
                </div>
              )}

              {analysisResult.summary && (
                <div className="summary-section full-summary">
                  <h5>Résumé Complet pour le Docteur</h5>
                  <pre className="summary-text">{analysisResult.summary}</pre>
                </div>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  )
}

export default SymptomAssistant

