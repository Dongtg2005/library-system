import { PencilSquareIcon, TrashIcon, BookOpenIcon, MagnifyingGlassIcon, PlusIcon, ChevronUpDownIcon, CheckIcon, XMarkIcon as CloseIcon } from '@heroicons/react/24/outline';
import { Listbox, Transition } from '@headlessui/react';
import React, { useEffect, useMemo, useState, Fragment } from 'react';
import Button from './Button';
import Input from './Input';
import Modal from './Modal';
import BookCoverUpload from './BookCoverUpload';
import { bookRows, PagedData } from '../data/mockData';
import { createBook, deleteBook, fetchBooks, searchBooks, updateBook, fetchCategoriesTree } from '../lib/api';
import { useAuth } from '../context/AuthContext';
import { useTranslation } from '../context/LanguageContext';

const pageSize = 6;

const BookTable = () => {
  const { token } = useAuth();
  const { t } = useTranslation();
  const [query, setQuery] = useState('');
  const [page, setPage] = useState(1);
  const [sortKey, setSortKey] = useState('title');
  const [books, setBooks] = useState([]);
  const [totalItems, setTotalItems] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [selected, setSelected] = useState(null);
  const [creating, setCreating] = useState(false);
  const [form, setForm] = useState({ title: '', author: '', categoryIds: [], isbn: '', totalQuantity: 1 });
  const [formError, setFormError] = useState('');
  const [categoryList, setCategoryList] = useState([]);

  const normalizedMockBooks = useMemo(
    () =>
      bookRows.map((row) => ({
        id: row.id,
        title: row.title,
        author: row.author,
        category: row.category,
        isbn: row.isbn,
        totalQuantity: Number(row.stock || 0),
        availableQty: Number(row.available || 0),
        status: String(row.status || 'AVAILABLE').toUpperCase().replace(/\s+/g, '_'),
      })),
    []
  );

  const formatStatus = (status) => String(status || 'UNKNOWN').replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, (s) => s.toUpperCase());

  const loadBooks = async () => {
    setLoading(true);
    setError('');

    try {
      const params = { page: Math.max(page - 1, 0), size: pageSize };
      const payload = query.trim() ? await searchBooks({ ...params, title: query.trim() }) : await fetchBooks(params);

      const content = Array.isArray(payload?.content) ? payload.content : [];
      setBooks(content);
      setTotalItems(Number(payload?.totalElements || content.length));
    } catch (apiError) {
      setError(apiError.message || t('bookTable.loadFailedFallback'));

      const filteredMock = normalizedMockBooks.filter((row) =>
        [row.title, row.author, row.category, row.isbn].some((value) => String(value || '').toLowerCase().includes(query.toLowerCase()))
      );

      setTotalItems(filteredMock.length);
      setBooks(filteredMock.slice((page - 1) * pageSize, page * pageSize));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadBooks();
    // Load categories for the form dropdown
    fetchCategoriesTree(token).then(data => {
      if (Array.isArray(data)) setCategoryList(data);
    }).catch(err => console.error('Failed to load categories', err));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page, query, token]);

  const sortedRows = useMemo(() => {
    return [...books].sort((a, b) => String(a[sortKey] ?? '').localeCompare(String(b[sortKey] ?? '')));
  }, [books, sortKey]);

  const totalPages = Math.max(1, Math.ceil(totalItems / pageSize));

  const openCreate = () => {
    setCreating(true);
    setSelected(null);
    setForm({ title: '', author: '', categoryIds: [], isbn: '', totalQuantity: 1 });
    setFormError('');
  };

  const openEdit = (row) => {
    setSelected(row);
    setCreating(false);
    setForm({
      title: row.title || '',
      author: row.author || '',
      categoryIds: row.categories && row.categories.length > 0 ? row.categories.map(c => c.id) : [],
      isbn: row.isbn || '',
      totalQuantity: Number(row.totalQuantity || 1),
    });
    setFormError('');
  };

  const closeModal = () => {
    setSelected(null);
    setCreating(false);
    setFormError('');
  };

  const handleSave = async () => {
    if (!form.title.trim() || !form.isbn.trim() || !Number(form.totalQuantity)) {
      setFormError(t('bookTable.requiredFields'));
      return;
    }

    if (!token) {
      setFormError(t('auth.loginRequired'));
      return;
    }

    const payload = {
      title: form.title.trim(),
      author: form.author.trim(),
      categoryIds: form.categoryIds,
      isbn: form.isbn.trim(),
      totalQuantity: Number(form.totalQuantity),
    };

    try {
      if (selected?.id) {
        await updateBook(token, selected.id, {
          title: payload.title,
          author: payload.author,
          categoryIds: payload.categoryIds,
          totalQuantity: payload.totalQuantity,
        });
      } else {
        await createBook(token, payload);
      }

      closeModal();
      loadBooks();
    } catch (saveError) {
      setFormError(saveError.message || t('bookTable.saveFailed'));
    }
  };

  const handleDelete = async (id) => {
    if (!token || !id) return;

    try {
      await deleteBook(token, id);
      if (selected?.id === id) closeModal();
      loadBooks();
    } catch (deleteError) {
      setError(deleteError.message || t('bookTable.deleteFailed'));
    }
  };

  return (
    <div className="space-y-5">
      <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
        <Input
          label={t('bookTable.searchBooks')}
          value={query}
          onChange={(e) => {
            setQuery(e.target.value);
            setPage(1);
          }}
          placeholder={t('bookTable.searchTitleAuthorCategory')}
        />
        <Button className="lg:mt-7" onClick={openCreate}>
          <PlusIcon className="h-5 w-5" />{t('bookTable.addBook')}
        </Button>
      </div>

      {error && <p className="text-sm text-amber-600">{error}</p>}

      <div className="overflow-hidden rounded-[28px] border border-slate-200 bg-white shadow-xl dark:border-slate-800 dark:bg-slate-950">
        <div className="overflow-x-auto">
        <table className="min-w-[840px] w-full divide-y divide-slate-200 dark:divide-slate-800">
          <thead className="bg-slate-50 dark:bg-slate-900">
            <tr>
              {[
                { label: t('bookTable.bookTitle'), sort: 'title' },
                { label: t('bookTable.author'), sort: 'author' },
                { label: t('bookTable.category'), sort: 'category' },
                { label: t('bookTable.isbn'), sort: 'isbn' },
                { label: t('bookTable.stock'), sort: 'totalQuantity' },
                { label: t('bookTable.status'), sort: 'status' },
                { label: t('common.actions') },
              ].map((head) => (
                <th key={head.label} className="px-5 py-4 text-left text-xs font-bold uppercase tracking-[0.18em] text-slate-500 dark:text-slate-400">
                  <button
                    className="inline-flex items-center gap-2"
                    onClick={() => head.sort && setSortKey(head.sort)}
                  >
                    {head.label}
                    {head.sort && <MagnifyingGlassIcon className="h-3.5 w-3.5 opacity-30" />}
                  </button>
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
            {loading ? (
              <tr>
                <td colSpan={7} className="px-5 py-6 text-sm text-slate-500">
                  {t('bookTable.loading')}
                </td>
              </tr>
            ) : (
              sortedRows.map((row) => (
                <tr key={row.id} className="transition hover:bg-slate-50/70 dark:hover:bg-slate-900/50">
                  <td className="px-5 py-4 font-semibold text-slate-900 dark:text-white">{row.title}</td>
                  <td className="px-5 py-4 text-slate-600 dark:text-slate-300">{row.author || '-'}</td>
                  <td className="px-5 py-4 text-slate-600 dark:text-slate-300">{row.category || '-'}</td>
                  <td className="px-5 py-4 text-slate-600 dark:text-slate-300">{row.isbn}</td>
                  <td className="px-5 py-4 text-slate-600 dark:text-slate-300">{Number(row.availableQty || 0)}/{Number(row.totalQuantity || 0)}</td>
                  <td className="px-5 py-4">
                    <span className="rounded-full bg-primary/10 px-3 py-1 text-xs font-bold text-primary">{formatStatus(row.status)}</span>
                  </td>
                  <td className="px-5 py-4">
                    <div className="flex items-center gap-2">
                      <Button variant="ghost" size="sm" onClick={() => openEdit(row)}><PencilSquareIcon className="h-4 w-4" />{t('bookTable.edit')}</Button>
                      <Button variant="ghost" size="sm" className="text-rose-500 hover:bg-rose-500/10" onClick={() => handleDelete(row.id)}><TrashIcon className="h-4 w-4" />{t('bookTable.delete')}</Button>
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
        </div>
      </div>

      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <p className="text-sm text-slate-500 dark:text-slate-400">{t('bookTable.showing', { count: sortedRows.length, total: totalItems })}</p>
        <div className="flex flex-wrap gap-2">
          <Button variant="secondary" size="sm" disabled={page === 1 || loading} onClick={() => setPage((p) => Math.max(1, p - 1))}>{t('bookTable.prev')}</Button>
          <Button variant="secondary" size="sm" disabled={page === totalPages || loading} onClick={() => setPage((p) => Math.min(totalPages, p + 1))}>{t('bookTable.next')}</Button>
        </div>
      </div>

      <Modal open={creating || !!selected} onClose={closeModal} title={selected ? t('bookTable.editBook') : t('bookTable.addBookTitle')}>
        <div className="space-y-4">
          <Input label={t('book.label.title')} value={form.title} onChange={(e) => setForm((prev) => ({ ...prev, title: e.target.value }))} />
          <Input label={t('book.label.author')} value={form.author} onChange={(e) => setForm((prev) => ({ ...prev, author: e.target.value }))} />
          
          <div className="space-y-1 relative">
            <label className="mb-2 block text-sm font-bold text-slate-700 dark:text-slate-300">{t('book.label.category')}</label>
            <Listbox
              value={form.categoryIds}
              onChange={(values) => setForm(prev => ({ ...prev, categoryIds: values }))}
              multiple
            >
              <div className="relative mt-1">
                <Listbox.Button className="relative w-full cursor-default rounded-2xl border border-slate-200 bg-white py-2.5 pl-4 pr-10 text-left outline-none focus:border-primary focus:ring-2 focus:ring-primary/20 dark:border-slate-800 dark:bg-slate-950 sm:text-sm transition-all shadow-sm">
                  <span className="block truncate">
                    {form.categoryIds.length === 0 
                      ? <span className="text-slate-400">{t('dropdown.selectOption')}</span>
                      : categoryList.filter(c => form.categoryIds.includes(c.id)).map(c => c.name).join(', ')
                    }
                  </span>
                  <span className="pointer-events-none absolute inset-y-0 right-0 flex items-center pr-2">
                    <ChevronUpDownIcon className="h-5 w-5 text-slate-400" aria-hidden="true" />
                  </span>
                </Listbox.Button>

                <Transition
                  as={Fragment}
                  leave="transition ease-in duration-100"
                  leaveFrom="opacity-100"
                  leaveTo="opacity-0"
                >
                  <Listbox.Options className="absolute z-50 mt-1 max-h-60 w-full overflow-auto rounded-xl bg-white py-1 text-base shadow-2xl ring-1 ring-black/5 focus:outline-none dark:bg-slate-900 sm:text-sm">
                    {categoryList.map((cat) => (
                      <Listbox.Option
                        key={cat.id}
                        className={({ active }) =>
                          `relative cursor-default select-none py-2.5 pl-10 pr-4 transition-colors ${
                            active ? 'bg-primary/10 text-primary' : 'text-slate-900 dark:text-slate-100'
                          }`
                        }
                        value={cat.id}
                      >
                        {({ selected }) => (
                          <>
                            <span className={`block truncate ${selected ? 'font-bold text-primary' : 'font-normal'}`}>
                              {cat.name}
                            </span>
                            {selected ? (
                              <span className="absolute inset-y-0 left-0 flex items-center pl-3 text-primary">
                                <CheckIcon className="h-5 w-5" aria-hidden="true" />
                              </span>
                            ) : null}
                          </>
                        )}
                      </Listbox.Option>
                    ))}
                  </Listbox.Options>
                </Transition>
              </div>
            </Listbox>
            
            {/* Selected Tags Display */}
            {form.categoryIds.length > 0 && (
              <div className="mt-3 flex flex-wrap gap-2">
                {categoryList
                  .filter(c => form.categoryIds.includes(c.id))
                  .map(cat => (
                    <span 
                      key={cat.id} 
                      className="inline-flex items-center gap-1.5 rounded-full bg-primary/10 px-3 py-1 text-xs font-bold text-primary animate-in fade-in zoom-in duration-200"
                    >
                      {cat.name}
                      <button
                        type="button"
                        onClick={() => setForm(prev => ({ 
                          ...prev, 
                          categoryIds: prev.categoryIds.filter(id => id !== cat.id) 
                        }))}
                        className="hover:text-primary-focus p-0.5 rounded-full hover:bg-primary/20 transition-colors"
                      >
                        <CloseIcon className="h-3 w-3" />
                      </button>
                    </span>
                  ))
                }
              </div>
            )}
          </div>

          <Input label={t('book.label.isbn')} value={form.isbn} onChange={(e) => setForm((prev) => ({ ...prev, isbn: e.target.value }))} />
          <Input label={t('bookTable.totalQuantity')} type="number" min="1" value={form.totalQuantity} onChange={(e) => setForm((prev) => ({ ...prev, totalQuantity: e.target.value }))} />
          
          {selected && (
            <div className="pt-2 border-t border-slate-100 dark:border-slate-800">
              <BookCoverUpload 
                bookId={selected.id} 
                currentCoverUrl={selected.coverImageUrl} // Backend provides the full URL or null
                onCoverUpdate={(url) => {
                   // Cập nhật lại row trong state để reload UI
                   setBooks(books.map(b => b.id === selected.id ? { ...b, coverImageUrl: url } : b));
                }}
              />
            </div>
          )}

          {formError && <p className="text-sm text-rose-500">{formError}</p>}
          <div className="flex justify-end gap-3">
            <Button variant="secondary" onClick={closeModal}>{t('common.cancel')}</Button>
            <Button onClick={handleSave}><BookOpenIcon className="h-4 w-4" />{t('bookTable.saveBook')}</Button>
          </div>
        </div>
      </Modal>
    </div>
  );
};

export default BookTable;
