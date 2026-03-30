import React, { useMemo, useState } from 'react';
import { Link, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './LayoutShell.css';

const menuItems = [
  { path: '/dashboard', label: 'Dashboard', icon: '⌁' },
  { path: '/books', label: 'Books', icon: '▦' },
  { path: '/borrow-return', label: 'Borrow/Return', icon: '↻' },
  { path: '/users', label: 'Users', icon: '◉', adminOnly: true },
  { path: '/notifications', label: 'Notifications', icon: '✦' },
  { path: '/account', label: 'Account', icon: '⚙' },
];

const pageTitles = {
  '/dashboard': 'System Dashboard',
  '/books': 'Book Catalog',
  '/borrow-return': 'Borrow & Return Center',
  '/users': 'User Management',
  '/notifications': 'Notifications',
  '/account': 'Account Settings',
};

const LayoutShell = () => {
  const { user, logout } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const [sidebarOpen, setSidebarOpen] = useState(false);

  const isAdmin = user?.role === 'ADMIN';

  const visibleItems = useMemo(() => {
    return menuItems.filter((item) => !item.adminOnly || isAdmin);
  }, [isAdmin]);

  const title = pageTitles[location.pathname] || 'Library Management System';

  const handleLogout = async () => {
    await logout();
    navigate('/login', { replace: true });
  };

  return (
    <div className="layout-shell">
      <aside className={`lms-sidebar ${sidebarOpen ? 'open' : ''}`}>
        <div className="brand">
          <div className="brand-mark">L</div>
          <div>
            <h1>LibraFlow</h1>
            <p>Microservices LMS</p>
          </div>
        </div>

        <nav className="sidebar-nav">
          {visibleItems.map((item) => {
            const active = location.pathname === item.path;
            return (
              <Link
                key={item.path}
                to={item.path}
                className={`sidebar-link ${active ? 'active' : ''}`}
                onClick={() => setSidebarOpen(false)}
              >
                <span className="icon">{item.icon}</span>
                <span>{item.label}</span>
              </Link>
            );
          })}
        </nav>
      </aside>

      <div className="layout-main">
        <header className="topbar">
          <button
            type="button"
            className="menu-toggle"
            onClick={() => setSidebarOpen(!sidebarOpen)}
            aria-label="Toggle navigation"
          >
            ☰
          </button>

          <div className="topbar-title">
            <h2>{title}</h2>
            <span>Welcome back, {user?.fullName || 'User'}</span>
          </div>

          <div className="topbar-actions">
            <div className="user-chip">
              <span>{user?.role || 'USER'}</span>
              <strong>{user?.email || 'N/A'}</strong>
            </div>
            <button type="button" className="logout-btn" onClick={handleLogout}>
              Logout
            </button>
          </div>
        </header>

        <main className="content-area" onClick={() => setSidebarOpen(false)}>
          <Outlet />
        </main>
      </div>
    </div>
  );
};

export default LayoutShell;
