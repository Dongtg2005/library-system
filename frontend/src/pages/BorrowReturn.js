import React, { useEffect, useMemo, useState } from 'react';
import { bookAPI, borrowAPI } from '../services/api';
import './BorrowReturn.css';

const BorrowReturn = () => {
  const [records, setRecords] = useState([]);
  const [books, setBooks] = useState([]);
  const [selectedBookId, setSelectedBookId] = useState('');
  const [message, setMessage] = useState('');

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [historyRes, booksRes] = await Promise.all([
          borrowAPI.getBorrowHistory(),
          bookAPI.getBooks(),
        ]);

        const historyData = normalizeList(historyRes.data);
        const booksData = normalizeList(booksRes.data);

        setRecords(historyData.length ? historyData : mockRecords);
        setBooks(booksData.length ? booksData : mockBooks);
      } catch (error) {
        setRecords(mockRecords);
        setBooks(mockBooks);
        setMessage('Loaded sample transactions. Connect Borrow Service for live mode.');
      }
    };

    fetchData();
  }, []);

  const availableBooks = useMemo(() => books.filter((book) => normalizeStatus(book.status) === 'AVAILABLE'), [books]);

  const borrowBook = async () => {
    if (!selectedBookId) {
      return;
    }

    const selected = books.find((book) => String(book.id) === selectedBookId);
    if (!selected) {
      return;
    }

    const entry = {
      id: Date.now(),
      userName: 'Current User',
      bookTitle: selected.title,
      borrowDate: new Date().toISOString(),
      returnDate: new Date(Date.now() + 1000 * 60 * 60 * 24 * 14).toISOString(),
      status: 'BORROWED',
    };

    try {
      await borrowAPI.borrowBook(selectedBookId);
      setMessage('Borrow request sent successfully.');
    } catch (error) {
      setMessage('Borrow API unavailable. Transaction simulated in UI.');
    }

    setRecords((prev) => [entry, ...prev]);
    setBooks((prev) => prev.map((book) => (String(book.id) === selectedBookId ? { ...book, status: 'BORROWED' } : book)));
    setSelectedBookId('');
  };

  const returnBook = async (recordId) => {
    try {
      await borrowAPI.returnBook(recordId);
    } catch (error) {
      setMessage('Return API unavailable. Transaction updated locally.');
    }

    setRecords((prev) =>
      prev.map((record) =>
        record.id === recordId || record.borrowId === recordId
          ? {
              ...record,
              status: 'RETURNED',
              returnDate: new Date().toISOString(),
            }
          : record
      )
    );
  };

  return (
    <div className="borrow-page">
      <section className="panel action-panel">
        <h3>Borrow a Book</h3>
        <div className="borrow-form">
          <select value={selectedBookId} onChange={(event) => setSelectedBookId(event.target.value)}>
            <option value="">Select available book</option>
            {availableBooks.map((book) => (
              <option key={book.id} value={book.id}>
                {book.title} - {book.author}
              </option>
            ))}
          </select>
          <button type="button" className="primary-btn" onClick={borrowBook}>
            Borrow
          </button>
        </div>
        {message && <p className="panel-note">{message}</p>}
      </section>

      <section className="panel table-panel">
        <header>
          <h3>Borrowing Records</h3>
          <p>Track borrow and return history</p>
        </header>

        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>User</th>
                <th>Book</th>
                <th>Borrow Date</th>
                <th>Return Date</th>
                <th>Status</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {records.map((record, index) => {
                const status = normalizeStatus(record.status);
                const overdue = isOverdue(record);
                const id = record.id || record.borrowId || index;

                return (
                  <tr key={id} className={overdue ? 'row-overdue' : ''}>
                    <td>{record.userName || record.userEmail || '-'}</td>
                    <td>{record.bookTitle || '-'}</td>
                    <td>{formatDate(record.borrowDate)}</td>
                    <td>{formatDate(record.returnDate || record.dueDate)}</td>
                    <td>
                      <span className={`status-pill ${overdue ? 'danger' : status === 'RETURNED' ? 'ok' : 'info'}`}>
                        {overdue ? 'OVERDUE' : status}
                      </span>
                    </td>
                    <td>
                      {status === 'BORROWED' ? (
                        <button type="button" className="action-btn" onClick={() => returnBook(id)}>
                          Return
                        </button>
                      ) : (
                        <span className="muted">Completed</span>
                      )}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </section>
    </div>
  );
};

const normalizeList = (data) => {
  if (Array.isArray(data)) {
    return data;
  }
  if (Array.isArray(data?.content)) {
    return data.content;
  }
  return [];
};

const normalizeStatus = (status) => (status || 'BORROWED').toUpperCase();

const formatDate = (value) => {
  if (!value) {
    return '-';
  }
  return new Date(value).toLocaleDateString('en-GB');
};

const isOverdue = (record) => {
  const dueDate = record.returnDate || record.dueDate;
  if (!dueDate || normalizeStatus(record.status) === 'RETURNED') {
    return false;
  }
  return new Date(dueDate).getTime() < Date.now();
};

const mockBooks = [
  { id: 11, title: 'Refactoring', author: 'Martin Fowler', status: 'AVAILABLE' },
  { id: 12, title: 'Domain-Driven Design', author: 'Eric Evans', status: 'AVAILABLE' },
  { id: 13, title: 'Code Complete', author: 'Steve McConnell', status: 'BORROWED' },
];

const mockRecords = [
  {
    id: 801,
    userName: 'Le Minh',
    bookTitle: 'Refactoring',
    borrowDate: '2026-03-10T00:00:00.000Z',
    returnDate: '2026-03-24T00:00:00.000Z',
    status: 'BORROWED',
  },
  {
    id: 802,
    userName: 'Tran Anh',
    bookTitle: 'Code Complete',
    borrowDate: '2026-03-01T00:00:00.000Z',
    returnDate: '2026-03-15T00:00:00.000Z',
    status: 'RETURNED',
  },
];

export default BorrowReturn;
