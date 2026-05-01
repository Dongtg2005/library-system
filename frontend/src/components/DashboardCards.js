import React, { useEffect, useState } from 'react';
import { AreaChart, Area, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis, BarChart, Bar } from 'recharts';
import { fetchDashboardStats } from '../lib/api';
import { useAuth } from '../context/AuthContext';

const toneMap = {
  primary: 'from-primary/90 to-cyan-500',
  amber: 'from-amber-500 to-yellow-400',
  cyan: 'from-cyan-500 to-teal-500',
  rose: 'from-emerald-500 to-teal-500',
};

const DashboardCards = () => {
  const { token } = useAuth();
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const loadStats = async () => {
      try {
        setLoading(true);
        const data = await fetchDashboardStats(token);
        setStats(data);
      } catch (err) {
        console.error('Failed to fetch dashboard stats:', err);
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    if (token) loadStats();
  }, [token]);

  if (loading) {
    return (
      <div className="flex h-96 items-center justify-center rounded-[32px] border border-dashed border-slate-300 dark:border-slate-700">
        <div className="flex flex-col items-center gap-3">
          <div className="h-10 w-10 animate-spin rounded-full border-4 border-primary border-t-transparent" />
          <p className="text-sm font-medium text-slate-500">Synchronizing database analytics...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="rounded-[32px] bg-rose-50 p-6 text-rose-700 dark:bg-rose-500/10 dark:text-rose-200">
        <p className="font-bold">Analytics Engine Error</p>
        <p className="mt-1 text-sm">{error}</p>
      </div>
    );
  }

  const statItems = [
    { label: 'Total Users', value: stats.totalUsers.toLocaleString(), delta: 'Real-time', tone: 'primary' },
    { label: 'Total Books', value: stats.totalBooks.toLocaleString(), delta: 'Inventory', tone: 'amber' },
    { label: 'Borrowed Books', value: stats.totalBorrowed.toLocaleString(), delta: 'Active', tone: 'cyan' },
    { label: 'Overdue Books', value: stats.totalOverdue.toLocaleString(), delta: 'Attention', tone: 'rose' },
  ];

  return (
    <div className="space-y-8 animate-in fade-in slide-in-from-bottom-4 duration-700">
      <div className="grid gap-5 sm:grid-cols-2 xl:grid-cols-4">
        {statItems.map((item, index) => (
          <div
            key={item.label}
            className="group relative overflow-hidden rounded-[28px] border border-white/50 bg-white/80 p-5 shadow-xl shadow-slate-200/40 backdrop-blur-xl transition-all duration-300 hover:-translate-y-1 hover:shadow-2xl dark:border-white/10 dark:bg-slate-900/70 dark:shadow-slate-950/30"
            style={{ animationDelay: `${index * 100}ms` }}
          >
            <div className={`absolute inset-0 bg-gradient-to-br ${toneMap[item.tone]} opacity-10 transition-opacity group-hover:opacity-20`} />
            <div className="relative">
              <p className="text-sm font-medium text-slate-500 dark:text-slate-400">{item.label}</p>
              <div className="mt-4 flex items-end justify-between gap-4">
                <h3 className="text-3xl font-black tracking-tight text-slate-900 dark:text-white">{item.value}</h3>
                <span className="rounded-full bg-slate-100 px-3 py-1 text-xs font-semibold text-slate-600 dark:bg-slate-800 dark:text-slate-300">{item.delta}</span>
              </div>
            </div>
          </div>
        ))}
      </div>

      <div className="grid gap-5 xl:grid-cols-3">
        <div className="xl:col-span-2 rounded-[30px] border border-white/50 bg-white/80 p-6 shadow-xl shadow-slate-200/40 backdrop-blur-xl dark:border-white/10 dark:bg-slate-900/70 dark:shadow-slate-950/30">
          <div className="mb-5 flex items-center justify-between">
            <div>
              <h3 className="text-lg font-bold text-slate-900 dark:text-white">Activity Timeline</h3>
              <p className="text-sm text-slate-500 dark:text-slate-400">7-day performance metrics from live records</p>
            </div>
            <span className="rounded-full bg-primary/10 px-3 py-1 text-xs font-semibold text-primary">Live Data</span>
          </div>
          <div className="h-80">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={stats.weeklyActivities}>
                <defs>
                  <linearGradient id="usersGradient" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#0f766e" stopOpacity={0.35} />
                    <stop offset="95%" stopColor="#0f766e" stopOpacity={0} />
                  </linearGradient>
                  <linearGradient id="borrowsGradient" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#14b8a6" stopOpacity={0.35} />
                    <stop offset="95%" stopColor="#14b8a6" stopOpacity={0} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" vertical={false} />
                <XAxis dataKey="name" stroke="#94a3b8" fontSize={12} tickLine={false} axisLine={false} />
                <YAxis stroke="#94a3b8" fontSize={12} tickLine={false} axisLine={false} />
                <Tooltip 
                  contentStyle={{ borderRadius: '16px', border: 'none', boxShadow: '0 10px 15px -3px rgb(0 0 0 / 0.1)' }}
                />
                <Area type="monotone" dataKey="users" stroke="#0f766e" fill="url(#usersGradient)" strokeWidth={3} />
                <Area type="monotone" dataKey="borrows" stroke="#14b8a6" fill="url(#borrowsGradient)" strokeWidth={3} />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </div>

        <div className="rounded-[30px] border border-white/50 bg-white/80 p-6 shadow-xl shadow-slate-200/40 backdrop-blur-xl dark:border-white/10 dark:bg-slate-900/70 dark:shadow-slate-950/30">
          <div className="mb-5">
            <h3 className="text-lg font-bold text-slate-900 dark:text-white">Collection Growth</h3>
            <p className="text-sm text-slate-500 dark:text-slate-400">Database entry trend (Weekly)</p>
          </div>
          <div className="h-72">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={stats.weeklyActivities}>
                <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" vertical={false} />
                <XAxis dataKey="name" stroke="#94a3b8" fontSize={12} tickLine={false} axisLine={false} />
                <YAxis stroke="#94a3b8" fontSize={12} tickLine={false} axisLine={false} />
                <Tooltip 
                   contentStyle={{ borderRadius: '16px', border: 'none', boxShadow: '0 10px 15px -3px rgb(0 0 0 / 0.1)' }}
                />
                <Bar dataKey="books" fill="#14b8a6" radius={[10, 10, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>
    </div>
  );
};

export default DashboardCards;

