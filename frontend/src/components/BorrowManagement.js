import React, { useEffect, useMemo, useState } from 'react';
import { ArrowPathIcon, ClockIcon, ExclamationTriangleIcon } from '@heroicons/react/24/outline';
import Button from './Button';
import Input from './Input';
import { borrowRows } from '../data/mockData';
import { createBorrow, extendBorrow, fetchBorrowHistory, returnBorrow } from '../lib/api';
import { useAuth } from '../context/AuthContext';

const isUuid = (value) => typeof value === 'string' && /^[0-9a-fA-F-]{36}$/.test(value);

const BorrowManagement = () => {
  const { token } = useAuth();
  const [rows, setRows] = useState([]);
  const [query, setQuery] = useState('');
  const [filter, setFilter] = useState('All');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const loadRows = async () => {
    setLoading(true);
    setError('');

    try {
      const payload = await fetchBorrowHistory(token);
      const mapped = Array.isArray(payload)
        ? payload.map((item) => ({
            id: item.id,
            user: `Member #${item.memberId}`,
            book: item.bookId ? `Book #${String(item.bookId).slice(0, 8)}` : '-',
            borrowDate: item.borrowDate || '-',
            dueDate: item.dueDate || '-',
            status: String(item.borrowStatus || 'UNKNOWN').replace(/_/g, ' '),
            fine: '$0.00',
            raw: item,
          }))
        : [];

      setRows(mapped);
    } catch (apiError) {
      setError(apiError.message || 'Unable to load borrow history from API. Showing fallback data.');
      setRows(borrowRows.map((row) => ({ ...row, raw: row })));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!token) return;
    loadRows();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token]);

  const filtered = useMemo(() => {
    return rows.filter((row) => {
      const normalized = String(row.status || '').toLowerCase();
      const matchesSearch = [row.user, row.book, row.status].some((value) => String(value || '').toLowerCase().includes(query.toLowerCase()));
      const matchesFilter =
        filter === 'All' ||
        (filter === 'Borrowed' && (normalized.includes('borrow') || normalized.includes('approved') || normalized.includes('active'))) ||
        (filter === 'Returned' && normalized.includes('return')) ||
        (filter === 'Overdue' && normalized.includes('overdue'));

      return matchesSearch && matchesFilter;
    });
  }, [query, filter, rows]);

  const handleReturn = async (id) => {
    if (!token || !isUuid(id)) return;

    try {
      await returnBorrow(token, id, 'Returned from admin panel');
      loadRows();
    } catch (returnError) {
      setError(returnError.message || 'Unable to return book.');
    }
  };

  const handleBorrow = async (row) => {
    if (!token) return;

    const bookId = row?.raw?.bookId || row?.raw?.book || row?.raw?.bookUuid;
    if (!isUuid(bookId)) return;

    try {
      await createBorrow(token, bookId, 'Created from admin panel');
      loadRows();
    } catch (borrowError) {
      setError(borrowError.message || 'Unable to create borrow request.');
    }
  };

  const handleExtend = async (id) => {
    if (!token || !isUuid(id)) return;

    try {
      await extendBorrow(token, id);
      loadRows();
    } catch (extendError) {
      setError(extendError.message || 'Unable to extend loan.');
    }
  };

  const summary = [
    { label: 'Active borrows', value: rows.filter((row) => String(row.status).toLowerCase().includes('borrow') || String(row.status).toLowerCase().includes('active')).length.toString(), icon: <ArrowPathIcon className="h-5 w-5" /> },
    { label: 'Overdue items', value: rows.filter((row) => String(row.status).toLowerCase().includes('overdue')).length.toString(), icon: <ExclamationTriangleIcon className="h-5 w-5" /> },
    { label: 'Returned items', value: rows.filter((row) => String(row.status).toLowerCase().includes('return')).length.toString(), icon: <ClockIcon className="h-5 w-5" /> },
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

      {error && <p className="text-sm text-amber-600">{error}</p>}
      {loading && <p className="text-sm text-slate-500">Loading borrow records...</p>}

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
                <span className={`rounded-full px-3 py-1 text-xs font-bold ${String(row.status).toLowerCase().includes('overdue') ? 'bg-rose-500/10 text-rose-600' : String(row.status).toLowerCase().includes('return') ? 'bg-emerald-500/10 text-emerald-600' : 'bg-amber-500/10 text-amber-600'}`}>{row.status}</span>
                <Button variant="secondary" size="sm" onClick={() => handleReturn(row.id)}>Return</Button>
                <Button variant="accent" size="sm" onClick={() => handleBorrow(row)}>Borrow</Button>
                <Button variant="ghost" size="sm" onClick={() => handleExtend(row.id)}>Extend</Button>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default BorrowManagement;
