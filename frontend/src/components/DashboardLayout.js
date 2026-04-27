import React, { useState } from 'react';
import { Outlet, useLocation, useNavigate } from 'react-router-dom';
import { BellIcon, ChevronDownIcon, UserCircleIcon } from '@heroicons/react/24/outline';
import Sidebar from './Sidebar';
import Dropdown from './Dropdown';
import Button from './Button';
import Toast from './Toast';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';

const titleMap = {
  '/dashboard': 'Dashboard',
  '/profile': 'Profile',
  '/users': 'User Service',
  '/admin/books': 'Book Service',
  '/admin/borrow': 'Borrow Service',
  '/admin/reservations': 'Reservations',
};

const DashboardLayout = () => {
  const [collapsed, setCollapsed] = useState(false);
  const { user, logout } = useAuth();
  const toast = useToast();
  const location = useLocation();
  const navigate = useNavigate();

  const menuItems = [
    { label: 'Profile', onClick: () => toast?.addToast({ type: 'info', title: 'Profile', message: 'Profile management coming soon.' }), icon: <UserCircleIcon className="h-4 w-4" /> },
    { label: 'Notifications', onClick: () => toast?.addToast({ type: 'success', title: 'Notifications', message: 'All notifications are synced.' }), icon: <BellIcon className="h-4 w-4" /> },
    { label: 'Logout', onClick: logout, icon: <ChevronDownIcon className="h-4 w-4" /> },
  ];

  return (
    <div className="min-h-screen bg-slate-50 text-slate-900 dark:bg-slate-950 dark:text-white">
      <Toast />
      <div className="flex min-h-screen">
        <Sidebar collapsed={collapsed} onToggle={() => setCollapsed((prev) => !prev)} user={user} />

        <div className="flex min-w-0 flex-1 flex-col">
          <header className="sticky top-0 z-40 border-b border-slate-200 bg-white/85 px-4 py-4 backdrop-blur-xl dark:border-slate-800 dark:bg-slate-950/85 sm:px-6 lg:px-8">
            <div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
              <div>
                <p className="text-sm font-semibold uppercase tracking-[0.2em] text-slate-500 dark:text-slate-400">{titleMap[location.pathname] || 'Dashboard'}</p>
                <h2 className="mt-1 text-2xl font-black tracking-tight">Welcome back, {user?.fullName || 'Admin'}.</h2>
              </div>
              <div className="flex flex-wrap items-center gap-3">
                <Button variant="secondary" onClick={() => navigate('/dashboard')}>Overview</Button>
                <Dropdown
                  button={<Button><UserCircleIcon className="h-5 w-5" />Account <ChevronDownIcon className="h-4 w-4" /></Button>}
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

