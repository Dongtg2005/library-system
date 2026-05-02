import React from 'react';
import { NavLink } from 'react-router-dom';
import {
  Bars3Icon,
  Squares2X2Icon,
  BookOpenIcon,
  UsersIcon,
  ArrowPathIcon,
  ShieldCheckIcon,
  SunIcon,
  MoonIcon,
  BellIcon,
} from '@heroicons/react/24/outline';
import { useTheme } from '../context/ThemeContext';
import { useTranslation } from '../context/LanguageContext';

const Sidebar = ({ collapsed, onToggle, user }) => {
  const { darkMode, toggleTheme } = useTheme();
  const { t } = useTranslation();
  const role = (user?.role || '').toUpperCase();
  
  const adminNavItems = [
    { to: '/dashboard', label: t('sidebar.dashboard'), icon: <Squares2X2Icon className="h-5 w-5" /> },
    { to: '/users', label: t('sidebar.userManagement'), icon: <UsersIcon className="h-5 w-5" /> },
    { to: '/admin/books', label: t('sidebar.bookService'), icon: <BookOpenIcon className="h-5 w-5" /> },
    { to: '/admin/borrow', label: t('sidebar.borrowService'), icon: <ArrowPathIcon className="h-5 w-5" /> },
    { to: '/admin/reservations', label: t('sidebar.reservations'), icon: <BellIcon className="h-5 w-5" /> },
    { to: '/profile', label: t('sidebar.settings'), icon: <ShieldCheckIcon className="h-5 w-5" /> },
  ];

  const librarianNavItems = [
    { to: '/dashboard', label: t('sidebar.workDashboard'), icon: <Squares2X2Icon className="h-5 w-5" /> },
    { to: '/admin/borrow', label: t('sidebar.borrowRequests'), icon: <ArrowPathIcon className="h-5 w-5" /> },
    { to: '/admin/books', label: t('sidebar.bookInventory'), icon: <BookOpenIcon className="h-5 w-5" /> },
    { to: '/admin/reservations', label: t('sidebar.reservations'), icon: <BellIcon className="h-5 w-5" /> },
    { to: '/profile', label: t('sidebar.profile'), icon: <ShieldCheckIcon className="h-5 w-5" /> },
  ];

  const navItems = role === 'LIBRARIAN' ? librarianNavItems : adminNavItems;
  const title = role === 'LIBRARIAN' ? t('sidebar.librarianSuite') : t('sidebar.adminSuite');

  return (
    <aside className={`flex w-full flex-col border-b border-slate-200 bg-white/95 backdrop-blur-xl transition-all duration-300 dark:border-slate-800 dark:bg-slate-950/95 lg:sticky lg:top-0 lg:h-screen lg:border-b-0 lg:border-r ${collapsed ? 'lg:w-24' : 'lg:w-72'}`}>
      <div className="flex items-center justify-between gap-3 border-b border-slate-200 px-5 py-5 dark:border-slate-800">
        <div className="flex items-center gap-3 overflow-hidden">
          <div className="flex h-11 w-11 items-center justify-center rounded-2xl bg-dashboard-gradient text-white shadow-lg shadow-primary/25">
            <BookOpenIcon className="h-6 w-6" />
          </div>
          {!collapsed && (
            <div>
              <p className="text-sm font-medium text-slate-500 dark:text-slate-400">{t('sidebar.library')}</p>
              <h1 className="text-lg font-black tracking-tight text-slate-900 dark:text-white">{title}</h1>
            </div>
          )}
        </div>
        <button
          onClick={onToggle}
          className="rounded-2xl p-2.5 text-slate-500 transition hover:bg-slate-100 hover:text-slate-900 dark:hover:bg-slate-800 dark:hover:text-white"
        >
          <Bars3Icon className="h-5 w-5" />
        </button>
      </div>

      <div className="flex-1 space-y-6 overflow-y-auto px-3 py-4 lg:overflow-y-auto">
        <nav className="grid gap-2 sm:grid-cols-2 lg:grid-cols-1 lg:space-y-1 lg:gap-0">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) =>
                `flex items-center gap-3 rounded-2xl px-4 py-3 text-sm font-semibold transition-all duration-200 ${isActive ? 'bg-primary text-white shadow-lg shadow-primary/25' : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900 dark:text-slate-300 dark:hover:bg-slate-800 dark:hover:text-white'}`
              }
            >
              {item.icon}
              {!collapsed && <span>{item.label}</span>}
            </NavLink>
          ))}
        </nav>

        {!collapsed && (
          <div className="rounded-3xl bg-card-gradient p-4 shadow-glow ring-1 ring-white/40 dark:ring-white/5">
            <p className="text-xs font-semibold uppercase tracking-[0.2em] text-slate-500 dark:text-slate-400">{t('sidebar.signedInAs')}</p>
            <p className="mt-2 truncate text-sm font-bold text-slate-900 dark:text-white">{user?.fullName || user?.email || t('sidebar.adminUser')}</p>
            <p className="mt-1 text-xs text-slate-500 dark:text-slate-400">{user?.role || 'ADMIN'}</p>
          </div>
        )}
      </div>

      <div className="border-t border-slate-200 p-4 dark:border-slate-800">
        <button
          onClick={toggleTheme}
          className="flex w-full items-center justify-center gap-3 rounded-2xl bg-slate-100 px-4 py-3 text-sm font-semibold text-slate-700 transition hover:bg-slate-200 dark:bg-slate-800 dark:text-slate-200 dark:hover:bg-slate-700"
        >
          {darkMode ? <SunIcon className="h-5 w-5" /> : <MoonIcon className="h-5 w-5" />}
          <span className="hidden sm:inline">{darkMode ? t('sidebar.lightMode') : t('sidebar.darkMode')}</span>
        </button>
      </div>
    </aside>
  );
};

export default Sidebar;
