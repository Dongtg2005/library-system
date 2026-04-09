import React from 'react';
import BorrowManagement from '../components/BorrowManagement';

const BorrowPage = () => {
  return (
    <div className="space-y-6">
      <div>
        <p className="text-sm font-semibold uppercase tracking-[0.2em] text-slate-500 dark:text-slate-400">Borrow Service</p>
        <h2 className="mt-2 text-3xl font-black text-slate-900 dark:text-white">Borrow / return workflow, late fees & RabbitMQ events</h2>
      </div>
      <BorrowManagement />
    </div>
  );
};

export default BorrowPage;
