import React, { useCallback, useEffect, useState } from 'react';
import Button from '../components/Button';
import { useAuth } from '../context/AuthContext';
import { extendBorrow, fetchBookById, fetchBorrowHistory, returnBorrow } from '../lib/api';
import { useToast } from '../context/ToastContext';

const tabs = [
  { key: 'borrowed', label: 'Borrowed', statuses: ['ACTIVE', 'PENDING_APPROVAL'] },
  { key: 'returned', label: 'Returned', statuses: ['RETURNED'] },
  { key: 'overdue', label: 'Overdue', statuses: ['OVERDUE'] },
];

const MyBorrowsPage = () => {
  const { token } = useAuth();
  const { addToast } = useToast() || {};
  const [activeTab, setActiveTab] = useState('borrowed');
  const [records, setRecords] = useState([]);
  const [bookMap, setBookMap] = useState({});
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState('');

  const load = useCallback(async () => {
    if (!token) return;
    setLoading(true);

    try {
      const history = await fetchBorrowHistory(token);
      setRecords(history || []);

      const ids = [...new Set((history || []).map((item) => item.bookId).filter(Boolean))];
      const entries = await Promise.all(
        ids.map(async (id) => {
          try {
            const book = await fetchBookById(id);
            return [id, book];
          } catch {
            return [id, null];
          }
        }),
      );

      setBookMap(Object.fromEntries(entries));
    } catch (error) {
      addToast?.({ type: 'error', title: 'Load failed', message: error.message || 'Failed to load borrow history.' });
      setRecords([]);
      setBookMap({});
    } finally {
      setLoading(false);
    }
  }, [addToast, token]);

  useEffect(() => {
    load();
  }, [load]);

  const list = records.filter((item) => tabs.find((tab) => tab.key === activeTab)?.statuses.includes(item.borrowStatus));

  const onExtend = async (borrowId) => {
    if (!token) return;
    setSubmitting(borrowId);
    try {
      await extendBorrow(token, borrowId);
      addToast?.({ type: 'success', title: 'Extended', message: 'Borrow period has been extended.' });
      await load();
    } catch (error) {
      addToast?.({ type: 'error', title: 'Extend failed', message: error.message || 'Unable to extend.' });
    } finally {
      setSubmitting('');
    }
  };

  const onReturn = async (borrowId) => {
    if (!token) return;
    setSubmitting(borrowId);
    try {
      await returnBorrow(token, borrowId, 'Returned from user portal');
      addToast?.({ type: 'success', title: 'Returned', message: 'Book return request sent.' });
      await load();
    } catch (error) {
      addToast?.({ type: 'error', title: 'Return failed', message: error.message || 'Unable to return.' });
    } finally {
      setSubmitting('');
    }
  };

  return (
    <div className="rounded-[2rem] border border-white/70 bg-white/85 p-6 page-fade dark:border-slate-800 dark:bg-slate-900/75">
      <h1 className="text-3xl font-black text-slate-950 dark:text-white">My Books</h1>
      <p className="mt-2 text-sm text-slate-600 dark:text-slate-300">Data from `/api/v1/borrows/history`.</p>

      <div className="mt-6 flex flex-wrap gap-2">
        {tabs.map((tab) => {
          const count = records.filter((item) => tab.statuses.includes(item.borrowStatus)).length;
          return (
            <button
              key={tab.key}
              type="button"
              onClick={() => setActiveTab(tab.key)}
              className={`rounded-full px-4 py-2 text-sm font-semibold transition ${
                tab.key === activeTab
                  ? 'bg-slate-900 text-white dark:bg-white dark:text-slate-900'
                  : 'bg-slate-100 text-slate-700 dark:bg-slate-800 dark:text-slate-200'
              }`}
            >
              {tab.label} ({count})
            </button>
          );
        })}
      </div>

      {loading ? (
        <div className="mt-6 rounded-2xl bg-slate-100 px-4 py-8 text-sm dark:bg-slate-800">Loading records...</div>
      ) : (
        <div className="mt-6 overflow-hidden rounded-3xl border border-slate-200 dark:border-slate-800">
          <table className="w-full text-left text-sm">
            <thead className="bg-slate-100 dark:bg-slate-800">
              <tr>
                <th className="px-4 py-3">Book</th>
                <th className="px-4 py-3">Borrow Date</th>
                <th className="px-4 py-3">Due Date</th>
                <th className="px-4 py-3">Status</th>
                <th className="px-4 py-3">Action</th>
              </tr>
            </thead>
            <tbody>
              {list.map((item) => (
                <tr key={item.id} className="border-t border-slate-200 dark:border-slate-800">
                  <td className="px-4 py-3">{bookMap[item.bookId]?.title || item.bookId}</td>
                  <td className="px-4 py-3">{item.borrowDate || '-'}</td>
                  <td className="px-4 py-3">{item.dueDate || '-'}</td>
                  <td className="px-4 py-3">{item.borrowStatus}</td>
                  <td className="px-4 py-3">
                    <div className="flex gap-2">
                      {['ACTIVE', 'PENDING_APPROVAL'].includes(item.borrowStatus) && (
                        <Button size="sm" variant="secondary" disabled={submitting === item.id} onClick={() => onExtend(item.id)}>
                          Extend
                        </Button>
                      )}
                      {['ACTIVE', 'OVERDUE', 'PENDING_APPROVAL'].includes(item.borrowStatus) && (
                        <Button size="sm" disabled={submitting === item.id} onClick={() => onReturn(item.id)}>
                          Return
                        </Button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {!list.length && <div className="px-4 py-8 text-sm text-slate-600 dark:text-slate-300">No records in this tab.</div>}
        </div>
      )}
    </div>
  );
};

export default MyBorrowsPage;
