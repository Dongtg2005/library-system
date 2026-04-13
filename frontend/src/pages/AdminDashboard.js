import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import DashboardCards from '../components/DashboardCards';
import { BookOpenIcon, UsersIcon, ArrowPathIcon, ShieldCheckIcon } from '@heroicons/react/24/outline';
import Button from '../components/Button';

const AdminDashboard = () => {
  const navigate = useNavigate();
  const { user, token } = useAuth();
  const [backendStatus, setBackendStatus] = useState('Checking...');
  const [stats, setStats] = useState({
    totalBooks: 0,
    totalUsers: 0,
    activeBorrows: 0,
    pendingApprovals: 0,
  });

  useEffect(() => {
    const checkBackend = async () => {
      try {
        const response = await fetch('/api/v1/auth/me', {
          headers: {
            'Content-Type': 'application/json',
            ...(token && { Authorization: `Bearer ${token}` }),
          },
        });

        if (response.ok) {
          setBackendStatus('Connected - API is working');
        } else {
          setBackendStatus(`Error: HTTP ${response.status}`);
        }
      } catch (error) {
        setBackendStatus(`Error: ${error.message}`);
      }
    };

    if (token) {
      checkBackend();
    }
  }, [token]);

  const quickActions = [
    {
      title: 'Book Management',
      description: 'Add, edit, delete books and manage inventory',
      icon: BookOpenIcon,
      path: '/admin/books',
      color: 'from-blue-500 to-indigo-500',
    },
    {
      title: 'User Management',
      description: 'Manage user accounts, roles and permissions',
      icon: UsersIcon,
      path: '/users',
      color: 'from-emerald-500 to-teal-500',
    },
    {
      title: 'Borrow Workflow',
      description: 'Track borrow requests, returns and overdue',
      icon: ArrowPathIcon,
      path: '/admin/borrow',
      color: 'from-amber-500 to-orange-500',
    },
    {
      title: 'System Settings',
      description: 'Configure policies, fines and notifications',
      icon: ShieldCheckIcon,
      path: '/profile',
      color: 'from-rose-500 to-pink-500',
    },
  ];

  return (
    <div className="space-y-8">
      {/* Header */}
      <div>
        <p className="text-sm font-semibold uppercase tracking-[0.2em] text-slate-500 dark:text-slate-400">
          Admin Control Panel
        </p>
        <h2 className="mt-2 text-3xl font-black text-slate-900 dark:text-white">
          Welcome back, {user?.fullName || user?.email}!
        </h2>
        <p className="mt-2 text-slate-600 dark:text-slate-400">
          Your role: <span className="font-semibold text-primary">{user?.role}</span> | Manage your library operations efficiently
        </p>
      </div>

      {/* Charts & Stats */}
      <DashboardCards />

      {/* Quick Actions Grid */}
      <div className="grid gap-5 sm:grid-cols-2 xl:grid-cols-4">
        {quickActions.map((action) => (
          <div
            key={action.title}
            className="group relative overflow-hidden rounded-[28px] border border-white/50 bg-white/80 p-6 shadow-lg shadow-slate-200/30 backdrop-blur-xl transition-all duration-300 hover:-translate-y-1 hover:shadow-xl dark:border-white/10 dark:bg-slate-900/70 dark:shadow-slate-950/20"
          >
            <div className={`absolute inset-0 bg-gradient-to-br ${action.color} opacity-5 transition-opacity group-hover:opacity-10`} />
            <div className="relative">
              <div className={`inline-flex rounded-2xl bg-gradient-to-br ${action.color} p-3 text-white shadow-lg`}>
                <action.icon className="h-6 w-6" />
              </div>
              <h3 className="mt-4 text-lg font-bold text-slate-900 dark:text-white">{action.title}</h3>
              <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">{action.description}</p>
              <Button 
                className="mt-4 w-full" 
                variant="secondary"
                onClick={() => navigate(action.path)}
              >
                Open
              </Button>
            </div>
          </div>
        ))}
      </div>

      {/* System Status */}
      <div className="rounded-[28px] border border-white/50 bg-white/80 p-6 shadow-lg backdrop-blur-xl dark:border-white/10 dark:bg-slate-900/70">
        <div className="flex items-center justify-between">
          <div>
            <h3 className="text-lg font-bold text-slate-900 dark:text-white">System Status</h3>
            <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">
              Backend: {backendStatus}
            </p>
          </div>
          <div className={`flex items-center gap-2 rounded-full px-4 py-2 ${backendStatus.includes('Connected') ? 'bg-emerald-500/10 text-emerald-600' : 'bg-rose-500/10 text-rose-600'}`}>
            <span className="relative flex h-3 w-3">
              <span className={`absolute inline-flex h-full w-full animate-ping rounded-full ${backendStatus.includes('Connected') ? 'bg-emerald-400' : 'bg-rose-400'} opacity-75`}></span>
              <span className={`relative inline-flex h-3 w-3 rounded-full ${backendStatus.includes('Connected') ? 'bg-emerald-500' : 'bg-rose-500'}`}></span>
            </span>
            <span className="text-sm font-semibold">{backendStatus.includes('Connected') ? 'Online' : 'Offline'}</span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdminDashboard;
