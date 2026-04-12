import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './AdminHomePageLegacy.css';

const AdminHomePageLegacy = () => {
  const navigate = useNavigate();
  const { user, token } = useAuth();
  const [backendStatus, setBackendStatus] = useState('Checking...');
  const [stats] = useState({
    activeBorrows: 3,
    overdueBorrows: 1,
    totalBooks: 45,
  });

  useEffect(() => {
    const checkBackend = async () => {
      try {
        const response = await fetch('/api/v1/auth/me', {
          headers: {
            'Content-Type': 'application/json',
            ...(token && { Authorization: `Bearer ${token}` }),
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

    checkBackend();
  }, [token]);

  return (
    <div className="legacy-home-page">
      <div className="legacy-page-header">
        <h2>Welcome to Library Management System</h2>
        <p>Manage your library borrowing efficiently</p>
      </div>

      <div className="legacy-welcome-card">
        <div className="legacy-welcome-content">
          <h3>Welcome, {user?.fullName || user?.email}!</h3>
          <p>Your role: <strong>{user?.role}</strong></p>
          <p className="legacy-welcome-message">
            Start exploring our library collection and manage your borrowed books.
          </p>
          <button className="legacy-btn-start" onClick={() => navigate('/admin/books')}>
            Browse Books
          </button>
        </div>
      </div>

      <div className="legacy-stats-grid">
        <div className="legacy-stat-card">
          <div className="legacy-stat-content">
            <h4>Total Books</h4>
            <div className="legacy-stat-number">{stats.totalBooks}</div>
            <p className="legacy-stat-label">In our library collection</p>
          </div>
        </div>

        <div className="legacy-stat-card">
          <div className="legacy-stat-content">
            <h4>Active Borrows</h4>
            <div className="legacy-stat-number">{stats.activeBorrows}</div>
            <p className="legacy-stat-label">Books currently being borrowed</p>
          </div>
        </div>

        <div className="legacy-stat-card legacy-stat-warning">
          <div className="legacy-stat-content">
            <h4>Overdue Books</h4>
            <div className="legacy-stat-number">{stats.overdueBorrows}</div>
            <p className="legacy-stat-label">Books overdue for return</p>
          </div>
        </div>
      </div>

      <div className="legacy-status-section">
        <h3>System Status</h3>
        <div className="legacy-status-card">
          <div className="legacy-status-content">
            <h4>Backend Status</h4>
            <p>{backendStatus}</p>
          </div>
        </div>
      </div>

      <div className="legacy-features-section">
        <h3>Available Features</h3>
        <div className="legacy-features-grid">
          <div className="legacy-feature-card">
            <h4>Manage Books</h4>
            <p>Add, edit, or delete books from the library collection. Manage inventory.</p>
            <button className="legacy-btn-feature" onClick={() => navigate('/admin/books')}>Manage</button>
          </div>
          <div className="legacy-feature-card">
            <h4>Borrow Workflow</h4>
            <p>Track borrow and return workflows and monitor overdue records.</p>
            <button className="legacy-btn-feature" onClick={() => navigate('/admin/borrow')}>Open</button>
          </div>
          <div className="legacy-feature-card">
            <h4>User Management</h4>
            <p>Review user accounts, permissions, and account status.</p>
            <button className="legacy-btn-feature" onClick={() => navigate('/users')}>Manage Users</button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdminHomePageLegacy;
