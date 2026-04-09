import React from 'react';
import UserTable from '../components/UserTable';

const UserPage = () => {
  return (
    <div className="space-y-6">
      <div>
        <p className="text-sm font-semibold uppercase tracking-[0.2em] text-slate-500 dark:text-slate-400">User Service</p>
        <h2 className="mt-2 text-3xl font-black text-slate-900 dark:text-white">User management & access control</h2>
      </div>
      <UserTable />
    </div>
  );
};

export default UserPage;
