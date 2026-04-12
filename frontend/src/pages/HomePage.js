import React, { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { ArrowRightIcon, MagnifyingGlassIcon, SparklesIcon } from '@heroicons/react/24/outline';
import Button from '../components/Button';
import { useAuth } from '../context/AuthContext';
import { fetchBooks, fetchBorrowHistory } from '../lib/api';

const HomePage = () => {
  const { isAuthenticated, token, user } = useAuth();
  const [books, setBooks] = useState([]);
  const [borrowHistory, setBorrowHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');

  useEffect(() => {
    let mounted = true;

    const load = async () => {
      setLoading(true);
      try {
        const booksData = await fetchBooks({ page: 0, size: 12 });
        if (mounted) {
          setBooks(booksData?.content || []);
        }
      } catch {
        if (mounted) setBooks([]);
      }

      if (isAuthenticated && token) {
        try {
          const history = await fetchBorrowHistory(token);
          if (mounted) setBorrowHistory(history || []);
        } catch {
          if (mounted) setBorrowHistory([]);
        }
      } else if (mounted) {
        setBorrowHistory([]);
      }

      if (mounted) setLoading(false);
    };

    load();

    return () => {
      mounted = false;
    };
  }, [isAuthenticated, token]);

  const featured = useMemo(() => {
    const keyword = search.trim().toLowerCase();
    const source = books.slice(0, 8);
    if (!keyword) return source;
    return source.filter((book) =>
      [book.title, book.author, book.category].some((value) => value?.toLowerCase().includes(keyword)),
    );
  }, [books, search]);

  const categories = useMemo(() => {
    const map = books.reduce((acc, book) => {
      const category = book.category || 'Uncategorized';
      acc[category] = (acc[category] || 0) + 1;
      return acc;
    }, {});

    return Object.entries(map).map(([name, total]) => ({ name, total }));
  }, [books]);

  const activeBorrows = borrowHistory.filter((item) => ['ACTIVE', 'PENDING_APPROVAL'].includes(item.borrowStatus)).length;
  const overdue = borrowHistory.filter((item) => item.borrowStatus === 'OVERDUE').length;

  return (
    <div className="space-y-8 page-fade">
      <section className="grid gap-6 lg:grid-cols-[1.3fr_0.7fr]">
        <div className="rounded-[2rem] border border-white/70 bg-white/85 p-8 shadow-[0_30px_120px_rgba(15,23,42,0.08)] backdrop-blur dark:border-slate-800 dark:bg-slate-900/75">
          <div className="inline-flex items-center gap-2 rounded-full bg-orange-100 px-4 py-2 text-sm font-semibold text-orange-700 dark:bg-orange-500/15 dark:text-orange-200">
            <SparklesIcon className="h-4 w-4" />
            Discover Your Next Favorite Book
          </div>

          <h1 className="mt-6 max-w-3xl text-4xl font-black leading-tight text-slate-950 dark:text-white sm:text-5xl">
            Browse live catalog data, borrow quickly, and track every due date in one place.
          </h1>

          <p className="mt-4 max-w-3xl text-slate-600 dark:text-slate-300">
            This home page reads real data from your backend endpoints for books and borrow history.
          </p>

          <div className="mt-6 flex flex-wrap gap-3">
            <Link to="/books">
              <Button size="lg">
                Browse Books
                <ArrowRightIcon className="h-4 w-4" />
              </Button>
            </Link>
            {!isAuthenticated && (
              <Link to="/register">
                <Button size="lg" variant="secondary">Join Now</Button>
              </Link>
            )}
            {isAuthenticated && (
              <Link to="/my-books">
                <Button size="lg" variant="secondary">My Books</Button>
              </Link>
            )}
          </div>

          <div className="mt-8 grid gap-4 sm:grid-cols-3">
            <div className="rounded-3xl bg-slate-100 p-4 dark:bg-slate-800/80">
              <p className="text-sm text-slate-500 dark:text-slate-400">Catalog books</p>
              <p className="mt-2 text-3xl font-black">{books.length}</p>
            </div>
            <div className="rounded-3xl bg-slate-100 p-4 dark:bg-slate-800/80">
              <p className="text-sm text-slate-500 dark:text-slate-400">Active borrows</p>
              <p className="mt-2 text-3xl font-black">{activeBorrows}</p>
            </div>
            <div className="rounded-3xl bg-slate-100 p-4 dark:bg-slate-800/80">
              <p className="text-sm text-slate-500 dark:text-slate-400">Overdue</p>
              <p className="mt-2 text-3xl font-black">{overdue}</p>
            </div>
          </div>
        </div>

        <div className="rounded-[2rem] border border-white/70 bg-white/85 p-6 shadow-[0_20px_80px_rgba(15,23,42,0.08)] backdrop-blur dark:border-slate-800 dark:bg-slate-900/75">
          <p className="text-sm font-semibold uppercase tracking-[0.25em] text-slate-500 dark:text-slate-400">Search</p>
          <div className="relative mt-4">
            <MagnifyingGlassIcon className="absolute left-4 top-1/2 h-5 w-5 -translate-y-1/2 text-slate-400" />
            <input
              value={search}
              onChange={(event) => setSearch(event.target.value)}
              placeholder="Search books..."
              className="w-full rounded-2xl border border-slate-200 bg-slate-50 py-3 pl-11 pr-4 text-sm outline-none transition focus:border-primary dark:border-slate-700 dark:bg-slate-950"
            />
          </div>
          <div className="mt-4 space-y-3">
            <Link to="/books" className="block rounded-2xl bg-slate-100 px-4 py-3 text-sm font-semibold dark:bg-slate-800">Category filter</Link>
            <Link to="/books" className="block rounded-2xl bg-slate-100 px-4 py-3 text-sm font-semibold dark:bg-slate-800">Author filter</Link>
          </div>
        </div>
      </section>

      <section className="rounded-[2rem] border border-white/70 bg-white/80 p-6 backdrop-blur dark:border-slate-800 dark:bg-slate-900/70">
        <div className="flex items-center justify-between">
          <h2 className="text-2xl font-black text-slate-950 dark:text-white">Featured Books</h2>
          <Link to="/books" className="text-sm font-semibold text-primary">View all</Link>
        </div>

        {loading ? (
          <div className="mt-6 rounded-3xl bg-slate-100 p-8 text-sm text-slate-500 dark:bg-slate-800">Loading books...</div>
        ) : (
          <div className="mt-6 grid gap-4 md:grid-cols-2 xl:grid-cols-4">
            {featured.map((book) => (
              <article key={book.id} className="rounded-[1.5rem] border border-slate-200 bg-white p-5 transition hover:-translate-y-1 hover:shadow-lg dark:border-slate-800 dark:bg-slate-950/90">
                <div className="rounded-[1.25rem] bg-[linear-gradient(145deg,#0f172a,#334155_55%,#fb923c_140%)] p-5 text-white">
                  <p className="text-xs uppercase tracking-[0.25em] text-white/70">{book.category || 'General'}</p>
                  <p className="mt-2 truncate text-lg font-bold">{book.title}</p>
                </div>
                <p className="mt-4 text-sm text-slate-600 dark:text-slate-300">{book.author}</p>
                <p className="mt-2 text-sm text-slate-500 dark:text-slate-400">{book.status} · {book.availableQty}/{book.totalQuantity} available</p>
                <Link to={`/books/${book.id}`} className="mt-4 inline-flex text-sm font-semibold text-primary">View Detail</Link>
              </article>
            ))}
            {!featured.length && (
              <div className="col-span-full rounded-2xl bg-slate-100 px-4 py-8 text-sm text-slate-600 dark:bg-slate-800 dark:text-slate-300">
                No books found from the API.
              </div>
            )}
          </div>
        )}
      </section>

      <section className="grid gap-6 md:grid-cols-2">
        <div className="rounded-[2rem] border border-white/70 bg-white/80 p-6 dark:border-slate-800 dark:bg-slate-900/70">
          <h3 className="text-xl font-black text-slate-950 dark:text-white">Categories</h3>
          <div className="mt-4 space-y-3">
            {categories.map((category) => (
              <div key={category.name} className="flex items-center justify-between rounded-2xl bg-slate-100 px-4 py-3 dark:bg-slate-800">
                <span className="font-semibold">{category.name}</span>
                <span className="text-sm text-slate-500 dark:text-slate-400">{category.total} books</span>
              </div>
            ))}
            {!categories.length && (
              <div className="rounded-2xl bg-slate-100 px-4 py-6 text-sm text-slate-600 dark:bg-slate-800 dark:text-slate-300">
                Category data will appear once books include category values.
              </div>
            )}
          </div>
        </div>

        <div className="rounded-[2rem] border border-white/70 bg-white/80 p-6 dark:border-slate-800 dark:bg-slate-900/70">
          <h3 className="text-xl font-black text-slate-950 dark:text-white">Account Activity</h3>
          <p className="mt-2 text-sm text-slate-600 dark:text-slate-300">
            {isAuthenticated ? `Signed in as ${user?.fullName || user?.email}` : 'Sign in to see your borrowing activity.'}
          </p>
          <div className="mt-4 space-y-3">
            {borrowHistory.slice(0, 4).map((item) => (
              <div key={item.id} className="rounded-2xl border border-slate-200 px-4 py-3 text-sm dark:border-slate-800">
                <p className="font-semibold">{item.borrowStatus}</p>
                <p className="text-slate-500 dark:text-slate-400">Record: {item.id}</p>
              </div>
            ))}
            {isAuthenticated && !borrowHistory.length && (
              <div className="rounded-2xl bg-slate-100 px-4 py-6 text-sm text-slate-600 dark:bg-slate-800 dark:text-slate-300">
                No borrow history yet.
              </div>
            )}
          </div>
        </div>
      </section>
    </div>
  );
};

export default HomePage;
