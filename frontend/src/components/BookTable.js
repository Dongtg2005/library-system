import React, { useMemo, useState } from 'react';
import { PencilSquareIcon, TrashIcon, BookOpenIcon, MagnifyingGlassIcon } from '@heroicons/react/24/outline';
import Button from './Button';
import Input from './Input';
import Modal from './Modal';
import { bookRows } from '../data/mockData';

const BookTable = () => {
  const [query, setQuery] = useState('');
  const [page, setPage] = useState(1);
  const [selected, setSelected] = useState(null);
  const [sortKey, setSortKey] = useState('title');
  const pageSize = 4;

  const filtered = useMemo(() => {
    const next = bookRows.filter((row) => [row.title, row.author, row.category, row.isbn].some((value) => value.toLowerCase().includes(query.toLowerCase())));
    return [...next].sort((a, b) => String(a[sortKey]).localeCompare(String(b[sortKey])));
  }, [query, sortKey]);

  const totalPages = Math.max(1, Math.ceil(filtered.length / pageSize));
  const pageRows = filtered.slice((page - 1) * pageSize, page * pageSize);

  return (
    <div className="space-y-5">
      <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
        <Input label="Search books" value={query} onChange={(e) => { setQuery(e.target.value); setPage(1); }} placeholder="Search title, author, category" />
        <Button className="lg:mt-7"><BookOpenIcon className="h-5 w-5" />Add Book</Button>
      </div>

      <div className="overflow-hidden rounded-[28px] border border-slate-200 bg-white shadow-xl dark:border-slate-800 dark:bg-slate-950">
        <table className="min-w-full divide-y divide-slate-200 dark:divide-slate-800">
          <thead className="bg-slate-50 dark:bg-slate-900">
            <tr>
              {['Title', 'Author', 'Category', 'ISBN', 'Stock', 'Status', 'Actions'].map((head) => (
                <th key={head} className="px-5 py-4 text-left text-xs font-bold uppercase tracking-[0.18em] text-slate-500 dark:text-slate-400">
                  <button className="inline-flex items-center gap-2" onClick={() => head !== 'Actions' && setSortKey(head.toLowerCase())}>
                    {head}
                    {head !== 'Actions' && <MagnifyingGlassIcon className="h-3.5 w-3.5 opacity-30" />}
                  </button>
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
            {pageRows.map((row) => (
              <tr key={row.id} className="transition hover:bg-slate-50/70 dark:hover:bg-slate-900/50">
                <td className="px-5 py-4 font-semibold text-slate-900 dark:text-white">{row.title}</td>
                <td className="px-5 py-4 text-slate-600 dark:text-slate-300">{row.author}</td>
                <td className="px-5 py-4 text-slate-600 dark:text-slate-300">{row.category}</td>
                <td className="px-5 py-4 text-slate-600 dark:text-slate-300">{row.isbn}</td>
                <td className="px-5 py-4 text-slate-600 dark:text-slate-300">{row.available}/{row.stock}</td>
                <td className="px-5 py-4"><span className={`rounded-full px-3 py-1 text-xs font-bold ${row.status === 'Available' ? 'bg-emerald-500/10 text-emerald-600' : row.status === 'Low stock' ? 'bg-amber-500/10 text-amber-600' : 'bg-rose-500/10 text-rose-600'}`}>{row.status}</span></td>
                <td className="px-5 py-4">
                  <div className="flex items-center gap-2">
                    <Button variant="ghost" size="sm" onClick={() => setSelected(row)}><PencilSquareIcon className="h-4 w-4" />Edit</Button>
                    <Button variant="ghost" size="sm" className="text-rose-500 hover:bg-rose-500/10"><TrashIcon className="h-4 w-4" />Delete</Button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="flex items-center justify-between gap-3">
        <p className="text-sm text-slate-500 dark:text-slate-400">Showing {pageRows.length} of {filtered.length} books</p>
        <div className="flex gap-2">
          <Button variant="secondary" size="sm" disabled={page === 1} onClick={() => setPage((p) => Math.max(1, p - 1))}>Prev</Button>
          <Button variant="secondary" size="sm" disabled={page === totalPages} onClick={() => setPage((p) => Math.min(totalPages, p + 1))}>Next</Button>
        </div>
      </div>

      <Modal open={!!selected} onClose={() => setSelected(null)} title="Edit Book">
        {selected && (
          <div className="space-y-4">
            <Input label="Title" defaultValue={selected.title} />
            <Input label="Author" defaultValue={selected.author} />
            <Input label="Category" defaultValue={selected.category} />
            <div className="flex justify-end gap-3">
              <Button variant="secondary" onClick={() => setSelected(null)}>Cancel</Button>
              <Button>Save</Button>
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
};

export default BookTable;
