import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import './MyBorrowsPage.css';

const MyBorrowsPage = ({ currentPage, setCurrentPage }) => {
  const { token } = useAuth();
  const [borrows, setBorrows] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [activeTab, setActiveTab] = useState('active'); // active, overdue, returned

  useEffect(() => {
    if (currentPage === 'myborrow') {
      fetchBorrows();
    }
  }, [currentPage, token]);

  const fetchBorrows = async () => {
    try {
      setLoading(true);
      setError('');

      // Mock borrow data
      const mockBorrows = [
        {
          id: '1',
          bookId: '1',
          bookTitle: 'Dế Mèn Là Vợ Tôi',
          borrowDate: '2024-03-20',
          dueDate: '2024-04-03',
          returnDate: null,
          status: 'ACTIVE',
          daysRemaining: 3,
          fine: 0,
        },
        {
          id: '2',
          bookId: '2',
          bookTitle: 'Số Đỏ',
          borrowDate: '2024-03-10',
          dueDate: '2024-03-24',
          returnDate: null,
          status: 'OVERDUE',
          daysRemaining: -16,
          fine: 16000, // 1000 per day
        },
        {
          id: '3',
          bookId: '3',
          bookTitle: 'Chí Phèo',
          borrowDate: '2024-02-15',
          dueDate: '2024-03-01',
          returnDate: '2024-03-01',
          status: 'RETURNED',
          daysRemaining: 0,
          fine: 0,
        },
      ];

      setBorrows(mockBorrows);
    } catch (err) {
      setError('Failed to load borrows: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  const getStatusBadge = (status) => {
    switch (status) {
      case 'ACTIVE':
        return <span className="badge badge-active">Active</span>;
      case 'OVERDUE':
        return <span className="badge badge-overdue">Overdue</span>;
      case 'RETURNED':
        return <span className="badge badge-returned">Returned</span>;
      default:
        return <span className="badge">{status}</span>;
    }
  };

  const filteredBorrows = borrows.filter(borrow => {
    if (activeTab === 'active') return borrow.status === 'ACTIVE';
    if (activeTab === 'overdue') return borrow.status === 'OVERDUE';
    if (activeTab === 'returned') return borrow.status === 'RETURNED';
    return true;
  });

  if (currentPage !== 'myborrow') return null;

  return (
    <div className="my-borrows-page">
      <div className="page-header">
        <h2>📖 My Borrowed Books</h2>
        <p>Manage your book borrowing and returns</p>
      </div>

      {error && <div className="error-message">{error}</div>}

      <div className="tabs">
        <button
          className={`tab ${activeTab === 'active' ? 'active' : ''}`}
          onClick={() => setActiveTab('active')}
        >
          📚 Active ({borrows.filter(b => b.status === 'ACTIVE').length})
        </button>
        <button
          className={`tab ${activeTab === 'overdue' ? 'active' : ''}`}
          onClick={() => setActiveTab('overdue')}
        >
          ⚠️ Overdue ({borrows.filter(b => b.status === 'OVERDUE').length})
        </button>
        <button
          className={`tab ${activeTab === 'returned' ? 'active' : ''}`}
          onClick={() => setActiveTab('returned')}
        >
          ✓ Returned ({borrows.filter(b => b.status === 'RETURNED').length})
        </button>
      </div>

      {loading ? (
        <div className="loading">Loading your borrows...</div>
      ) : (
        <>
          {filteredBorrows.length === 0 ? (
            <div className="no-results">
              {activeTab === 'active' && 'You have no active borrows.'}
              {activeTab === 'overdue' && 'You have no overdue borrows.'}
              {activeTab === 'returned' && 'You have no returned books yet.'}
            </div>
          ) : (
            <div className="borrows-table-wrapper">
              <table className="borrows-table">
                <thead>
                  <tr>
                    <th>Book Title</th>
                    <th>Borrow Date</th>
                    <th>Due Date</th>
                    <th>Status</th>
                    {activeTab === 'overdue' && <th>Fine</th>}
                    {activeTab === 'active' && <th>Days Left</th>}
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredBorrows.map(borrow => (
                    <tr key={borrow.id} className={`row-${borrow.status.toLowerCase()}`}>
                      <td className="title-cell">{borrow.bookTitle}</td>
                      <td>{new Date(borrow.borrowDate).toLocaleDateString()}</td>
                      <td>{new Date(borrow.dueDate).toLocaleDateString()}</td>
                      <td>{getStatusBadge(borrow.status)}</td>
                      {activeTab === 'overdue' && (
                        <td className="fine-cell">
                          <span className="fine-amount">
                            ₫{borrow.fine.toLocaleString('vi-VN')}
                          </span>
                        </td>
                      )}
                      {activeTab === 'active' && (
                        <td className={borrow.daysRemaining <= 3 ? 'urgent' : ''}>
                          {borrow.daysRemaining} day(s)
                        </td>
                      )}
                      <td>
                        {borrow.status === 'ACTIVE' && (
                          <button className="btn-return">Return Book</button>
                        )}
                        {borrow.status === 'OVERDUE' && (
                          <button className="btn-return urgent">Return Now</button>
                        )}
                        {borrow.status === 'RETURNED' && (
                          <button className="btn-borrow-again">Borrow Again</button>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </>
      )}

      {/* Fine Summary Card */}
      {borrows.some(b => b.status === 'OVERDUE') && (
        <div className="fine-summary">
          <h3>Outstanding Fines</h3>
          <p className="fine-total">
            Total Fine: <span className="amount">
              ₫{borrows
                .filter(b => b.status === 'OVERDUE')
                .reduce((sum, b) => sum + b.fine, 0)
                .toLocaleString('vi-VN')}
            </span>
          </p>
          <p className="fine-note">Please return your overdue books to avoid additional fines.</p>
          <button className="btn-pay-fine">Pay Fine</button>
        </div>
      )}
    </div>
  );
};

export default MyBorrowsPage;
