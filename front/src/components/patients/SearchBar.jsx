import { useState } from 'react'
import './SearchBar.css'

const SearchBar = ({ onSearch, placeholder = 'Rechercher...' }) => {
  const [searchTerm, setSearchTerm] = useState('')

  const handleSubmit = (e) => {
    e.preventDefault()
    onSearch(searchTerm)
  }

  const handleClear = () => {
    setSearchTerm('')
    onSearch('')
  }

  return (
    <form onSubmit={handleSubmit} className="search-bar">
      <input
        type="text"
        value={searchTerm}
        onChange={(e) => setSearchTerm(e.target.value)}
        placeholder={placeholder}
        className="search-input"
      />
      {searchTerm && (
        <button type="button" onClick={handleClear} className="search-clear">
          ×
        </button>
      )}
      <button type="submit" className="search-button">
        🔍 Rechercher
      </button>
    </form>
  )
}

export default SearchBar

