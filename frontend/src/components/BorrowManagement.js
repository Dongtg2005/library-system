import React, { useEffect, useMemo, useState } from 'react';
import { ArrowPathIcon, ClockIcon, ExclamationTriangleIcon, CheckIcon, XMarkIcon } from '@heroicons/react/24/outline';
import Button from './Button';
import Input from './Input';
import { apiRequest } from '../lib/api';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { useTranslation } from '../context/LanguageContext';

const isUuid = (value) => typeof value === 'string' && /^[0-9a-fA-F-]{36}$/.test(value);

const BorrowManagement = () => {
  const { token } = useAuth();
  const toast = useToast();
  const { t } = useTranslation();
  const [rows, setRows] = useState([]);
  const [query, setQuery] = useState('');
  const [filter, setFilter] = useState('All');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const loadRows = async () => {
    setLoading(true);
    setError('');
    try {
      const payload = await apiRequest('/api/v1/borrows?size=200', { token });
      const items = payload?.content ?? (Array.isArray(payload) ? payload : []);
      const mapped = items.map((item) => ({
        id: item.id,
        user: item.memberName || item.userName || item.fullName || `Member #${item.memberId}`,
        book: item.bookId ? `Book #${String(item.bookId).slice(0, 8)}` : '-',
        borrowDate: item.borrowDate || '-',
        dueDate: item.dueDate || '-',
        status: String(item.borrowStatus || 'UNKNOWN'),
        fine: '$0.00',
        raw: item,
      }));
      setRows(mapped);
    } catch (apiError) {
      setError(apiError.message || t('borrowManagement.unableToLoad'));
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
      const normalized = row.status.toLowerCase();
      const matchesSearch = [row.user, row.book, row.status].some((value) =>
        String(value || '').toLowerCase().includes(query.toLowerCase())
      );
      const matchesFilter =
        filter === 'All' ||
        (filter === 'Pending' && normalized.includes('pending')) ||
        (filter === 'Borrowed' && (normalized === 'active')) ||
        (filter === 'Returned' && normalized.includes('return')) ||
        (filter === 'Overdue' && normalized.includes('overdue'));
      return matchesSearch && matchesFilter;
    });
  }, [query, filter, rows]);

  const handleApprove = async (id) => {
    try {
      await apiRequest(`/api/v1/borrows/${id}/approve`, { method: 'PUT', token });
      toast?.addToast({ type: 'success', title: t('borrowManagement.approved'), message: t('borrowManagement.approvedMessage') });
      loadRows();
    } catch (e) {
      toast?.addToast({ type: 'error', title: t('borrowManagement.error'), message: e.message });
    }
  };

  const handleReject = async (id) => {
    const reason = prompt(t('borrowManagement.rejectReason')) || '';
    try {
      await apiRequest(`/api/v1/borrows/${id}/reject?reason=${encodeURIComponent(reason)}`, { method: 'PUT', token });
      toast?.addToast({ type: 'success', title: t('borrowManagement.rejected'), message: t('borrowManagement.rejectedMessage') });
      loadRows();
    } catch (e) {
      toast?.addToast({ type: 'error', title: t('borrowManagement.error'), message: e.message });
    }
  };

  const handleReturn = async (id) => {
    try {
      await apiRequest('/api/v1/borrows/return', {
        method: 'POST',
        token,
        body: { borrowRecordId: id, conditionOnReturn: 'GOOD', returnNotes: 'Returned via admin panel' },
      });
      toast?.addToast({ type: 'success', title: t('borrowManagement.returned'), message: t('borrowManagement.returnedMessage') });
      loadRows();
    } catch (e) {
      toast?.addToast({ type: 'error', title: t('borrowManagement.error'), message: e.message });
    }
  };

  const handleExtend = async (id) => {
    try {
      await apiRequest(`/api/v1/borrows/${id}/extend`, { method: 'POST', token });
      toast?.addToast({ type: 'success', title: t('borrowManagement.extended'), message: t('borrowManagement.extendedMessage') });
      loadRows();
    } catch (e) {
      toast?.addToast({ type: 'error', title: t('borrowManagement.error'), message: e.message });
    }
  };

  const statusBadge = (status) => {
    const s = status.toLowerCase();
    if (s.includes('pending')) return 'bg-amber-500/10 text-amber-600';
    if (s === 'active') return 'bg-teal-500/10 text-teal-600';
    if (s.includes('return') || s === 'returned') return 'bg-emerald-500/10 text-emerald-600';
    if (s.includes('overdue')) return 'bg-rose-500/10 text-rose-600';
    if (s === 'cancelled') return 'bg-slate-500/10 text-slate-500';
    return 'bg-slate-200/60 text-slate-600';
  };

  const filterOptions = [
    { value: 'All', label: t('borrowManagement.all') },
    { value: 'Pending', label: t('borrowManagement.pending') },
    { value: 'Borrowed', label: t('borrowManagement.borrowed') },
    { value: 'Returned', label: t('borrowManagement.returned') },
    { value: 'Overdue', label: t('borrowManagement.overdue') },
  ];

  const summary = [
    { label: t('borrowManagement.activeBorrows'), value: rows.filter((r) => r.status === 'ACTIVE').length, icon: <ArrowPathIcon className="h-5 w-5" /> },
    { label: t('borrowManagement.overdueItems'), value: rows.filter((r) => r.status === 'OVERDUE').length, icon: <ExclamationTriangleIcon className="h-5 w-5" /> },
    { label: t('borrowManagement.returnedItems'), value: rows.filter((r) => r.status === 'RETURNED').length, icon: <ClockIcon className="h-5 w-5" /> },
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
        <Input label={t('borrowManagement.searchRecords')} value={query} onChange={(e) => setQuery(e.target.value)} placeholder={t('borrowManagement.searchPlaceholder')} />
        <div className="lg:mt-7 flex gap-2 flex-wrap">
          {filterOptions.map((item) => (
            <Button key={item.value} variant={filter === item.value ? 'primary' : 'secondary'} size="sm" onClick={() => setFilter(item.value)}>{item.label}</Button>
          ))}
        </div>
      </div>

      {error && <p className="text-sm text-rose-600">{error}</p>}
      {loading && <p className="text-sm text-slate-500">{t('borrowManagement.loading')}</p>}

      <div className="grid gap-4">
        {filtered.length === 0 && !loading && (
          <div className="rounded-[28px] border border-slate-200 bg-white p-8 text-center text-slate-400 dark:border-slate-800 dark:bg-slate-950">
            {t('borrowManagement.noRecords')}
          </div>
        )}
        {filtered.map((row) => (
          <div key={row.id} className="rounded-[28px] border border-slate-200 bg-white p-5 shadow-lg transition hover:-translate-y-1 dark:border-slate-800 dark:bg-slate-950">
            <div className="grid gap-4 md:grid-cols-[1.3fr_1fr_1fr_auto] md:items-center">
              <div>
                <p className="text-xs font-semibold uppercase tracking-[0.2em] text-slate-400">{t('borrowManagement.borrower')}</p>
                <h4 className="mt-2 text-lg font-bold text-slate-900 dark:text-white">{row.user}</h4>
                <p className="text-sm text-slate-500 dark:text-slate-400">{row.book}</p>
              </div>
              <div>
                <p className="text-xs font-semibold uppercase tracking-[0.2em] text-slate-400">{t('borrowManagement.borrowDate')}</p>
                <p className="mt-2 font-semibold text-slate-700 dark:text-slate-200">{row.borrowDate}</p>
              </div>
              <div>
                <p className="text-xs font-semibold uppercase tracking-[0.2em] text-slate-400">{t('borrowManagement.dueDate')}</p>
                <p className="mt-2 font-semibold text-slate-700 dark:text-slate-200">{row.dueDate}</p>
              </div>
              <div className="flex flex-wrap gap-2 items-center">
                <span className={`rounded-full px-3 py-1 text-xs font-bold ${statusBadge(row.status)}`}>
                  {row.status.replace(/_/g, ' ')}
                </span>
                {row.status === 'PENDING_APPROVAL' && (
                  <>
                    <Button variant="primary" size="sm" onClick={() => handleApprove(row.id)}>
                      <CheckIcon className="h-4 w-4 mr-1" />{t('borrowManagement.approve')}
                    </Button>
                    <Button variant="secondary" size="sm" onClick={() => handleReject(row.id)} className="text-rose-600">
                      <XMarkIcon className="h-4 w-4 mr-1" />{t('borrowManagement.reject')}
                    </Button>
                  </>
                )}
                {row.status === 'ACTIVE' && (
                  <>
                    <Button variant="secondary" size="sm" onClick={() => handleReturn(row.id)}>{t('borrowManagement.return')}</Button>
                    <Button variant="ghost" size="sm" onClick={() => handleExtend(row.id)}>{t('borrowManagement.extend')}</Button>
                  </>
                )}
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default BorrowManagement;
