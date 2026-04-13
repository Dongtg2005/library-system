import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import AdminDashboard from '../pages/AdminDashboard';
import LibrarianDashboard from '../pages/LibrarianDashboard';

const RoleBasedDashboard = () => {
  const { user } = useAuth();
  const role = (user?.role || '').toUpperCase();

  if (role === 'ADMIN') {
    return <AdminDashboard />;
  }
  
  if (role === 'LIBRARIAN') {
    return <LibrarianDashboard />;
  }

  // USER or unknown role - redirect to home
  return <Navigate to="/" replace />;
};

export default RoleBasedDashboard;
