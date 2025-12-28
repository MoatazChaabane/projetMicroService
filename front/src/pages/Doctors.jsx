import { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'
import { doctorAPI } from '../services/doctorApi'
import DoctorTable from '../components/doctors/DoctorTable'
import DoctorFilters from '../components/doctors/DoctorFilters'
import DoctorModal from '../components/doctors/DoctorModal'
import DeleteConfirmModal from '../components/doctors/DeleteConfirmModal'
import Pagination from '../components/common/Pagination'
import './Doctors.css'

const Doctors = () => {
  const { isAuthenticated, user } = useAuth()
  const [doctors, setDoctors] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  
  // Pagination
  const [currentPage, setCurrentPage] = useState(0)
  const [pageSize, setPageSize] = useState(10)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [sortBy, setSortBy] = useState('rating')
  const [sortDir, setSortDir] = useState('desc')
  
  // Filtres
  const [searchFilters, setSearchFilters] = useState(null)
  
  // Modals
  const [showAddModal, setShowAddModal] = useState(false)
  const [showEditModal, setShowEditModal] = useState(false)
  const [showDeleteModal, setShowDeleteModal] = useState(false)
  const [selectedDoctor, setSelectedDoctor] = useState(null)

  const userRole = user?.role || ''

  useEffect(() => {
    if (isAuthenticated) {
      fetchDoctors()
    }
  }, [currentPage, pageSize, sortBy, sortDir, searchFilters, isAuthenticated])

  const fetchDoctors = async () => {
    setLoading(true)
    setError('')
    try {
      const params = {
        page: currentPage,
        size: pageSize,
        sortBy,
        sortDir
      }
      
      let response
      if (searchFilters && Object.keys(searchFilters).length > 0) {
        response = await doctorAPI.searchDoctors(searchFilters, params)
      } else {
        response = await doctorAPI.getAllDoctors(params)
      }
      
      setDoctors(response.data.content || [])
      setTotalPages(response.data.totalPages || 0)
      setTotalElements(response.data.totalElements || 0)
    } catch (err) {
      console.error('Error fetching doctors:', err)
      setError(err.response?.data?.message || 'Erreur lors du chargement des docteurs')
      setDoctors([])
    } finally {
      setLoading(false)
    }
  }

  const handleAdd = () => {
    setSelectedDoctor(null)
    setShowAddModal(true)
  }

  const handleEdit = (doctor) => {
    setSelectedDoctor(doctor)
    setShowEditModal(true)
  }

  const handleDelete = (doctor) => {
    setSelectedDoctor(doctor)
    setShowDeleteModal(true)
  }

  const handleFilterChange = (filters) => {
    setSearchFilters(filters)
    setCurrentPage(0)
  }

  const handleClearFilters = () => {
    setSearchFilters(null)
    setCurrentPage(0)
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
    setShowDeleteModal(false)
    setSelectedDoctor(null)
  }

  const handleSaveSuccess = () => {
    handleModalClose()
    fetchDoctors()
  }

  const handleDeleteSuccess = () => {
    handleModalClose()
    fetchDoctors()
  }

  if (!isAuthenticated) {
    return (
      <div className="doctors-container">
        <div className="error-message">Veuillez vous connecter pour accéder à cette page.</div>
      </div>
    )
  }

  return (
    <div className="doctors-container">
      <div className="doctors-header">
        <h1>👨‍⚕️ Gestion des Docteurs</h1>
        {userRole === 'ADMIN' && (
          <button onClick={handleAdd} className="btn-add">
            + Ajouter un docteur
          </button>
        )}
      </div>

      <DoctorFilters
        onFilterChange={handleFilterChange}
        onClearFilters={handleClearFilters}
      />

      <div className="doctors-toolbar">
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
          <DoctorTable
            doctors={doctors}
            onEdit={handleEdit}
            onDelete={handleDelete}
            onSort={handleSort}
            sortBy={sortBy}
            sortDir={sortDir}
            userRole={userRole}
          />

          {totalElements > 0 && (
            <Pagination
              currentPage={currentPage}
              totalPages={totalPages}
              totalElements={totalElements}
              pageSize={pageSize}
              onPageChange={handlePageChange}
              itemLabel="docteur"
            />
          )}

          {totalElements === 0 && !loading && (
            <div className="no-data">
              {searchFilters ? 'Aucun docteur trouvé avec ces filtres.' : 'Aucun docteur enregistré.'}
            </div>
          )}
        </>
      )}

      {showAddModal && (
        <DoctorModal
          mode="add"
          onClose={handleModalClose}
          onSave={handleSaveSuccess}
        />
      )}

      {showEditModal && selectedDoctor && (
        <DoctorModal
          mode="edit"
          doctor={selectedDoctor}
          onClose={handleModalClose}
          onSave={handleSaveSuccess}
        />
      )}

      {showDeleteModal && selectedDoctor && (
        <DeleteConfirmModal
          doctor={selectedDoctor}
          onClose={handleModalClose}
          onConfirm={handleDeleteSuccess}
        />
      )}
    </div>
  )
}

export default Doctors

