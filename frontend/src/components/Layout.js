import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useTranslation } from '../context/LanguageContext';
import './Layout.css';

const Layout = ({ children }) => {
  const { user, logout } = useAuth();
  const { t } = useTranslation();
  const [sidebarOpen, setSidebarOpen] = useState(true);
  const [currentPage, setCurrentPage] = useState('home');

  const menuItems = [
    { id: 'home', label: t('nav.dashboard'), page: 'home' },
    { id: 'books', label: t('nav.books'), page: 'books' },
    { id: 'myborrow', label: t('nav.myBooks'), page: 'myborrow' },
    ...(user?.role === 'ADMIN' || user?.role === 'LIBRARIAN' ? [
      { id: 'manage', label: t('bookTable.title'), page: 'manage' },
      { id: 'users', label: t('nav.users'), page: 'users' },
    ] : []),
  ];

  const handleLogout = async () => {
    await logout();
  };

  return (
    <div className="layout">
      {/* Header */}
      <header className="header">
        <div className="header-left">
          <button 
            className="menu-toggle"
            onClick={() => setSidebarOpen(!sidebarOpen)}
            title={t('sidebar.toggle')}
          >
            ☰
          </button>
          <h1 className="app-title">{t('sidebar.library')}</h1>
        </div>
        <div className="header-right">
          <span className="user-welcome">{t('auth.welcomeBack', { name: user?.fullName || user?.email })}</span>
          <button onClick={handleLogout} className="logout-btn">
            {t('auth.logout')}
          </button>
        </div>
      </header>

      <div className="container">
        {/* Sidebar */}
        <aside className={`sidebar ${sidebarOpen ? 'open' : 'closed'}`}>
          <nav className="nav-menu">
            {menuItems.map(item => (
              <button
                key={item.id}
                className={`nav-item ${currentPage === item.page ? 'active' : ''}`}
                onClick={() => setCurrentPage(item.page)}
              >
                {item.label}
              </button>
            ))}
          </nav>
        </aside>

        {/* Main Content */}
        <main className="main-content">
          {React.cloneElement(children, { currentPage, setCurrentPage })}
        </main>
      </div>
    </div>
  );
};

export default Layout;
