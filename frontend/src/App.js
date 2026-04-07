import React from 'react';
import { AuthProvider, useAuth } from './context/AuthContext';
import Login from './components/Login';
import Dashboard from './pages/Dashboard';
import './App.css';

function AppContent() {
  const { isAuthenticated, loading } = useAuth();

  if (loading) {
    return (
      <div className="App">
        <div className="loading-screen">
          <p>Loading...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="App">
      {isAuthenticated ? (
        <Dashboard />
      ) : (
        <Login onLoginSuccess={() => {}} />
      )}
    </div>
  );
}

function App() {
  return (
    <AuthProvider>
      <AppContent />
    </AuthProvider>
  );
}

export default App;
