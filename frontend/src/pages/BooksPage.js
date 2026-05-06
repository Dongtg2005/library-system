import React, { useEffect, useMemo, useState, useRef } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { fetchBooks, searchBooks, autocompleteBooks, fetchTopBorrowedBooks } from '../lib/api';
import useDebounce from '../hooks/useDebounce';
import { useTranslation } from '../context/LanguageContext';
import { formatCategoryName } from '../lib/categoryLabels';
import { buildTopRankMap, getBookBadgeText } from '../lib/bookHotTag';

const statusColors = {
  AVAILABLE: 'bg-emerald-100 text-emerald-700 dark:bg-emerald-500/15 dark:text-emerald-200',
  OUT_OF_STOCK: 'bg-rose-100 text-rose-700 dark:bg-rose-500/15 dark:text-rose-200',
  ARCHIVED: 'bg-slate-200 text-slate-700 dark:bg-slate-700 dark:text-slate-200',
  DAMAGED: 'bg-amber-100 text-amber-700 dark:bg-amber-500/15 dark:text-amber-200',
};

const BooksPage = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { t, language } = useTranslation();

  const [books, setBooks] = useState([]);
  const [topRankMap, setTopRankMap] = useState({});
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
        setError(err.message || t('booksPage.failedToLoad'));
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

  useEffect(() => {
    let mounted = true;

    const loadTopBorrowed = async () => {
      try {
        const topBooks = await fetchTopBorrowedBooks({ limit: 3 });
        if (!mounted) return;
        setTopRankMap(buildTopRankMap(Array.isArray(topBooks) ? topBooks : []));
      } catch {
        if (mounted) setTopRankMap({});
      }
    };

    loadTopBorrowed();
    return () => {
      mounted = false;
    };
  }, []);

  const categories = useMemo(() => {
    const set = new Set();
    books.forEach((book) => {
      if (book.category) set.add(book.category); // Fallback until db seeded ok
      if (book.categories && book.categories.length) {
          book.categories.forEach(c => set.add(c.name));
      }
    });
    return Array.from(set).map((category) => ({ value: category, label: formatCategoryName(category, language) }));
  }, [books, language]);

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
        <h2 className="text-lg font-black">{t('common.filter')}</h2>

        <div className="mt-4 space-y-4">
          <label className="block text-sm font-medium text-slate-600 dark:text-slate-300">
            {t('booksPage.category')}
            <select
              value={categoryFilter}
              onChange={(event) => {
                setPage(0);
                setCategoryFilter(event.target.value);
              }}
              className="mt-2 w-full rounded-2xl border border-slate-200 bg-white px-3 py-2 text-sm dark:border-slate-700 dark:bg-slate-950"
            >
              <option value="">{t('booksPage.allCategories')}</option>
              {categories.map((category) => (
                <option key={category.value} value={category.value}>{category.label}</option>
              ))}
            </select>
          </label>

          <label className="block text-sm font-medium text-slate-600 dark:text-slate-300">
            {t('booksPage.author')}
            <select
              value={authorFilter}
              onChange={(event) => {
                setPage(0);
                setAuthorFilter(event.target.value);
              }}
              className="mt-2 w-full rounded-2xl border border-slate-200 bg-white px-3 py-2 text-sm dark:border-slate-700 dark:bg-slate-950"
            >
              <option value="">{t('booksPage.allAuthors')}</option>
              {authors.map((author) => (
                <option key={author} value={author}>{author}</option>
              ))}
            </select>
          </label>

          <label className="block text-sm font-medium text-slate-600 dark:text-slate-300">
            {t('booksPage.availability')}
            <select
              value={statusFilter}
              onChange={(event) => {
                setPage(0);
                setStatusFilter(event.target.value);
              }}
              className="mt-2 w-full rounded-2xl border border-slate-200 bg-white px-3 py-2 text-sm dark:border-slate-700 dark:bg-slate-950"
            >
              <option value="">{t('booksPage.allStatuses')}</option>
              <option value="AVAILABLE">{t('bookTable.available')}</option>
              <option value="OUT_OF_STOCK">{t('bookTable.outOfStock')}</option>
              <option value="ARCHIVED">{t('bookTable.archived')}</option>
              <option value="DAMAGED">{t('bookTable.damaged')}</option>
            </select>
          </label>
        </div>
      </aside>

      <section className="rounded-[2rem] border border-white/70 bg-white/85 p-6 shadow-sm backdrop-blur dark:border-slate-800 dark:bg-slate-900/75">
        <div className="flex flex-wrap items-center justify-between gap-3 relative" ref={searchContainerRef}>
          <h1 className="text-2xl font-black text-slate-950 dark:text-white">{t('nav.books')}</h1>
          <div className="relative w-full md:w-72">
              <input
                type="search"
                value={searchInput}
                onChange={(event) => {
                  setPage(0);
                  setSearchInput(event.target.value);
                }}
                placeholder={t('booksPage.searchPlaceholder')}
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
          <div className="mt-6 rounded-2xl bg-slate-100 px-4 py-8 text-sm text-slate-600 dark:bg-slate-800 dark:text-slate-300">{t('booksPage.loading')}</div>
        ) : (
          <>
            <div className="mt-6 grid gap-4 md:grid-cols-2 xl:grid-cols-3">
              {books.map((book) => {
                const categoryNames = book.categories?.map(c => c.name).join(', ') || book.category || t('book.label.uncategorized');
                const badgeText = getBookBadgeText(book, topRankMap);
                return (
                <article key={book.id} className="group relative overflow-hidden rounded-3xl border border-slate-200 bg-white p-4 transition-all hover:shadow-xl dark:border-slate-800 dark:bg-slate-950/90">
                  <div className="relative mb-4 aspect-[2/3] overflow-hidden rounded-2xl bg-slate-100 dark:bg-slate-900">
                    {badgeText && (
                      <span className="absolute right-3 top-3 z-10 rounded-full bg-rose-500 px-2.5 py-1 text-[11px] font-black uppercase tracking-wide text-white shadow-lg shadow-rose-500/30">
                        {badgeText}
                      </span>
                    )}
                    {book.coverImageUrl ? (
                      <img 
                        src={book.coverImageUrl} 
                        alt={book.title} 
                        className="h-full w-full object-cover transition-transform duration-500 group-hover:scale-110"
                      />
                    ) : (
                      <div className="flex h-full w-full items-center justify-center bg-gradient-to-br from-slate-100 to-slate-200 dark:from-slate-800 dark:to-slate-900">
                        <span className="text-4xl font-black text-slate-300 dark:text-slate-700">{book.title?.slice(0, 1)}</span>
                      </div>
                    )}
                  </div>
                  <h3 className="text-lg font-bold text-slate-950 dark:text-white line-clamp-1">{book.title}</h3>
                  <p className="mt-1 text-sm text-slate-600 dark:text-slate-300 truncate">{book.author}</p>
                  <p className="mt-3 text-sm text-slate-500 dark:text-slate-400 truncate">{categoryNames}</p>
                  <div className="mt-3 flex items-center justify-between">
                    <span className={`rounded-full px-3 py-1 text-xs font-bold ${statusColors[book.status] || statusColors.ARCHIVED}`}>
                      {book.status}
                    </span>
                    <span className="text-xs text-slate-500 dark:text-slate-400">{book.availableQty}/{book.totalQuantity}</span>
                  </div>
                  <Link to={`/books/${book.id}`} className="mt-4 inline-flex text-sm font-semibold text-primary hover:underline">{t('book.action.viewDetails')}</Link>
                </article>
              )})}
            </div>

            {!books.length && (
              <div className="mt-6 rounded-2xl bg-slate-100 px-4 py-8 text-sm text-slate-600 dark:bg-slate-800 dark:text-slate-300">{t('booksPage.noBooksFound')}</div>
            )}

            <div className="mt-8 flex items-center justify-between">
              <button
                type="button"
                onClick={() => setPage((prev) => Math.max(prev - 1, 0))}
                disabled={page === 0}
                className="rounded-xl border border-slate-200 px-4 py-2 text-sm font-semibold disabled:opacity-50 dark:border-slate-700"
              >
                {t('booksPage.previous')}
              </button>
              <p className="text-sm text-slate-600 dark:text-slate-300">{t('booksPage.page')} {page + 1} / {Math.max(totalPages, 1)}</p>
              <button
                type="button"
                onClick={() => setPage((prev) => (prev + 1 < totalPages ? prev + 1 : prev))}
                disabled={totalPages === 0 || page + 1 >= totalPages}
                className="rounded-xl border border-slate-200 px-4 py-2 text-sm font-semibold disabled:opacity-50 dark:border-slate-700"
              >
                {t('booksPage.next')}
              </button>
            </div>
          </>
        )}
      </section>
    </div>
  );
};

export default BooksPage;
