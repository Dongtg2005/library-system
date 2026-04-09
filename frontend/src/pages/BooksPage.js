import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import './BooksPage.css';

const BooksPage = ({ currentPage, setCurrentPage }) => {
  const { token } = useAuth();
  const [books, setBooks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedBook, setSelectedBook] = useState(null);

  useEffect(() => {
    if (currentPage === 'books') {
      fetchBooks();
    }
  }, [currentPage, token]);

  const fetchBooks = async () => {
    try {
      setLoading(true);
      setError('');
      
      // Mock books data - replace with actual API when backend ready
      const mockBooks = [
        {
          id: '1',
          isbn: '978-3-16-148410-0',
          title: 'Dế Mèn Là Vợ Tôi',
          author: 'Nguyễn Nhật Ánh',
          publisher: 'NXB Văn Học',
          pages: 320,
          availableQuantity: 5,
          totalQuantity: 10,
          averageRating: 4.5,
          ratingCount: 12,
          coverImageUrl: '📚',
          description: 'Một tác phẩm kinh điển của văn học Việt Nam',
        },
        {
          id: '2',
          isbn: '978-3-16-148411-0',
          title: 'Số Đỏ',
          author: 'Bùi Anh Tấn',
          publisher: 'NXB Văn Học',
          pages: 280,
          availableQuantity: 3,
          totalQuantity: 8,
          averageRating: 4.2,
          ratingCount: 8,
          coverImageUrl: '📖',
          description: 'Tác phẩm tiêu biểu về tình yêu và con người',
        },
        {
          id: '3',
          isbn: '978-3-16-148412-0',
          title: 'Chí Phèo',
          author: 'Nam Cao',
          publisher: 'NXB Văn Học',
          pages: 250,
          availableQuantity: 0,
          totalQuantity: 15,
          averageRating: 4.8,
          ratingCount: 25,
          coverImageUrl: '📘',
          description: 'Câu chuyện đau lòng về cuộc đời của một nhân vật',
        },
      ];

      setBooks(mockBooks);
    } catch (err) {
      setError('Failed to load books: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  const filteredBooks = books.filter(book =>
    book.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
    book.author.toLowerCase().includes(searchTerm.toLowerCase())
  );

  if (currentPage !== 'books') return null;

  return (
    <div className="books-page">
      <div className="page-header">
        <h2>📚 Browse Library Books</h2>
        <p>Discover and borrow books from our collection</p>
      </div>

      {error && <div className="error-message">{error}</div>}

      <div className="search-bar">
        <input
          type="text"
          placeholder="Search by title or author..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className="search-input"
        />
      </div>

      {loading ? (
        <div className="loading">Loading books...</div>
      ) : (
        <>
          <div className="books-count">
            Found {filteredBooks.length} book(s)
          </div>

          {filteredBooks.length === 0 ? (
            <div className="no-results">
              No books found matching your search.
            </div>
          ) : (
            <div className="books-grid">
              {filteredBooks.map(book => (
                <div key={book.id} className="book-card">
                  <div className="book-cover">{book.coverImageUrl}</div>
                  <div className="book-info">
                    <h3 className="book-title">{book.title}</h3>
                    <p className="book-author">by {book.author}</p>
                    <div className="book-meta">
                      <span className="rating">
                        ⭐ {book.averageRating} ({book.ratingCount} reviews)
                      </span>
                    </div>
                    <div className="book-availability">
                      <span className={book.availableQuantity > 0 ? 'available' : 'unavailable'}>
                        {book.availableQuantity > 0 
                          ? `${book.availableQuantity} available` 
                          : 'Out of stock'}
                      </span>
                    </div>
                    <button
                      className={`btn-borrow ${book.availableQuantity > 0 ? '' : 'disabled'}`}
                      disabled={book.availableQuantity <= 0}
                      onClick={() => setSelectedBook(book)}
                    >
                      {book.availableQuantity > 0 ? 'Borrow' : 'Request Reservation'}
                    </button>
                    <button
                      className="btn-details"
                      onClick={() => setSelectedBook(book)}
                    >
                      View Details
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </>
      )}

      {/* Book Detail Modal */}
      {selectedBook && (
        <div className="modal-overlay" onClick={() => setSelectedBook(null)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <button className="close-btn" onClick={() => setSelectedBook(null)}>✕</button>
            <div className="modal-book-detail">
              <div className="modal-cover">{selectedBook.coverImageUrl}</div>
              <div className="modal-info">
                <h2>{selectedBook.title}</h2>
                <p className="author">by {selectedBook.author}</p>
                <p className="publisher">Publisher: {selectedBook.publisher}</p>
                <p className="isbn">ISBN: {selectedBook.isbn}</p>
                <p className="pages">Pages: {selectedBook.pages}</p>
                <p className="description">{selectedBook.description}</p>
                <div className="modal-availability">
                  <strong>Availability: </strong>
                  {selectedBook.availableQuantity}/{selectedBook.totalQuantity} books available
                </div>
                <div className="modal-actions">
                  <button
                    className={`btn-primary ${selectedBook.availableQuantity > 0 ? '' : 'disabled'}`}
                    disabled={selectedBook.availableQuantity <= 0}
                  >
                    Borrow This Book
                  </button>
                  <button className="btn-secondary" onClick={() => setSelectedBook(null)}>
                    Close
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default BooksPage;
