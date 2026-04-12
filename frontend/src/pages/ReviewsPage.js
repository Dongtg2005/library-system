import React, { useEffect, useState } from 'react';
import { fetchBorrowHistory } from '../lib/api';
import { useAuth } from '../context/AuthContext';

const ReviewsPage = () => {
  const { token } = useAuth();
  const [borrowCount, setBorrowCount] = useState(0);

  useEffect(() => {
    let mounted = true;

    const load = async () => {
      if (!token) return;
      try {
        const history = await fetchBorrowHistory(token);
        if (mounted) setBorrowCount((history || []).length);
      } catch {
        if (mounted) setBorrowCount(0);
      }
    };

    load();

    return () => {
      mounted = false;
    };
  }, [token]);

  return (
    <div className="rounded-[2rem] border border-white/70 bg-white/85 p-6 page-fade dark:border-slate-800 dark:bg-slate-900/75">
      <h1 className="text-3xl font-black text-slate-950 dark:text-white">Reviews</h1>
      <p className="mt-2 text-sm text-slate-600 dark:text-slate-300">Borrow records detected: {borrowCount}</p>

      <div className="mt-6 rounded-2xl border border-slate-200 px-4 py-6 text-sm text-slate-600 dark:border-slate-800 dark:text-slate-300">
        Review endpoints are not exposed in the current backend. This page is wired and ready to consume API once available.
      </div>
    </div>
  );
};

export default ReviewsPage;
