import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import './Dashboard.css';

const Dashboard = () => {
  const { user, logout, token } = useAuth();
  const [backendStatus, setBackendStatus] = useState('Checking...');
  const [apiError, setApiError] = useState('');

  useEffect(() => {
    const checkBackend = async () => {
      try {
        const response = await fetch('/api/actuator/health', {
          headers: {
            'Content-Type': 'application/json',
            ...(token && { 'Authorization': `Bearer ${token}` }),
          },
        });

        if (response.ok) {
          const data = await response.json();
          setBackendStatus(`Connected - Status: ${data.status}`);
        } else if (response.status === 401) {
          setApiError('Authentication required for API');
          setBackendStatus('Unauthorized');
        } else {
          setBackendStatus(`Error: HTTP ${response.status}`);
        }
      } catch (error) {
        setBackendStatus(`Error: ${error.message}`);
        setApiError(error.message);
      }
    };

    checkBackend();
  }, [token]);

  return (
    <div className="dashboard">
      <header className="dashboard-header">
        <div className="header-left">
          <h1>Library Management System</h1>
        </div>
        <div className="header-right">
          {user && (
            <>
              <span className="user-info">Welcome, {user.fullName || user.email}</span>
              <button onClick={logout} className="logout-btn">
                Logout
              </button>
            </>
          )}
        </div>
      </header>

      <main className="dashboard-content">
        <div className="status-card">
          <h2>Backend Status</h2>
          <div className="status-info">
            <p className="status-badge">
              {backendStatus.includes('Connected') ? '✅' : '❌'} {backendStatus}
            </p>
            {apiError && <p className="error-text">{apiError}</p>}
          </div>
        </div>

        <div className="user-card">
          <h2>Your Information</h2>
          {user && (
            <div className="user-details">
              <p><strong>Name:</strong> {user.fullName || 'N/A'}</p>
              <p><strong>Email:</strong> {user.email}</p>
              <p><strong>Role:</strong> {user.role || 'USER'}</p>
            </div>
          )}
        </div>

        <div className="info-card">
          <h2>Available Features</h2>
          <ul>
            <li>📚 Book Management</li>
            <li>👤 User Management</li>
            <li>🔄 Book Borrowing</li>
            <li>📖 Inventory Tracking</li>
          </ul>
        </div>
      </main>
    </div>
  );
};

export default Dashboard;
