import React, { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { fetchBooks } from '../lib/api';

const CategoriesPage = () => {
  const [books, setBooks] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let mounted = true;

    const load = async () => {
      setLoading(true);
      try {
        const response = await fetchBooks({ page: 0, size: 100 });
        if (mounted) setBooks(response?.content || []);
      } catch {
        if (mounted) setBooks([]);
      } finally {
        if (mounted) setLoading(false);
      }
    };

    load();

    return () => {
      mounted = false;
    };
  }, []);

  const categories = useMemo(() => {
    const map = books.reduce((acc, book) => {
      const category = book.category || 'Uncategorized';
      acc[category] = (acc[category] || 0) + 1;
      return acc;
    }, {});

    return Object.entries(map)
      .map(([name, total]) => ({ name, total }))
      .sort((a, b) => b.total - a.total);
  }, [books]);

  return (
    <div className="rounded-[2rem] border border-white/70 bg-white/85 p-6 page-fade dark:border-slate-800 dark:bg-slate-900/75">
      <h1 className="text-3xl font-black text-slate-950 dark:text-white">Categories</h1>
      <p className="mt-2 text-sm text-slate-600 dark:text-slate-300">Aggregated from `/api/v1/books` response.</p>

      {loading ? (
        <div className="mt-6 rounded-2xl bg-slate-100 px-4 py-8 text-sm dark:bg-slate-800">Loading categories...</div>
      ) : (
        <div className="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {categories.map((category) => (
            <Link key={category.name} to={`/books?category=${encodeURIComponent(category.name)}`} className="rounded-3xl border border-slate-200 bg-white p-5 transition hover:-translate-y-1 hover:shadow-lg dark:border-slate-800 dark:bg-slate-950/90">
              <p className="text-xs uppercase tracking-[0.2em] text-slate-500 dark:text-slate-400">Category</p>
              <h3 className="mt-2 text-xl font-black text-slate-950 dark:text-white">{category.name}</h3>
              <p className="mt-2 text-sm text-slate-600 dark:text-slate-300">{category.total} books</p>
            </Link>
          ))}
          {!categories.length && (
            <div className="col-span-full rounded-2xl bg-slate-100 px-4 py-8 text-sm text-slate-600 dark:bg-slate-800 dark:text-slate-300">No categories found.</div>
          )}
        </div>
      )}
    </div>
  );
};

export default CategoriesPage;
