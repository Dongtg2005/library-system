import React from 'react';
import { NavLink, Outlet, useNavigate, Link } from 'react-router-dom';
import {
  ArrowRightOnRectangleIcon,
  Bars3Icon,
  BookOpenIcon,
  ChevronDownIcon,
  HeartIcon,
  HomeIcon,
  Squares2X2Icon,
  UserCircleIcon,
} from '@heroicons/react/24/outline';
import Button from './Button';
import Dropdown from './Dropdown';
import LanguageSelector from './LanguageSelector';
import ThemeToggle from './ThemeToggle';
import Toast from './Toast';
import { useAuth } from '../context/AuthContext';
import { useTranslation } from '../context/LanguageContext';

const LibraryLayout = () => {
  const { isAuthenticated, user, logout } = useAuth();
  const { t } = useTranslation();
  const navigate = useNavigate();
  const role = (user?.role || 'GUEST').toUpperCase();

  const navItems = [
    { label: t('nav.home'), to: '/', icon: HomeIcon },
    { label: t('nav.books'), to: '/books', icon: BookOpenIcon },
    { label: t('nav.categories'), to: '/categories', icon: Squares2X2Icon },
  ];

  const linkClassName = ({ isActive }) =>
    `inline-flex items-center gap-2 rounded-full px-4 py-2 text-sm font-semibold transition ${
      isActive
        ? 'bg-slate-900 text-white dark:bg-white dark:text-slate-950'
        : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900 dark:text-slate-300 dark:hover:bg-slate-800 dark:hover:text-white'
    }`;

  const userMenuItems = [
    { label: t('nav.profile'), onClick: () => navigate('/profile'), icon: <UserCircleIcon className="h-4 w-4" /> },
    { label: t('nav.myBooks'), onClick: () => navigate('/my-books'), icon: <BookOpenIcon className="h-4 w-4" /> },
    { label: t('nav.favorites'), onClick: () => navigate('/favorites'), icon: <HeartIcon className="h-4 w-4" /> },
    { label: t('auth.logout'), onClick: logout, icon: <ArrowRightOnRectangleIcon className="h-4 w-4" /> },
  ];

  const renderRoleAction = () => {
    if (!isAuthenticated) {
      return (
        <div className="flex items-center gap-2">
          <Button variant="ghost" onClick={() => navigate('/login')}>
            {t('auth.login')}
          </Button>
          <Button onClick={() => navigate('/register')}>{t('auth.register')}</Button>
        </div>
      );
    }

    if (role === 'LIBRARIAN') {
      return <Button onClick={() => navigate('/dashboard')}>{t('nav.dashboard')}</Button>;
    }

    if (role === 'ADMIN') {
      return <Button onClick={() => navigate('/dashboard')}>{t('adminDashboard.controlPanel')}</Button>;
    }

    return (
      <Dropdown
        button={
          <button className="inline-flex items-center gap-2 rounded-full border border-slate-200 bg-white px-4 py-2 text-sm font-semibold text-slate-700 shadow-sm transition hover:border-primary hover:text-primary dark:border-slate-700 dark:bg-slate-900 dark:text-slate-100">
            <UserCircleIcon className="h-5 w-5" />
            <span className="max-w-28 truncate">{user?.fullName || t('auth.login')}</span>
            <ChevronDownIcon className="h-4 w-4" />
          </button>
        }
        items={userMenuItems}
      />
    );
  };

  return (
    <div className="min-h-screen bg-slate-100 text-slate-900 transition dark:bg-slate-950 dark:text-slate-100">
      <Toast />
      <div className="absolute inset-x-0 top-0 -z-10 overflow-hidden">
        <div className="mx-auto h-[32rem] max-w-7xl rounded-b-[3rem] bg-[radial-gradient(circle_at_top_left,_rgba(20,184,166,0.22),_transparent_34%),radial-gradient(circle_at_top_right,_rgba(249,115,22,0.2),_transparent_28%),linear-gradient(180deg,_rgba(255,255,255,0.85),_rgba(241,245,249,0.4))] dark:bg-[radial-gradient(circle_at_top_left,_rgba(6,182,212,0.18),_transparent_32%),radial-gradient(circle_at_top_right,_rgba(251,146,60,0.16),_transparent_30%),linear-gradient(180deg,_rgba(15,23,42,0.95),_rgba(2,6,23,0.75))]" />
      </div>

      <header className="sticky top-0 z-50 border-b border-white/60 bg-white/80 backdrop-blur-xl dark:border-slate-800 dark:bg-slate-950/80">
        <div className="mx-auto flex max-w-7xl flex-wrap items-center justify-between gap-4 px-4 py-4 sm:px-6 lg:px-8">
          <Link 
            to="/" 
            className="group relative z-[100] flex cursor-pointer items-center gap-3 rounded-2xl p-1 transition-all hover:bg-slate-50 active:scale-95 dark:hover:bg-slate-900"
          >
            <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-slate-900 text-white transition-all group-hover:bg-primary group-hover:shadow-lg group-hover:shadow-primary/30 dark:bg-white dark:text-slate-950 dark:group-hover:bg-primary dark:group-hover:text-white">
              <Bars3Icon className="h-6 w-6" />
            </div>
            <div className="select-none">
              <p className="text-xs font-semibold uppercase tracking-[0.3em] text-slate-500 transition-colors group-hover:text-primary dark:text-slate-400">Library System</p>
              <p className="text-lg font-black tracking-tight transition-colors group-hover:text-primary">Reader Portal</p>
            </div>
          </Link>

          <nav className="flex flex-wrap items-center gap-2">
            {navItems.map(({ label, to, icon: Icon }) => (
              <NavLink key={to} to={to} className={linkClassName}>
                <Icon className="h-4 w-4" />
                {label}
              </NavLink>
            ))}
          </nav>

          <div className="flex items-center gap-3">
            <LanguageSelector className="w-36" />
            <ThemeToggle />
            {renderRoleAction()}
          </div>
        </div>
      </header>

      <main className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
        <Outlet />
      </main>

      <footer className="border-t border-slate-200/80 bg-white/70 dark:border-slate-800 dark:bg-slate-950/70">
        <div className="mx-auto grid max-w-7xl gap-8 px-4 py-10 text-sm text-slate-600 dark:text-slate-300 sm:px-6 lg:grid-cols-3 lg:px-8">
          <div>
            <Link to="/" className="text-base font-bold text-slate-900 transition hover:text-primary dark:text-white">Library System</Link>
            <p className="mt-3 max-w-sm">{t('home.description')}</p>
          </div>
          <div>
            <p className="font-semibold text-slate-900 dark:text-white">{t('profilePage.email')}</p>
            <p className="mt-3">Email: support@library.local</p>
            <p>Phone: +84 28 1234 5678</p>
            <p>Address: Central Reading Hall</p>
          </div>
          <div>
            <p className="font-semibold text-slate-900 dark:text-white">{t('nav.dashboard')}</p>
            <p className="mt-3">{t('myBorrows.title')}</p>
            <div className="mt-3 flex items-center gap-3">
              <a href="https://facebook.com" className="hover:text-primary">Facebook</a>
              <a href="https://instagram.com" className="hover:text-primary">Instagram</a>
              <a href="https://linkedin.com" className="hover:text-primary">LinkedIn</a>
            </div>
          </div>
        </div>
      </footer>
    </div>
  );
};

export default LibraryLayout;
