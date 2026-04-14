import React, { useEffect, useMemo, useState } from 'react';
import { PencilSquareIcon, TrashIcon, BookOpenIcon, MagnifyingGlassIcon, PlusIcon } from '@heroicons/react/24/outline';
import Button from './Button';
import Input from './Input';
import Modal from './Modal';
import { bookRows } from '../data/mockData';
import { createBook, deleteBook, fetchBooks, searchBooks, updateBook } from '../lib/api';
import { useAuth } from '../context/AuthContext';

const pageSize = 6;

const BookTable = () => {
  const { token } = useAuth();
  const [query, setQuery] = useState('');
  const [page, setPage] = useState(1);
  const [sortKey, setSortKey] = useState('title');
  const [books, setBooks] = useState([]);
  const [totalItems, setTotalItems] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [selected, setSelected] = useState(null);
  const [creating, setCreating] = useState(false);
  const [form, setForm] = useState({ title: '', author: '', category: '', isbn: '', totalQuantity: 1 });
  const [formError, setFormError] = useState('');

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
      setError(apiError.message || 'Unable to load books from API. Showing fallback data.');

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
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page, query, token]);

  const sortedRows = useMemo(() => {
    return [...books].sort((a, b) => String(a[sortKey] ?? '').localeCompare(String(b[sortKey] ?? '')));
  }, [books, sortKey]);

  const totalPages = Math.max(1, Math.ceil(totalItems / pageSize));

  const openCreate = () => {
    setCreating(true);
    setSelected(null);
    setForm({ title: '', author: '', category: '', isbn: '', totalQuantity: 1 });
    setFormError('');
  };

  const openEdit = (row) => {
    setSelected(row);
    setCreating(false);
    setForm({
      title: row.title || '',
      author: row.author || '',
      category: row.category || '',
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
      setFormError('Title, ISBN and total quantity are required.');
      return;
    }

    if (!token) {
      setFormError('Missing access token. Please login again.');
      return;
    }

    const payload = {
      title: form.title.trim(),
      author: form.author.trim(),
      category: form.category.trim(),
      isbn: form.isbn.trim(),
      totalQuantity: Number(form.totalQuantity),
    };

    try {
      if (selected?.id) {
        await updateBook(token, selected.id, {
          title: payload.title,
          author: payload.author,
          category: payload.category,
          totalQuantity: payload.totalQuantity,
        });
      } else {
        await createBook(token, payload);
      }

      closeModal();
      loadBooks();
    } catch (saveError) {
      setFormError(saveError.message || 'Unable to save book.');
    }
  };

  const handleDelete = async (id) => {
    if (!token || !id) return;

    try {
      await deleteBook(token, id);
      if (selected?.id === id) closeModal();
      loadBooks();
    } catch (deleteError) {
      setError(deleteError.message || 'Unable to delete book.');
    }
  };

  return (
    <div className="space-y-5">
      <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
        <Input
          label="Search books"
          value={query}
          onChange={(e) => {
            setQuery(e.target.value);
            setPage(1);
          }}
          placeholder="Search title, author, category"
        />
        <Button className="lg:mt-7" onClick={openCreate}>
          <PlusIcon className="h-5 w-5" />Add Book
        </Button>
      </div>

      {error && <p className="text-sm text-amber-600">{error}</p>}

      <div className="overflow-hidden rounded-[28px] border border-slate-200 bg-white shadow-xl dark:border-slate-800 dark:bg-slate-950">
        <table className="min-w-full divide-y divide-slate-200 dark:divide-slate-800">
          <thead className="bg-slate-50 dark:bg-slate-900">
            <tr>
              {['Title', 'Author', 'Category', 'ISBN', 'Stock', 'Status', 'Actions'].map((head) => (
                <th key={head} className="px-5 py-4 text-left text-xs font-bold uppercase tracking-[0.18em] text-slate-500 dark:text-slate-400">
                  <button
                    className="inline-flex items-center gap-2"
                    onClick={() => head !== 'Actions' && setSortKey(head === 'Stock' ? 'totalQuantity' : head.toLowerCase())}
                  >
                    {head}
                    {head !== 'Actions' && <MagnifyingGlassIcon className="h-3.5 w-3.5 opacity-30" />}
                  </button>
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
            {loading ? (
              <tr>
                <td colSpan={7} className="px-5 py-6 text-sm text-slate-500">
                  Loading books...
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
                      <Button variant="ghost" size="sm" onClick={() => openEdit(row)}><PencilSquareIcon className="h-4 w-4" />Edit</Button>
                      <Button variant="ghost" size="sm" className="text-rose-500 hover:bg-rose-500/10" onClick={() => handleDelete(row.id)}><TrashIcon className="h-4 w-4" />Delete</Button>
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      <div className="flex items-center justify-between gap-3">
        <p className="text-sm text-slate-500 dark:text-slate-400">Showing {sortedRows.length} of {totalItems} books</p>
        <div className="flex gap-2">
          <Button variant="secondary" size="sm" disabled={page === 1 || loading} onClick={() => setPage((p) => Math.max(1, p - 1))}>Prev</Button>
          <Button variant="secondary" size="sm" disabled={page === totalPages || loading} onClick={() => setPage((p) => Math.min(totalPages, p + 1))}>Next</Button>
        </div>
      </div>

      <Modal open={creating || !!selected} onClose={closeModal} title={selected ? 'Edit Book' : 'Add Book'}>
        <div className="space-y-4">
          <Input label="Title" value={form.title} onChange={(e) => setForm((prev) => ({ ...prev, title: e.target.value }))} />
          <Input label="Author" value={form.author} onChange={(e) => setForm((prev) => ({ ...prev, author: e.target.value }))} />
          <Input label="Category" value={form.category} onChange={(e) => setForm((prev) => ({ ...prev, category: e.target.value }))} />
          <Input label="ISBN" value={form.isbn} onChange={(e) => setForm((prev) => ({ ...prev, isbn: e.target.value }))} />
          <Input label="Total quantity" type="number" min="1" value={form.totalQuantity} onChange={(e) => setForm((prev) => ({ ...prev, totalQuantity: e.target.value }))} />
          {formError && <p className="text-sm text-rose-500">{formError}</p>}
          <div className="flex justify-end gap-3">
            <Button variant="secondary" onClick={closeModal}>Cancel</Button>
            <Button onClick={handleSave}><BookOpenIcon className="h-4 w-4" />Save</Button>
          </div>
        </div>
      </Modal>
    </div>
  );
};

export default BookTable;
