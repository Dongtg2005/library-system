import React from 'react';
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { ThemeProvider } from './context/ThemeContext';
import { ToastProvider } from './context/ToastContext';
import DashboardLayout from './components/DashboardLayout';
import AuthPage from './pages/AuthPage';
import BookPage from './pages/BookPage';
import BorrowPage from './pages/BorrowPage';
import DashboardPage from './pages/DashboardPage';
import UserPage from './pages/UserPage';

const LoadingScreen = () => (
  <div className="flex min-h-screen items-center justify-center bg-slate-950 text-white">
    <div className="rounded-3xl border border-white/10 bg-white/5 px-6 py-4 text-sm font-semibold backdrop-blur">
      Loading Library Admin Suite...
    </div>
  </div>
);

const RequireAuth = ({ children }) => {
  const { isAuthenticated, loading } = useAuth();

  if (loading) return <LoadingScreen />;
  if (!isAuthenticated) return <Navigate to="/auth" replace />;

  return children;
};

const PublicOnly = ({ children }) => {
  const { isAuthenticated, loading } = useAuth();

  if (loading) return <LoadingScreen />;
  if (isAuthenticated) return <Navigate to="/dashboard" replace />;

  return children;
};

const HomeRedirect = () => {
  const { isAuthenticated, loading } = useAuth();

  if (loading) return <LoadingScreen />;
  return <Navigate to={isAuthenticated ? '/dashboard' : '/auth'} replace />;
};

function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<HomeRedirect />} />
      <Route
        path="/auth"
        element={
          <PublicOnly>
            <AuthPage />
          </PublicOnly>
        }
      />
      <Route
        element={
          <RequireAuth>
            <DashboardLayout />
          </RequireAuth>
        }
      >
        <Route path="dashboard" element={<DashboardPage />} />
        <Route path="users" element={<UserPage />} />
        <Route path="books" element={<BookPage />} />
        <Route path="borrow" element={<BorrowPage />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <ThemeProvider>
          <ToastProvider>
            <AppRoutes />
          </ToastProvider>
        </ThemeProvider>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
