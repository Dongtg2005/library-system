import React, { useEffect, useMemo, useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { fetchBooks, searchBooks } from '../lib/api';

const statusColors = {
  AVAILABLE: 'bg-emerald-100 text-emerald-700 dark:bg-emerald-500/15 dark:text-emerald-200',
  OUT_OF_STOCK: 'bg-rose-100 text-rose-700 dark:bg-rose-500/15 dark:text-rose-200',
  ARCHIVED: 'bg-slate-200 text-slate-700 dark:bg-slate-700 dark:text-slate-200',
  DAMAGED: 'bg-amber-100 text-amber-700 dark:bg-amber-500/15 dark:text-amber-200',
};

const BooksPage = () => {
  const location = useLocation();
  const [books, setBooks] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [searchInput, setSearchInput] = useState('');
  const [categoryFilter, setCategoryFilter] = useState('');
  const [authorFilter, setAuthorFilter] = useState('');
  const [statusFilter, setStatusFilter] = useState('');

  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const category = params.get('category') || '';
    setCategoryFilter(category);
    setPage(0);
  }, [location.search]);

  useEffect(() => {
    let mounted = true;

    const load = async () => {
      setLoading(true);
      setError('');
      try {
        const hasFilters = searchInput || categoryFilter || authorFilter || statusFilter;
        const params = {
          page,
          size: 12,
          ...(hasFilters
            ? {
                title: searchInput,
                author: authorFilter,
                category: categoryFilter,
                status: statusFilter,
              }
            : {}),
        };

        const response = hasFilters ? await searchBooks(params) : await fetchBooks(params);
        if (!mounted) return;

        setBooks(response?.content || []);
        setTotalPages(response?.totalPages || 0);
      } catch (err) {
        if (!mounted) return;
        setError(err.message || 'Failed to load books');
        setBooks([]);
        setTotalPages(0);
      } finally {
        if (mounted) setLoading(false);
      }
    };

    load();

    return () => {
      mounted = false;
    };
  }, [page, searchInput, categoryFilter, authorFilter, statusFilter]);

  const categories = useMemo(() => {
    const set = new Set();
    books.forEach((book) => {
      if (book.category) set.add(book.category);
    });
    return Array.from(set);
  }, [books]);

  const authors = useMemo(() => {
    const set = new Set();
    books.forEach((book) => {
      if (book.author) set.add(book.author);
    });
    return Array.from(set);
  }, [books]);

  return (
    <div className="grid gap-6 lg:grid-cols-[0.3fr_0.7fr] page-fade">
      <aside className="rounded-[2rem] border border-white/70 bg-white/85 p-5 shadow-sm backdrop-blur dark:border-slate-800 dark:bg-slate-900/75">
        <h2 className="text-lg font-black">Filters</h2>

        <div className="mt-4 space-y-4">
          <label className="block text-sm font-medium text-slate-600 dark:text-slate-300">
            Category
            <select
              value={categoryFilter}
              onChange={(event) => {
                setPage(0);
                setCategoryFilter(event.target.value);
              }}
              className="mt-2 w-full rounded-2xl border border-slate-200 bg-white px-3 py-2 text-sm dark:border-slate-700 dark:bg-slate-950"
            >
              <option value="">All</option>
              {categories.map((category) => (
                <option key={category} value={category}>{category}</option>
              ))}
            </select>
          </label>

          <label className="block text-sm font-medium text-slate-600 dark:text-slate-300">
            Author
            <select
              value={authorFilter}
              onChange={(event) => {
                setPage(0);
                setAuthorFilter(event.target.value);
              }}
              className="mt-2 w-full rounded-2xl border border-slate-200 bg-white px-3 py-2 text-sm dark:border-slate-700 dark:bg-slate-950"
            >
              <option value="">All</option>
              {authors.map((author) => (
                <option key={author} value={author}>{author}</option>
              ))}
            </select>
          </label>

          <label className="block text-sm font-medium text-slate-600 dark:text-slate-300">
            Availability
            <select
              value={statusFilter}
              onChange={(event) => {
                setPage(0);
                setStatusFilter(event.target.value);
              }}
              className="mt-2 w-full rounded-2xl border border-slate-200 bg-white px-3 py-2 text-sm dark:border-slate-700 dark:bg-slate-950"
            >
              <option value="">All</option>
              <option value="AVAILABLE">Available</option>
              <option value="OUT_OF_STOCK">Out of stock</option>
              <option value="ARCHIVED">Archived</option>
              <option value="DAMAGED">Damaged</option>
            </select>
          </label>
        </div>
      </aside>

      <section className="rounded-[2rem] border border-white/70 bg-white/85 p-6 shadow-sm backdrop-blur dark:border-slate-800 dark:bg-slate-900/75">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <h1 className="text-2xl font-black text-slate-950 dark:text-white">Books</h1>
          <input
            type="search"
            value={searchInput}
            onChange={(event) => {
              setPage(0);
              setSearchInput(event.target.value);
            }}
            placeholder="Search books..."
            className="w-full rounded-2xl border border-slate-200 bg-white px-4 py-2 text-sm outline-none focus:border-primary md:w-72 dark:border-slate-700 dark:bg-slate-950"
          />
        </div>

        {error && (
          <div className="mt-4 rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700 dark:border-rose-500/20 dark:bg-rose-500/10 dark:text-rose-200">
            {error}
          </div>
        )}

        {loading ? (
          <div className="mt-6 rounded-2xl bg-slate-100 px-4 py-8 text-sm text-slate-600 dark:bg-slate-800 dark:text-slate-300">Loading books...</div>
        ) : (
          <>
            <div className="mt-6 grid gap-4 md:grid-cols-2 xl:grid-cols-3">
              {books.map((book) => (
                <article key={book.id} className="rounded-3xl border border-slate-200 bg-white p-5 dark:border-slate-800 dark:bg-slate-950/90">
                  <h3 className="text-lg font-bold text-slate-950 dark:text-white">{book.title}</h3>
                  <p className="mt-1 text-sm text-slate-600 dark:text-slate-300">{book.author}</p>
                  <p className="mt-3 text-sm text-slate-500 dark:text-slate-400">{book.category || 'Uncategorized'}</p>
                  <div className="mt-3 flex items-center justify-between">
                    <span className={`rounded-full px-3 py-1 text-xs font-bold ${statusColors[book.status] || statusColors.ARCHIVED}`}>
                      {book.status}
                    </span>
                    <span className="text-xs text-slate-500 dark:text-slate-400">{book.availableQty}/{book.totalQuantity}</span>
                  </div>
                  <Link to={`/books/${book.id}`} className="mt-4 inline-flex text-sm font-semibold text-primary">View Detail</Link>
                </article>
              ))}
            </div>

            {!books.length && (
              <div className="mt-6 rounded-2xl bg-slate-100 px-4 py-8 text-sm text-slate-600 dark:bg-slate-800 dark:text-slate-300">No books found.</div>
            )}

            <div className="mt-8 flex items-center justify-between">
              <button
                type="button"
                onClick={() => setPage((prev) => Math.max(prev - 1, 0))}
                disabled={page === 0}
                className="rounded-xl border border-slate-200 px-4 py-2 text-sm font-semibold disabled:opacity-50 dark:border-slate-700"
              >
                Previous
              </button>
              <p className="text-sm text-slate-600 dark:text-slate-300">Page {page + 1} / {Math.max(totalPages, 1)}</p>
              <button
                type="button"
                onClick={() => setPage((prev) => (prev + 1 < totalPages ? prev + 1 : prev))}
                disabled={totalPages === 0 || page + 1 >= totalPages}
                className="rounded-xl border border-slate-200 px-4 py-2 text-sm font-semibold disabled:opacity-50 dark:border-slate-700"
              >
                Next
              </button>
            </div>
          </>
        )}
      </section>
    </div>
  );
};

export default BooksPage;
