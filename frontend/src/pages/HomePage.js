import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import './HomePage.css';

const HomePage = ({ currentPage, setCurrentPage }) => {
  const { user, token } = useAuth();
  const [backendStatus, setBackendStatus] = useState('Checking...');
  const [stats, setStats] = useState({
    activeBorrows: 3,
    overdueBorrows: 1,
    totalBooks: 45,
  });

  useEffect(() => {
    if (currentPage === 'home') {
      checkBackend();
    }
  }, [currentPage, token]);

  const checkBackend = async () => {
    try {
      const response = await fetch('/api/v1/auth/me', {
        headers: {
          'Content-Type': 'application/json',
          ...(token && { 'Authorization': `Bearer ${token}` }),
        },
      });

      if (response.ok) {
        setBackendStatus('Connected - API is working');
      } else {
        setBackendStatus(`Error: HTTP ${response.status}`);
      }
    } catch (error) {
      setBackendStatus(`Error: ${error.message}`);
    }
  };

  if (currentPage !== 'home') return null;

  return (
    <div className="home-page">
      <div className="page-header">
        <h2>🏠 Welcome to Library Management System</h2>
        <p>Manage your library borrowing efficiently</p>
      </div>

      {/* User Welcome Card */}
      <div className="welcome-card">
        <div className="welcome-content">
          <h3>Welcome, {user?.fullName || user?.email}!</h3>
          <p>Your role: <strong>{user?.role}</strong></p>
          <p className="welcome-message">
            Start exploring our library collection and manage your borrowed books.
          </p>
          <button 
            className="btn-start"
            onClick={() => setCurrentPage('books')}
          >
            Browse Books →
          </button>
        </div>
      </div>

      {/* Quick Stats */}
      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-icon">📚</div>
          <div className="stat-content">
            <h4>Total Books</h4>
            <div className="stat-number">{stats.totalBooks}</div>
            <p className="stat-label">In our library collection</p>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon">📖</div>
          <div className="stat-content">
            <h4>Active Borrows</h4>
            <div className="stat-number">{stats.activeBorrows}</div>
            <p className="stat-label">Books you're currently borrowing</p>
          </div>
        </div>

        <div className="stat-card stat-warning">
          <div className="stat-icon">⚠️</div>
          <div className="stat-content">
            <h4>Overdue Books</h4>
            <div className="stat-number">{stats.overdueBorrows}</div>
            <p className="stat-label">Books overdue for return</p>
          </div>
        </div>
      </div>

      {/* Backend Status */}
      <div className="status-section">
        <h3>System Status</h3>
        <div className="status-card">
          <div className="status-indicator">
            {backendStatus.includes('Connected') ? '✅' : '❌'}
          </div>
          <div className="status-content">
            <h4>Backend Status</h4>
            <p>{backendStatus}</p>
          </div>
        </div>
      </div>

      {/* Features Overview */}
      <div className="features-section">
        <h3>Available Features</h3>
        <div className="features-grid">
          <div className="feature-card">
            <div className="feature-icon">📚</div>
            <h4>Browse Books</h4>
            <p>Explore our collection of books, search by title or author, and view detailed information.</p>
            <button 
              className="btn-feature"
              onClick={() => setCurrentPage('books')}
            >
              View Books
            </button>
          </div>

          <div className="feature-card">
            <div className="feature-icon">📖</div>
            <h4>My Borrows</h4>
            <p>Track your borrowed books, due dates, and manage your borrowing history.</p>
            <button 
              className="btn-feature"
              onClick={() => setCurrentPage('myborrow')}
            >
              View Borrows
            </button>
          </div>

          {(user?.role === 'ADMIN' || user?.role === 'LIBRARIAN') && (
            <>
              <div className="feature-card">
                <div className="feature-icon">⚙️</div>
                <h4>Manage Books</h4>
                <p>Add, edit, or delete books from the library collection. Manage inventory.</p>
                <button className="btn-feature">Manage</button>
              </div>

              <div className="feature-card">
                <div className="feature-icon">👥</div>
                <h4>User Management</h4>
                <p>View user accounts, manage roles, and handle user-related issues.</p>
                <button className="btn-feature">Manage Users</button>
              </div>
            </>
          )}
        </div>
      </div>

      {/* Recent Activity */}
      <div className="recent-section">
        <h3>Recent Activity</h3>
        <div className="activity-list">
          <div className="activity-item">
            <span className="activity-time">Today</span>
            <span className="activity-text">You borrowed "Dế Mèn Là Vợ Tôi"</span>
          </div>
          <div className="activity-item">
            <span className="activity-time">yesterday</span>
            <span className="activity-text">You returned "Chí Phèo"</span>
          </div>
          <div className="activity-item urgent">
            <span className="activity-time">3 days ago</span>
            <span className="activity-text">Book "Số Đỏ" is now overdue</span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default HomePage;
