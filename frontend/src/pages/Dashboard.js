import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { borrowAPI } from '../services/api';
import './Dashboard.css';

const Dashboard = () => {
  const { user } = useAuth();
  const [borrowedBooks, setBorrowedBooks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchBorrowedBooks = async () => {
      try {
        setLoading(true);
        const response = await borrowAPI.getBorrowedBooks();
        setBorrowedBooks(response.data || []);
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    if (user) {
      fetchBorrowedBooks();
    }
  }, [user]);

  return (
    <div className="dashboard-container">
      <div className="welcome-section">
        <h1>Xin chào, {user?.fullName}! 👋</h1>
        <p>Chào mừng bạn đến với Hệ Thống Quản Lý Thư Viện</p>
      </div>

      <div className="dashboard-grid">
        {/* Stats Cards */}
        <div className="stats-grid">
          <div className="stat-card">
            <div className="stat-icon">📚</div>
            <div className="stat-content">
              <h3>Sách Đã Mượn</h3>
              <p className="stat-number">{borrowedBooks.length}</p>
            </div>
          </div>

          <div className="stat-card">
            <div className="stat-icon">👤</div>
            <div className="stat-content">
              <h3>Vai Trò</h3>
              <p className="stat-value">{user?.role || 'N/A'}</p>
            </div>
          </div>

          <div className="stat-card">
            <div className="stat-icon">✉️</div>
            <div className="stat-content">
              <h3>Email</h3>
              <p className="stat-value">{user?.email}</p>
            </div>
          </div>
        </div>

        {/* Borrowed Books Section */}
        <div className="section">
          <h2>📖 Sách Đang Mượn</h2>
          
          {loading && <p className="loading">Đang tải...</p>}
          {error && <p className="error">{error}</p>}
          
          {!loading && borrowedBooks.length === 0 && (
            <div className="empty-state">
              <p>Bạn chưa mượn sách nào</p>
              <a href="/books" className="btn btn-primary">Tìm sách</a>
            </div>
          )}

          {!loading && borrowedBooks.length > 0 && (
            <table className="books-table">
              <thead>
                <tr>
                  <th>Tên Sách</th>
                  <th>Tác Giả</th>
                  <th>Ngày Mượn</th>
                  <th>Hạn Trả</th>
                  <th>Trạng Thái</th>
                </tr>
              </thead>
              <tbody>
                {borrowedBooks.map((book) => (
                  <tr key={book.borrowId}>
                    <td>{book.bookTitle}</td>
                    <td>{book.author}</td>
                    <td>{new Date(book.borrowDate).toLocaleDateString('vi-VN')}</td>
                    <td>{new Date(book.returnDate).toLocaleDateString('vi-VN')}</td>
                    <td>
                      <span className={`status-badge status-${book.status?.toLowerCase()}`}>
                        {book.status}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>

        {/* Quick Links */}
        <div className="section">
          <h2>⚡ Liên Kết Nhanh</h2>
          <div className="quick-links">
            <a href="/books" className="quick-link-card">
              <div className="link-icon">🔍</div>
              <h3>Tìm Sách</h3>
              <p>Khám phá bộ sưu tập sách của thư viện</p>
            </a>
            <a href="/account" className="quick-link-card">
              <div className="link-icon">⚙️</div>
              <h3>Cài Đặt Tài Khoản</h3>
              <p>Quản lý thông tin tài khoản của bạn</p>
            </a>
            <a href="/history" className="quick-link-card">
              <div className="link-icon">📜</div>
              <h3>Lịch Sử Mượn</h3>
              <p>Xem lịch sử mượn sách của bạn</p>
            </a>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
