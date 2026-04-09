import React, { useMemo, useState } from 'react';
import { ArrowPathIcon, ClockIcon, ExclamationTriangleIcon } from '@heroicons/react/24/outline';
import Button from './Button';
import Input from './Input';
import { borrowRows } from '../data/mockData';

const BorrowManagement = () => {
  const [query, setQuery] = useState('');
  const [filter, setFilter] = useState('All');

  const filtered = useMemo(() => {
    return borrowRows.filter((row) => {
      const matchesSearch = [row.user, row.book, row.status].some((value) => value.toLowerCase().includes(query.toLowerCase()));
      const matchesFilter = filter === 'All' || row.status === filter;
      return matchesSearch && matchesFilter;
    });
  }, [query, filter]);

  const summary = [
    { label: 'Active borrows', value: '1,284', icon: <ArrowPathIcon className="h-5 w-5" /> },
    { label: 'Overdue items', value: '38', icon: <ExclamationTriangleIcon className="h-5 w-5" /> },
    { label: 'Late fees', value: '$4,860', icon: <ClockIcon className="h-5 w-5" /> },
  ];

  return (
    <div className="space-y-6">
      <div className="grid gap-4 md:grid-cols-3">
        {summary.map((item) => (
          <div key={item.label} className="rounded-[28px] border border-slate-200 bg-white p-5 shadow-lg dark:border-slate-800 dark:bg-slate-950">
            <div className="flex items-center gap-3 text-primary">{item.icon}<span className="text-sm font-semibold text-slate-500 dark:text-slate-400">{item.label}</span></div>
            <p className="mt-4 text-3xl font-black text-slate-900 dark:text-white">{item.value}</p>
          </div>
        ))}
      </div>

      <div className="grid gap-4 lg:grid-cols-[1fr_auto]">
        <Input label="Search borrowing records" value={query} onChange={(e) => setQuery(e.target.value)} placeholder="Search user, book or status" />
        <div className="lg:mt-7 flex gap-2">
          {['All', 'Borrowed', 'Returned', 'Overdue'].map((item) => (
            <Button key={item} variant={filter === item ? 'primary' : 'secondary'} size="sm" onClick={() => setFilter(item)}>{item}</Button>
          ))}
        </div>
      </div>

      <div className="grid gap-4">
        {filtered.map((row) => (
          <div key={row.id} className="rounded-[28px] border border-slate-200 bg-white p-5 shadow-lg transition hover:-translate-y-1 dark:border-slate-800 dark:bg-slate-950">
            <div className="grid gap-4 md:grid-cols-[1.3fr_1fr_1fr_auto] md:items-center">
              <div>
                <p className="text-xs font-semibold uppercase tracking-[0.2em] text-slate-400">Borrower</p>
                <h4 className="mt-2 text-lg font-bold text-slate-900 dark:text-white">{row.user}</h4>
                <p className="text-sm text-slate-500 dark:text-slate-400">{row.book}</p>
              </div>
              <div>
                <p className="text-xs font-semibold uppercase tracking-[0.2em] text-slate-400">Borrow date</p>
                <p className="mt-2 font-semibold text-slate-700 dark:text-slate-200">{row.borrowDate}</p>
              </div>
              <div>
                <p className="text-xs font-semibold uppercase tracking-[0.2em] text-slate-400">Due date</p>
                <p className="mt-2 font-semibold text-slate-700 dark:text-slate-200">{row.dueDate}</p>
              </div>
              <div className="flex flex-wrap gap-2">
                <span className={`rounded-full px-3 py-1 text-xs font-bold ${row.status === 'Overdue' ? 'bg-rose-500/10 text-rose-600' : row.status === 'Returned' ? 'bg-emerald-500/10 text-emerald-600' : 'bg-amber-500/10 text-amber-600'}`}>{row.status}</span>
                <Button variant="secondary" size="sm">Return</Button>
                <Button variant="accent" size="sm">Borrow</Button>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default BorrowManagement;
