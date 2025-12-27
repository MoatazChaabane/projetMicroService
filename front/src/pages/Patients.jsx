import { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'
import { patientAPI } from '../services/patientApi'
import PatientTable from '../components/patients/PatientTable'
import PatientModal from '../components/patients/PatientModal'
import PatientDetailsModal from '../components/patients/PatientDetailsModal'
import DeleteConfirmModal from '../components/patients/DeleteConfirmModal'
import SearchBar from '../components/patients/SearchBar'
import Pagination from '../components/patients/Pagination'
import './Patients.css'

const Patients = () => {
  const { isAuthenticated } = useAuth()
  const [patients, setPatients] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  
  // Pagination
  const [currentPage, setCurrentPage] = useState(0)
  const [pageSize, setPageSize] = useState(10)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [sortBy, setSortBy] = useState('id')
  const [sortDir, setSortDir] = useState('asc')
  
  // Recherche
  const [searchTerm, setSearchTerm] = useState('')
  
  // Modals
  const [showAddModal, setShowAddModal] = useState(false)
  const [showEditModal, setShowEditModal] = useState(false)
  const [showDetailsModal, setShowDetailsModal] = useState(false)
  const [showDeleteModal, setShowDeleteModal] = useState(false)
  const [selectedPatient, setSelectedPatient] = useState(null)

  useEffect(() => {
    if (isAuthenticated) {
      fetchPatients()
    }
  }, [currentPage, pageSize, sortBy, sortDir, searchTerm, isAuthenticated])

  const fetchPatients = async () => {
    setLoading(true)
    setError('')
    try {
      const params = {
        page: currentPage,
        size: pageSize,
        sortBy,
        sortDir
      }
      
      const response = searchTerm.trim()
        ? await patientAPI.searchPatients({ ...params, search: searchTerm })
        : await patientAPI.getAllPatients(params)
      
      setPatients(response.data.content || [])
      setTotalPages(response.data.totalPages || 0)
      setTotalElements(response.data.totalElements || 0)
    } catch (err) {
      console.error('Error fetching patients:', err)
      setError(err.response?.data?.message || 'Erreur lors du chargement des patients')
      setPatients([])
    } finally {
      setLoading(false)
    }
  }

  const handleAdd = () => {
    setSelectedPatient(null)
    setShowAddModal(true)
  }

  const handleEdit = (patient) => {
    setSelectedPatient(patient)
    setShowEditModal(true)
  }

  const handleView = (patient) => {
    setSelectedPatient(patient)
    setShowDetailsModal(true)
  }

  const handleDelete = (patient) => {
    setSelectedPatient(patient)
    setShowDeleteModal(true)
  }

  const handleSearch = (term) => {
    setSearchTerm(term)
    setCurrentPage(0) // Reset à la première page lors d'une recherche
  }

  const handlePageChange = (page) => {
    setCurrentPage(page)
  }

  const handlePageSizeChange = (size) => {
    setPageSize(size)
    setCurrentPage(0)
  }

  const handleSort = (field) => {
    if (sortBy === field) {
      setSortDir(sortDir === 'asc' ? 'desc' : 'asc')
    } else {
      setSortBy(field)
      setSortDir('asc')
    }
    setCurrentPage(0)
  }

  const handleModalClose = () => {
    setShowAddModal(false)
    setShowEditModal(false)
    setShowDetailsModal(false)
    setShowDeleteModal(false)
    setSelectedPatient(null)
  }

  const handleSaveSuccess = () => {
    handleModalClose()
    fetchPatients()
  }

  const handleDeleteSuccess = () => {
    handleModalClose()
    fetchPatients()
  }

  if (!isAuthenticated) {
    return (
      <div className="patients-container">
        <div className="error-message">Veuillez vous connecter pour accéder à cette page.</div>
      </div>
    )
  }

  return (
    <div className="patients-container">
      <div className="patients-header">
        <h1>Gestion des Patients</h1>
        <button onClick={handleAdd} className="btn-add">
          + Ajouter un patient
        </button>
      </div>

      <div className="patients-toolbar">
        <SearchBar onSearch={handleSearch} placeholder="Rechercher par nom, prénom ou téléphone..." />
        <div className="page-size-selector">
          <label>Par page:</label>
          <select value={pageSize} onChange={(e) => handlePageSizeChange(Number(e.target.value))}>
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
          <PatientTable
            patients={patients}
            onEdit={handleEdit}
            onView={handleView}
            onDelete={handleDelete}
            onSort={handleSort}
            sortBy={sortBy}
            sortDir={sortDir}
          />

          {totalElements > 0 && (
            <Pagination
              currentPage={currentPage}
              totalPages={totalPages}
              totalElements={totalElements}
              pageSize={pageSize}
              onPageChange={handlePageChange}
            />
          )}

          {totalElements === 0 && !loading && (
            <div className="no-data">
              {searchTerm ? 'Aucun patient trouvé pour cette recherche.' : 'Aucun patient enregistré.'}
            </div>
          )}
        </>
      )}

      {showAddModal && (
        <PatientModal
          mode="add"
          onClose={handleModalClose}
          onSave={handleSaveSuccess}
        />
      )}

      {showEditModal && selectedPatient && (
        <PatientModal
          mode="edit"
          patient={selectedPatient}
          onClose={handleModalClose}
          onSave={handleSaveSuccess}
        />
      )}

      {showDetailsModal && selectedPatient && (
        <PatientDetailsModal
          patient={selectedPatient}
          onClose={handleModalClose}
          onEdit={() => {
            setShowDetailsModal(false)
            handleEdit(selectedPatient)
          }}
        />
      )}

      {showDeleteModal && selectedPatient && (
        <DeleteConfirmModal
          patient={selectedPatient}
          onClose={handleModalClose}
          onConfirm={handleDeleteSuccess}
        />
      )}
    </div>
  )
}

export default Patients

