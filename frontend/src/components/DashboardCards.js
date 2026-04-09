import React from 'react';
import { AreaChart, Area, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis, BarChart, Bar } from 'recharts';
import { chartData, dashboardStats } from '../data/mockData';

const toneMap = {
  primary: 'from-primary/90 to-indigo-500',
  amber: 'from-amber-500 to-yellow-400',
  cyan: 'from-cyan-500 to-sky-500',
  rose: 'from-rose-500 to-fuchsia-500',
};

const DashboardCards = () => {
  return (
    <div className="space-y-8">
      <div className="grid gap-5 sm:grid-cols-2 xl:grid-cols-4">
        {dashboardStats.map((item, index) => (
          <div
            key={item.label}
            className="group relative overflow-hidden rounded-[28px] border border-white/50 bg-white/80 p-5 shadow-xl shadow-slate-200/40 backdrop-blur-xl transition-all duration-300 hover:-translate-y-1 hover:shadow-2xl dark:border-white/10 dark:bg-slate-900/70 dark:shadow-slate-950/30"
            style={{ animationDelay: `${index * 90}ms` }}
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
              <h3 className="text-lg font-bold text-slate-900 dark:text-white">Activity Overview</h3>
              <p className="text-sm text-slate-500 dark:text-slate-400">Weekly trend across users, books and borrows</p>
            </div>
            <span className="rounded-full bg-primary/10 px-3 py-1 text-xs font-semibold text-primary">Live analytics</span>
          </div>
          <div className="h-80">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={chartData}>
                <defs>
                  <linearGradient id="usersGradient" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#4f46e5" stopOpacity={0.35} />
                    <stop offset="95%" stopColor="#4f46e5" stopOpacity={0} />
                  </linearGradient>
                  <linearGradient id="borrowsGradient" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#06b6d4" stopOpacity={0.35} />
                    <stop offset="95%" stopColor="#06b6d4" stopOpacity={0} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
                <XAxis dataKey="name" stroke="#94a3b8" />
                <YAxis stroke="#94a3b8" />
                <Tooltip />
                <Area type="monotone" dataKey="users" stroke="#4f46e5" fill="url(#usersGradient)" strokeWidth={3} />
                <Area type="monotone" dataKey="borrows" stroke="#06b6d4" fill="url(#borrowsGradient)" strokeWidth={3} />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </div>

        <div className="rounded-[30px] border border-white/50 bg-white/80 p-6 shadow-xl shadow-slate-200/40 backdrop-blur-xl dark:border-white/10 dark:bg-slate-900/70 dark:shadow-slate-950/30">
          <div className="mb-5">
            <h3 className="text-lg font-bold text-slate-900 dark:text-white">Books vs Borrows</h3>
            <p className="text-sm text-slate-500 dark:text-slate-400">Operational snapshot</p>
          </div>
          <div className="h-72">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
                <XAxis dataKey="name" stroke="#94a3b8" />
                <YAxis stroke="#94a3b8" />
                <Tooltip />
                <Bar dataKey="books" fill="#facc15" radius={[10, 10, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>
    </div>
  );
};

export default DashboardCards;
