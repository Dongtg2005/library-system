import React, { useEffect, useMemo, useState, useRef } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { fetchBooks, searchBooks, autocompleteBooks } from '../lib/api';
import useDebounce from '../hooks/useDebounce';

const statusColors = {
  AVAILABLE: 'bg-emerald-100 text-emerald-700 dark:bg-emerald-500/15 dark:text-emerald-200',
  OUT_OF_STOCK: 'bg-rose-100 text-rose-700 dark:bg-rose-500/15 dark:text-rose-200',
  ARCHIVED: 'bg-slate-200 text-slate-700 dark:bg-slate-700 dark:text-slate-200',
  DAMAGED: 'bg-amber-100 text-amber-700 dark:bg-amber-500/15 dark:text-amber-200',
};

const BooksPage = () => {
  const location = useLocation();
  const navigate = useNavigate();

  const [books, setBooks] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Lấy state từ URL (Shareable Links)
  const queryParams = new URLSearchParams(location.search);
  const initialQ = queryParams.get('q') || '';
  const initialCategory = queryParams.get('category') || '';
  const initialAuthor = queryParams.get('author') || '';
  const initialStatus = queryParams.get('status') || '';
  const initialPage = parseInt(queryParams.get('page') || '0', 10);

  const [searchInput, setSearchInput] = useState(initialQ);
  const [categoryFilter, setCategoryFilter] = useState(initialCategory);
  const [authorFilter, setAuthorFilter] = useState(initialAuthor);
  const [statusFilter, setStatusFilter] = useState(initialStatus);
  const [suggestions, setSuggestions] = useState([]);
  const [showSuggestions, setShowSuggestions] = useState(false);
  const searchContainerRef = useRef(null);

  // Áp dụng Debounce cho ô tìm kiếm
  const debouncedSearchTerm = useDebounce(searchInput, 500);

  // Đồng bộ State lên URL
  useEffect(() => {
    const params = new URLSearchParams();
    if (debouncedSearchTerm) params.set('q', debouncedSearchTerm);
    if (categoryFilter) params.set('category', categoryFilter);
    if (authorFilter) params.set('author', authorFilter);
    if (statusFilter) params.set('status', statusFilter);
    if (page > 0) params.set('page', page);

    navigate({ search: params.toString() }, { replace: true });
  }, [debouncedSearchTerm, categoryFilter, authorFilter, statusFilter, page, navigate]);

  // Autocomplete logic
  useEffect(() => {
    let mounted = true;
    const fetchSuggestions = async () => {
      if (debouncedSearchTerm.trim().length >= 2) {
        try {
          const results = await autocompleteBooks(debouncedSearchTerm);
          if (mounted) {
            setSuggestions(results);
            setShowSuggestions(true);
          }
        } catch (error) {
          console.error("Autocomplete error:", error);
        }
      } else {
        setSuggestions([]);
        setShowSuggestions(false);
      }
    };
    fetchSuggestions();
    return () => { mounted = false; };
  }, [debouncedSearchTerm]);

  // Đóng dropdown khi click ra ngoài
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (searchContainerRef.current && !searchContainerRef.current.contains(event.target)) {
        setShowSuggestions(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  // Load sách chính
  useEffect(() => {
    let mounted = true;

    const load = async () => {
      setLoading(true);
      setError('');
      try {
        const hasFilters = debouncedSearchTerm || categoryFilter || authorFilter || statusFilter;
        const params = {
          page,
          size: 12,
          ...(hasFilters
            ? {
                q: debouncedSearchTerm,
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
  }, [page, debouncedSearchTerm, categoryFilter, authorFilter, statusFilter]);

  const categories = useMemo(() => {
    const set = new Set();
    books.forEach((book) => {
      if (book.category) set.add(book.category); // Fallback until db seeded ok
      if (book.categories && book.categories.length) {
          book.categories.forEach(c => set.add(c.name));
      }
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
              <option value="">All Categories</option>
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
              <option value="">All Authors</option>
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
              <option value="">All Statuses</option>
              <option value="AVAILABLE">Available</option>
              <option value="OUT_OF_STOCK">Out of stock</option>
              <option value="ARCHIVED">Archived</option>
              <option value="DAMAGED">Damaged</option>
            </select>
          </label>
        </div>
      </aside>

      <section className="rounded-[2rem] border border-white/70 bg-white/85 p-6 shadow-sm backdrop-blur dark:border-slate-800 dark:bg-slate-900/75">
        <div className="flex flex-wrap items-center justify-between gap-3 relative" ref={searchContainerRef}>
          <h1 className="text-2xl font-black text-slate-950 dark:text-white">Books</h1>
          <div className="relative w-full md:w-72">
              <input
                type="search"
                value={searchInput}
                onChange={(event) => {
                  setPage(0);
                  setSearchInput(event.target.value);
                }}
                placeholder="Search by title, author, or ISBN..."
                className="w-full rounded-2xl border border-slate-200 bg-white px-4 py-2 text-sm outline-none focus:border-primary dark:border-slate-700 dark:bg-slate-950"
              />
              
              {/* Autocomplete Dropdown */}
              {showSuggestions && suggestions.length > 0 && (
                  <div className="absolute z-10 w-full mt-2 bg-white rounded-xl shadow-lg border border-slate-100 dark:bg-slate-800 dark:border-slate-700 max-h-60 overflow-y-auto overflow-x-hidden">
                      {suggestions.map((suggestion) => (
                          <Link 
                              key={suggestion.id} 
                              to={`/books/${suggestion.id}`}
                              className="block px-4 py-3 hover:bg-slate-50 dark:hover:bg-slate-700/50 text-sm border-b border-slate-100 dark:border-slate-700/50 last:border-0"
                          >
                              <div className="font-semibold text-slate-900 dark:text-white truncate">{suggestion.title}</div>
                              <div className="text-slate-500 dark:text-slate-400 text-xs truncate mt-0.5">{suggestion.author} - ISBN: {suggestion.isbn}</div>
                          </Link>
                      ))}
                  </div>
              )}
          </div>
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
              {books.map((book) => {
                const categoryNames = book.categories?.map(c => c.name).join(', ') || book.category || 'Uncategorized';
                return (
                <article key={book.id} className="rounded-3xl border border-slate-200 bg-white p-5 dark:border-slate-800 dark:bg-slate-950/90">
                  <h3 className="text-lg font-bold text-slate-950 dark:text-white line-clamp-1">{book.title}</h3>
                  <p className="mt-1 text-sm text-slate-600 dark:text-slate-300 truncate">{book.author}</p>
                  <p className="mt-3 text-sm text-slate-500 dark:text-slate-400 truncate">{categoryNames}</p>
                  <div className="mt-3 flex items-center justify-between">
                    <span className={`rounded-full px-3 py-1 text-xs font-bold ${statusColors[book.status] || statusColors.ARCHIVED}`}>
                      {book.status}
                    </span>
                    <span className="text-xs text-slate-500 dark:text-slate-400">{book.availableQty}/{book.totalQuantity}</span>
                  </div>
                  <Link to={`/books/${book.id}`} className="mt-4 inline-flex text-sm font-semibold text-primary hover:underline">View Detail</Link>
                </article>
              )})}
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
