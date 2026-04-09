import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import './Layout.css';

const Layout = ({ children }) => {
  const { user, logout } = useAuth();
  const [sidebarOpen, setSidebarOpen] = useState(true);
  const [currentPage, setCurrentPage] = useState('home');

  const menuItems = [
    { id: 'home', label: '🏠 Dashboard', page: 'home' },
    { id: 'books', label: '📚 Browse Books', page: 'books' },
    { id: 'myborrow', label: '📖 My Borrows', page: 'myborrow' },
    ...(user?.role === 'ADMIN' || user?.role === 'LIBRARIAN' ? [
      { id: 'manage', label: '⚙️ Manage Books', page: 'manage' },
      { id: 'users', label: '👥 Users', page: 'users' },
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
            title="Toggle sidebar"
          >
            ☰
          </button>
          <h1 className="app-title">📚 Library System</h1>
        </div>
        <div className="header-right">
          <span className="user-welcome">Welcome, {user?.fullName || user?.email}</span>
          <button onClick={handleLogout} className="logout-btn">
            Logout
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
