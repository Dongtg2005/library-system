import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { useTranslation } from '../context/LanguageContext';
import './Dashboard.css';

const Dashboard = () => {
  const { user, logout, token } = useAuth();
  const { t } = useTranslation();
  const [backendStatus, setBackendStatus] = useState(t('dashboard.checking'));
  const [apiError, setApiError] = useState('');

  useEffect(() => {
    const checkBackend = async () => {
      try {
        const response = await fetch('/api/v1/auth/me', {
          headers: {
            'Content-Type': 'application/json',
            ...(token && { 'Authorization': `Bearer ${token}` }),
          },
        });

        if (response.ok) {
          setBackendStatus(t('dashboard.connected'));
        } else if (response.status === 401) {
          setApiError(t('dashboard.authRequired'));
          setBackendStatus(t('dashboard.connectedUnauthorized'));
        } else {
          setBackendStatus(t('dashboard.error', { status: response.status }));
        }
      } catch (error) {
        setBackendStatus(t('dashboard.error', { status: error.message }));
        setApiError(error.message);
      }
    };

    if (token) {
      checkBackend();
    }
  }, [token, t]);

  return (
    <div className="dashboard">
      <header className="dashboard-header">
        <div className="header-left">
          <h1>{t('dashboard.libraryManagementSystem')}</h1>
        </div>
        <div className="header-right">
          {user && (
            <>
              <span className="user-info">{t('dashboard.welcome', { name: user.fullName || user.email })}</span>
              <button onClick={logout} className="logout-btn">
                {t('auth.logout')}
              </button>
            </>
          )}
        </div>
      </header>

      <main className="dashboard-content">
        <div className="status-card">
          <h2>{t('dashboard.backendStatus')}</h2>
          <div className="status-info">
            <p className="status-badge">
              {backendStatus.includes(t('dashboard.connected')) ? t('dashboard.online') : t('dashboard.offline')} {backendStatus}
            </p>
            {apiError && <p className="error-text">{apiError}</p>}
          </div>
        </div>

        <div className="user-card">
          <h2>{t('dashboard.yourInformation')}</h2>
          {user && (
            <div className="user-details">
              <p><strong>{t('auth.name')}:</strong> {user.fullName || 'N/A'}</p>
              <p><strong>{t('auth.email')}:</strong> {user.email}</p>
              <p><strong>{t('auth.role')}:</strong> {user.role || 'USER'}</p>
            </div>
          )}
        </div>

        <div className="info-card">
          <h2>{t('dashboard.availableFeatures')}</h2>
          <ul>
            <li>{t('dashboard.bookManagement')}</li>
            <li>{t('dashboard.userManagement')}</li>
            <li>{t('dashboard.bookBorrowing')}</li>
            <li>{t('dashboard.inventoryTracking')}</li>
          </ul>
        </div>
      </main>
    </div>
  );
};

export default Dashboard;
