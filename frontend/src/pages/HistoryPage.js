import React, { useEffect, useState } from 'react';
import { fetchBookById, fetchBorrowHistory } from '../lib/api';
import { useAuth } from '../context/AuthContext';

const HistoryPage = () => {
  const { token } = useAuth();
  const [history, setHistory] = useState([]);
  const [bookMap, setBookMap] = useState({});
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let mounted = true;

    const load = async () => {
      if (!token) {
        if (mounted) {
          setHistory([]);
          setBookMap({});
          setLoading(false);
        }
        return;
      }

      setLoading(true);
      try {
        const data = await fetchBorrowHistory(token);
        if (!mounted) return;
        setHistory(data || []);

        const ids = [...new Set((data || []).map((item) => item.bookId).filter(Boolean))];
        const books = await Promise.all(
          ids.map(async (id) => {
            try {
              return [id, await fetchBookById(id)];
            } catch {
              return [id, null];
            }
          }),
        );
        if (!mounted) return;
        setBookMap(Object.fromEntries(books));
      } catch {
        if (mounted) {
          setHistory([]);
          setBookMap({});
        }
      } finally {
        if (mounted) setLoading(false);
      }
    };

    load();

    return () => {
      mounted = false;
    };
  }, [token]);

  return (
    <div className="rounded-[2rem] border border-white/70 bg-white/85 p-6 page-fade dark:border-slate-800 dark:bg-slate-900/75">
      <h1 className="text-3xl font-black text-slate-950 dark:text-white">Borrow History</h1>
      <p className="mt-2 text-sm text-slate-600 dark:text-slate-300">Timeline built from `/api/v1/borrows/history`.</p>

      {loading ? (
        <div className="mt-6 rounded-2xl bg-slate-100 px-4 py-8 text-sm dark:bg-slate-800">Loading timeline...</div>
      ) : (
        <div className="mt-6 space-y-4">
          {history.map((item) => (
            <div key={item.id} className="rounded-3xl border border-slate-200 bg-white p-4 dark:border-slate-800 dark:bg-slate-950/80">
              <div className="flex flex-wrap items-center justify-between gap-2">
                <h3 className="font-bold text-slate-950 dark:text-white">{bookMap[item.bookId]?.title || item.bookId}</h3>
                <span className="rounded-full bg-slate-100 px-3 py-1 text-xs font-semibold dark:bg-slate-800">{item.borrowStatus}</span>
              </div>
              <p className="mt-2 text-sm text-slate-600 dark:text-slate-300">Borrow: {item.borrowDate || '-'} · Due: {item.dueDate || '-'} · Return: {item.returnDate || '-'}</p>
            </div>
          ))}
          {!history.length && <div className="rounded-2xl bg-slate-100 px-4 py-8 text-sm text-slate-600 dark:bg-slate-800 dark:text-slate-300">No history data available.</div>}
        </div>
      )}
    </div>
  );
};

export default HistoryPage;
