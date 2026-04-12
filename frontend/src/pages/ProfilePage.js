import React from 'react';
import { useAuth } from '../context/AuthContext';

const ProfilePage = () => {
  const { user } = useAuth();

  return (
    <div className="rounded-[2rem] border border-white/70 bg-white/85 p-6 page-fade dark:border-slate-800 dark:bg-slate-900/75">
      <h1 className="text-3xl font-black text-slate-950 dark:text-white">Profile</h1>
      <p className="mt-2 text-sm text-slate-600 dark:text-slate-300">Current identity from authenticated token payload.</p>

      <div className="mt-6 grid gap-4 sm:grid-cols-2">
        <div className="rounded-2xl bg-slate-100 p-4 dark:bg-slate-800">
          <p className="text-sm text-slate-500 dark:text-slate-400">Full name</p>
          <p className="mt-1 font-semibold">{user?.fullName || '-'}</p>
        </div>
        <div className="rounded-2xl bg-slate-100 p-4 dark:bg-slate-800">
          <p className="text-sm text-slate-500 dark:text-slate-400">Email</p>
          <p className="mt-1 font-semibold">{user?.email || '-'}</p>
        </div>
        <div className="rounded-2xl bg-slate-100 p-4 dark:bg-slate-800">
          <p className="text-sm text-slate-500 dark:text-slate-400">Role</p>
          <p className="mt-1 font-semibold">{user?.role || '-'}</p>
        </div>
        <div className="rounded-2xl bg-slate-100 p-4 dark:bg-slate-800">
          <p className="text-sm text-slate-500 dark:text-slate-400">User ID</p>
          <p className="mt-1 font-semibold">{user?.userId || '-'}</p>
        </div>
      </div>

      <div className="mt-6 rounded-2xl border border-slate-200 px-4 py-3 text-sm text-slate-600 dark:border-slate-800 dark:text-slate-300">
        Edit profile endpoint is not available yet in the current backend API.
      </div>
    </div>
  );
};

export default ProfilePage;
