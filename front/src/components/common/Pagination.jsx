import './Pagination.css'

const Pagination = ({ currentPage, totalPages, totalElements, pageSize, onPageChange, itemLabel = 'élément' }) => {
  const getPageNumbers = () => {
    const pages = []
    const maxPages = 5
    
    let startPage = Math.max(0, currentPage - Math.floor(maxPages / 2))
    let endPage = Math.min(totalPages - 1, startPage + maxPages - 1)
    
    if (endPage - startPage < maxPages - 1) {
      startPage = Math.max(0, endPage - maxPages + 1)
    }
    
    for (let i = startPage; i <= endPage; i++) {
      pages.push(i)
    }
    
    return pages
  }

  const startItem = currentPage * pageSize + 1
  const endItem = Math.min((currentPage + 1) * pageSize, totalElements)

  if (totalPages <= 1) return null

  return (
    <div className="pagination-container">
      <div className="pagination-info">
        Affichage de {startItem} à {endItem} sur {totalElements} {itemLabel}(s)
      </div>
      <div className="pagination-controls">
        <button
          onClick={() => onPageChange(0)}
          disabled={currentPage === 0}
          className="pagination-btn"
        >
          ««
        </button>
        <button
          onClick={() => onPageChange(currentPage - 1)}
          disabled={currentPage === 0}
          className="pagination-btn"
        >
          ‹
        </button>
        
        {getPageNumbers().map((page) => (
          <button
            key={page}
            onClick={() => onPageChange(page)}
            className={`pagination-btn ${currentPage === page ? 'active' : ''}`}
          >
            {page + 1}
          </button>
        ))}
        
        <button
          onClick={() => onPageChange(currentPage + 1)}
          disabled={currentPage >= totalPages - 1}
          className="pagination-btn"
        >
          ›
        </button>
        <button
          onClick={() => onPageChange(totalPages - 1)}
          disabled={currentPage >= totalPages - 1}
          className="pagination-btn"
        >
          »»
        </button>
      </div>
    </div>
  )
}

export default Pagination

