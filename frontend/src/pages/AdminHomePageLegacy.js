import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useTranslation } from '../context/LanguageContext';
import './AdminHomePageLegacy.css';

const AdminHomePageLegacy = () => {
  const navigate = useNavigate();
  const { user, token } = useAuth();
  const { t } = useTranslation();
  const [backendStatus, setBackendStatus] = useState(t('adminDashboard.checking'));
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
          setBackendStatus(t('adminDashboard.connected'));
        } else {
          setBackendStatus(t('adminDashboard.error', { status: response.status }));
        }
      } catch (error) {
        setBackendStatus(t('adminDashboard.error', { status: error.message }));
      }
    };

    checkBackend();
  }, [token, t]);

  return (
    <div className="legacy-home-page">
      <div className="legacy-page-header">
        <h2>{t('adminDashboard.controlPanel')}</h2>
        <p>{t('adminDashboard.manageLibrary')}</p>
      </div>

      <div className="legacy-welcome-card">
        <div className="legacy-welcome-content">
          <h3>{t('adminDashboard.welcomeBack', { name: user?.fullName || user?.email })}</h3>
          <p>{t('auth.role')}: <strong>{user?.role}</strong></p>
          <p className="legacy-welcome-message">
            {t('adminHomePageLegacy.startExploring')}
          </p>
          <button className="legacy-btn-start" onClick={() => navigate('/admin/books')}>
            {t('adminHomePageLegacy.browseBooks')}
          </button>
        </div>
      </div>

      <div className="legacy-stats-grid">
        <div className="legacy-stat-card">
          <div className="legacy-stat-content">
            <h4>{t('adminDashboard.totalBooks')}</h4>
            <div className="legacy-stat-number">{stats.totalBooks}</div>
            <p className="legacy-stat-label">{t('adminHomePageLegacy.inLibraryCollection')}</p>
          </div>
        </div>

        <div className="legacy-stat-card">
          <div className="legacy-stat-content">
            <h4>{t('adminDashboard.activeBorrows')}</h4>
            <div className="legacy-stat-number">{stats.activeBorrows}</div>
            <p className="legacy-stat-label">{t('adminHomePageLegacy.booksCurrentlyBorrowed')}</p>
          </div>
        </div>

        <div className="legacy-stat-card legacy-stat-warning">
          <div className="legacy-stat-content">
            <h4>{t('adminDashboard.overdueItems')}</h4>
            <div className="legacy-stat-number">{stats.overdueBorrows}</div>
            <p className="legacy-stat-label">{t('adminHomePageLegacy.booksOverdueReturn')}</p>
          </div>
        </div>
      </div>

      <div className="legacy-status-section">
        <h3>{t('adminDashboard.systemStatus')}</h3>
        <div className="legacy-status-card">
          <div className="legacy-status-content">
            <h4>{t('adminDashboard.backend')}</h4>
            <p>{backendStatus}</p>
          </div>
        </div>
      </div>

      <div className="legacy-features-section">
        <h3>{t('adminDashboard.systemSettings')}</h3>
        <div className="legacy-features-grid">
          <div className="legacy-feature-card">
            <h4>{t('adminDashboard.bookManagement')}</h4>
            <p>{t('adminHomePageLegacy.bookManagementDesc')}</p>
            <button className="legacy-btn-feature" onClick={() => navigate('/admin/books')}>
              {t('adminDashboard.bookManagement')}
            </button>
          </div>
          <div className="legacy-feature-card">
            <h4>{t('adminDashboard.borrowWorkflow')}</h4>
            <p>{t('adminHomePageLegacy.borrowWorkflowDesc')}</p>
            <button className="legacy-btn-feature" onClick={() => navigate('/admin/borrow')}>
              {t('adminDashboard.borrowWorkflow')}
            </button>
          </div>
          <div className="legacy-feature-card">
            <h4>{t('adminDashboard.userManagement')}</h4>
            <p>{t('adminHomePageLegacy.userManagementDesc')}</p>
            <button className="legacy-btn-feature" onClick={() => navigate('/users')}>
              {t('adminDashboard.userManagement')}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdminHomePageLegacy;
