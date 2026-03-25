import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './Navigation.css';

const Navigation = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [showMenu, setShowMenu] = useState(false);

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  return (
    <nav className="navbar">
      <div className="navbar-container">
        {/* Logo */}
        <Link to="/dashboard" className="navbar-brand">
          <span className="brand-icon">📚</span>
          <span className="brand-text">Thư Viện</span>
        </Link>

        {/* Hamburger Menu */}
        <button
          className="hamburger"
          onClick={() => setShowMenu(!showMenu)}
        >
          <span></span>
          <span></span>
          <span></span>
        </button>

        {/* Menu */}
        <div className={`navbar-menu ${showMenu ? 'active' : ''}`}>
          <Link to="/dashboard" className="nav-link">
            📊 Dashboard
          </Link>
          <Link to="/books" className="nav-link">
            📚 Sách
          </Link>
          
          <div className="user-dropdown">
            <button className="user-button">
              👤 {user?.fullName}
            </button>
            <div className="dropdown-menu">
              <Link to="/account" className="dropdown-item">
                ⚙️ Cài Đặt
              </Link>
              <button
                className="dropdown-item logout-btn"
                onClick={handleLogout}
              >
                📤 Đăng Xuất
              </button>
            </div>
          </div>
        </div>
      </div>
    </nav>
  );
};

export default Navigation;
