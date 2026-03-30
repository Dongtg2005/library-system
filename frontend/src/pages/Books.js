import React, { useEffect, useMemo, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { bookAPI } from '../services/api';
import './Books.css';

const defaultForm = {
  title: '',
  author: '',
  category: '',
  status: 'AVAILABLE',
};

const Books = () => {
  const { user } = useAuth();
  const [books, setBooks] = useState([]);
  const [search, setSearch] = useState('');
  const [categoryFilter, setCategoryFilter] = useState('ALL');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(defaultForm);
  const [message, setMessage] = useState('');

  const canManage = user?.role === 'ADMIN' || user?.role === 'LIBRARIAN';

  useEffect(() => {
    const fetchBooks = async () => {
      try {
        const response = await bookAPI.getBooks();
        const data = normalizeList(response.data);
        if (data.length === 0) {
          setBooks(mockBooks);
          return;
        }
        setBooks(data);
      } catch (error) {
        setBooks(mockBooks);
        setMessage('Loaded fallback data. Connect Book Service for live data.');
      }
    };

    fetchBooks();
  }, []);

  const categories = useMemo(() => {
    const set = new Set(books.map((book) => book.category).filter(Boolean));
    return ['ALL', ...Array.from(set)];
  }, [books]);

  const filteredBooks = useMemo(() => {
    return books.filter((book) => {
      const matchSearch = `${book.title} ${book.author}`.toLowerCase().includes(search.toLowerCase());
      const matchCategory = categoryFilter === 'ALL' || book.category === categoryFilter;
      const matchStatus = statusFilter === 'ALL' || normalizeStatus(book.status) === statusFilter;
      return matchSearch && matchCategory && matchStatus;
    });
  }, [books, categoryFilter, search, statusFilter]);

  const submitForm = async (event) => {
    event.preventDefault();
    if (!canManage) {
      return;
    }

    try {
      if (editingId) {
        await bookAPI.updateBook(editingId, form);
        setBooks((prev) => prev.map((item) => (item.id === editingId ? { ...item, ...form } : item)));
        setMessage('Book updated successfully.');
      } else {
        const response = await bookAPI.createBook(form);
        const created = response?.data?.id ? response.data : { ...form, id: Date.now() };
        setBooks((prev) => [created, ...prev]);
        setMessage('Book added successfully.');
      }
      setForm(defaultForm);
      setEditingId(null);
    } catch (error) {
      setMessage('Book Service is unavailable. Local UI still updated for preview.');
      if (editingId) {
        setBooks((prev) => prev.map((item) => (item.id === editingId ? { ...item, ...form } : item)));
      } else {
        setBooks((prev) => [{ ...form, id: Date.now() }, ...prev]);
      }
      setForm(defaultForm);
      setEditingId(null);
    }
  };

  const startEdit = (book) => {
    setEditingId(book.id);
    setForm({
      title: book.title || '',
      author: book.author || '',
      category: book.category || '',
      status: normalizeStatus(book.status),
    });
  };

  const removeBook = async (id) => {
    if (!canManage) {
      return;
    }
    try {
      await bookAPI.deleteBook(id);
    } catch (error) {
      setMessage('Book deleted in UI. Sync service later.');
    }
    setBooks((prev) => prev.filter((book) => book.id !== id));
  };

  return (
    <div className="books-page">
      <section className="panel tools-panel">
        <div className="tools-row">
          <input
            value={search}
            onChange={(event) => setSearch(event.target.value)}
            placeholder="Search by title or author"
          />
          <select value={categoryFilter} onChange={(event) => setCategoryFilter(event.target.value)}>
            {categories.map((category) => (
              <option key={category} value={category}>
                {category}
              </option>
            ))}
          </select>
          <select value={statusFilter} onChange={(event) => setStatusFilter(event.target.value)}>
            <option value="ALL">All Status</option>
            <option value="AVAILABLE">Available</option>
            <option value="BORROWED">Borrowed</option>
          </select>
        </div>
        {message && <p className="panel-note">{message}</p>}
      </section>

      <section className="panel table-panel">
        <header>
          <h3>Books</h3>
          <p>{filteredBooks.length} records</p>
        </header>
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Title</th>
                <th>Author</th>
                <th>Category</th>
                <th>Status</th>
                {canManage && <th>Actions</th>}
              </tr>
            </thead>
            <tbody>
              {filteredBooks.map((book) => (
                <tr key={book.id}>
                  <td>{book.title}</td>
                  <td>{book.author}</td>
                  <td>{book.category || '-'}</td>
                  <td>
                    <span className={`status-pill ${normalizeStatus(book.status) === 'AVAILABLE' ? 'ok' : 'info'}`}>
                      {normalizeStatus(book.status)}
                    </span>
                  </td>
                  {canManage && (
                    <td>
                      <button type="button" className="action-btn" onClick={() => startEdit(book)}>
                        Edit
                      </button>
                      <button type="button" className="action-btn danger" onClick={() => removeBook(book.id)}>
                        Delete
                      </button>
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      {canManage && (
        <section className="panel form-panel">
          <h3>{editingId ? 'Edit Book' : 'Add Book'}</h3>
          <form onSubmit={submitForm}>
            <div className="form-grid">
              <input
                value={form.title}
                onChange={(event) => setForm((prev) => ({ ...prev, title: event.target.value }))}
                placeholder="Title"
                required
              />
              <input
                value={form.author}
                onChange={(event) => setForm((prev) => ({ ...prev, author: event.target.value }))}
                placeholder="Author"
                required
              />
              <input
                value={form.category}
                onChange={(event) => setForm((prev) => ({ ...prev, category: event.target.value }))}
                placeholder="Category"
                required
              />
              <select
                value={form.status}
                onChange={(event) => setForm((prev) => ({ ...prev, status: event.target.value }))}
              >
                <option value="AVAILABLE">Available</option>
                <option value="BORROWED">Borrowed</option>
              </select>
            </div>
            <div className="form-actions">
              <button type="submit" className="primary-btn">
                {editingId ? 'Update Book' : 'Create Book'}
              </button>
              {editingId && (
                <button
                  type="button"
                  className="ghost-btn"
                  onClick={() => {
                    setEditingId(null);
                    setForm(defaultForm);
                  }}
                >
                  Cancel
                </button>
              )}
            </div>
          </form>
        </section>
      )}
    </div>
  );
};

const normalizeStatus = (status) => (status || 'AVAILABLE').toUpperCase();

const normalizeList = (data) => {
  if (Array.isArray(data)) {
    return data;
  }
  if (Array.isArray(data?.content)) {
    return data.content;
  }
  return [];
};

const mockBooks = [
  { id: 1, title: 'Atomic Habits', author: 'James Clear', category: 'Self Help', status: 'AVAILABLE' },
  { id: 2, title: 'Clean Architecture', author: 'Robert C. Martin', category: 'Software', status: 'BORROWED' },
  { id: 3, title: 'Sapiens', author: 'Yuval Noah Harari', category: 'History', status: 'AVAILABLE' },
  { id: 4, title: 'Deep Work', author: 'Cal Newport', category: 'Productivity', status: 'AVAILABLE' },
];

export default Books;
