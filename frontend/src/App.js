import React from 'react';
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import DashboardLayout from './components/DashboardLayout';
import LibraryLayout from './components/LibraryLayout';
import { AuthProvider, useAuth } from './context/AuthContext';
import { ThemeProvider } from './context/ThemeContext';
import { ToastProvider } from './context/ToastContext';
import AuthPage from './pages/AuthPage';
import AdminDashboard from './pages/AdminDashboard';
import BookDetailPage from './pages/BookDetailPage';
import BookPage from './pages/BookPage';
import BooksPage from './pages/BooksPage';
import BorrowBookPage from './pages/BorrowBookPage';
import BorrowPage from './pages/BorrowPage';
import CategoriesPage from './pages/CategoriesPage';
import FavoritesPage from './pages/FavoritesPage';
import HistoryPage from './pages/HistoryPage';
import HomePage from './pages/HomePage';
import MyBorrowsPage from './pages/MyBorrowsPage';
import ProfilePage from './pages/ProfilePage';
import ReviewsPage from './pages/ReviewsPage';
import UserPage from './pages/UserPage';

const LoadingScreen = () => (
  <div className="flex min-h-screen items-center justify-center bg-slate-100 text-slate-700 dark:bg-slate-950 dark:text-slate-100">
    <div className="rounded-3xl border border-slate-200 bg-white px-6 py-4 text-sm font-semibold shadow dark:border-slate-800 dark:bg-slate-900">
      Loading Library System...
    </div>
  </div>
);

const RequireAuth = ({ children }) => {
  const { isAuthenticated, loading } = useAuth();

  if (loading) return <LoadingScreen />;
  if (!isAuthenticated) return <Navigate to="/login" replace />;

  return children;
};

const PublicOnly = ({ children }) => {
  const { isAuthenticated, loading } = useAuth();

  if (loading) return <LoadingScreen />;
  if (isAuthenticated) return <Navigate to="/" replace />;

  return children;
};

function AppRoutes() {
  return (
    <Routes>
      <Route element={<LibraryLayout />}>
        <Route index element={<HomePage />} />
        <Route path="books" element={<BooksPage />} />
        <Route path="books/:id" element={<BookDetailPage />} />
        <Route path="categories" element={<CategoriesPage />} />
      </Route>

      <Route path="/auth" element={<Navigate to="/login" replace />} />
      <Route
        path="/login"
        element={
          <PublicOnly>
            <AuthPage initialMode="login" />
          </PublicOnly>
        }
      />
      <Route
        path="/register"
        element={
          <PublicOnly>
            <AuthPage initialMode="register" />
          </PublicOnly>
        }
      />

      <Route
        element={
          <RequireAuth>
            <LibraryLayout />
          </RequireAuth>
        }
      >
        <Route path="my-books" element={<MyBorrowsPage />} />
        <Route path="favorites" element={<FavoritesPage />} />
        <Route path="history" element={<HistoryPage />} />
        <Route path="profile" element={<ProfilePage />} />
        <Route path="reviews" element={<ReviewsPage />} />
        <Route path="borrow/:bookId" element={<BorrowBookPage />} />
      </Route>

      <Route
        element={
          <RequireAuth>
            <DashboardLayout />
          </RequireAuth>
        }
      >
        <Route path="dashboard" element={<AdminDashboard />} />
        <Route path="users" element={<UserPage />} />
        <Route path="admin/books" element={<BookPage />} />
        <Route path="admin/borrow" element={<BorrowPage />} />
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
