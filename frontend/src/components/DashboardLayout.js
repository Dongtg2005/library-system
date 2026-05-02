import React, { useState } from 'react';
import { Outlet, useLocation, useNavigate } from 'react-router-dom';
import { BellIcon, ChevronDownIcon, UserCircleIcon } from '@heroicons/react/24/outline';
import Sidebar from './Sidebar';
import Dropdown from './Dropdown';
import Button from './Button';
import LanguageSelector from './LanguageSelector';
import Toast from './Toast';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { useTranslation } from '../context/LanguageContext';

const DashboardLayout = () => {
  const [collapsed, setCollapsed] = useState(false);
  const { user, logout } = useAuth();
  const toast = useToast();
  const { t } = useTranslation();
  const location = useLocation();
  const navigate = useNavigate();

  const titleMap = {
    '/dashboard': t('nav.dashboard'),
    '/profile': t('nav.profile'),
    '/users': t('sidebar.userManagement'),
    '/admin/books': t('sidebar.bookService'),
    '/admin/borrow': t('sidebar.borrowService'),
    '/admin/reservations': t('sidebar.reservations'),
  };

  const menuItems = [
    { label: t('nav.profile'), onClick: () => toast?.addToast({ type: 'info', title: t('nav.profile'), message: t('profilePage.editNotAvailable') }), icon: <UserCircleIcon className="h-4 w-4" /> },
    { label: t('nav.favorites'), onClick: () => toast?.addToast({ type: 'success', title: t('nav.favorites'), message: t('favoritesPage.description') }), icon: <BellIcon className="h-4 w-4" /> },
    { label: t('auth.logout'), onClick: logout, icon: <ChevronDownIcon className="h-4 w-4" /> },
  ];

  return (
    <div className="min-h-screen bg-slate-50 text-slate-900 dark:bg-slate-950 dark:text-white">
      <Toast />
      <div className="flex min-h-screen flex-col lg:flex-row">
        <Sidebar collapsed={collapsed} onToggle={() => setCollapsed((prev) => !prev)} user={user} />

        <div className="flex min-w-0 flex-1 flex-col">
          <header className="sticky top-0 z-40 border-b border-slate-200 bg-white/85 px-4 py-4 backdrop-blur-xl dark:border-slate-800 dark:bg-slate-950/85 sm:px-6 lg:px-8">
            <div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
              <div>
                <p className="text-sm font-semibold uppercase tracking-[0.2em] text-slate-500 dark:text-slate-400">{titleMap[location.pathname] || t('nav.dashboard')}</p>
                <h2 className="mt-1 text-2xl font-black tracking-tight">{t('adminDashboard.welcomeBack', { name: user?.fullName || t('sidebar.adminUser') })}</h2>
              </div>
              <div className="flex flex-wrap items-center gap-3">
                <LanguageSelector className="w-36" />
                <Button variant="secondary" onClick={() => navigate('/dashboard')}>{t('common.overview')}</Button>
                <Dropdown
                  button={<Button><UserCircleIcon className="h-5 w-5" />{t('adminDashboard.controlPanel')} <ChevronDownIcon className="h-4 w-4" /></Button>}
                  items={menuItems}
                />
              </div>
            </div>
          </header>

          <main className="flex-1 px-4 py-6 sm:px-6 lg:px-8">
            <div className="mx-auto max-w-7xl">
              <Outlet />
            </div>
          </main>
        </div>
      </div>
    </div>
  );
};

export default DashboardLayout;
