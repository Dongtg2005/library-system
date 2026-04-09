import React, { useMemo, useState } from 'react';
import { PencilSquareIcon, TrashIcon, MagnifyingGlassIcon } from '@heroicons/react/24/outline';
import Button from './Button';
import Input from './Input';
import Modal from './Modal';
import Dropdown from './Dropdown';
import { userRows } from '../data/mockData';

const UserTable = () => {
  const [query, setQuery] = useState('');
  const [page, setPage] = useState(1);
  const [sortKey, setSortKey] = useState('name');
  const [selected, setSelected] = useState(null);
  const [statusFilter, setStatusFilter] = useState('All');
  const pageSize = 4;

  const filtered = useMemo(() => {
    const next = userRows.filter((row) => {
      const matchesSearch = [row.name, row.email, row.role].some((value) => value.toLowerCase().includes(query.toLowerCase()));
      const matchesStatus = statusFilter === 'All' || row.status === statusFilter;
      return matchesSearch && matchesStatus;
    });

    return [...next].sort((a, b) => String(a[sortKey]).localeCompare(String(b[sortKey])));
  }, [query, sortKey, statusFilter]);

  const totalPages = Math.max(1, Math.ceil(filtered.length / pageSize));
  const pageRows = filtered.slice((page - 1) * pageSize, page * pageSize);

  return (
    <div className="space-y-5">
      <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
        <Input label="Search users" value={query} onChange={(e) => { setQuery(e.target.value); setPage(1); }} placeholder="Search name, email, role" />
        <Dropdown
          button={
            <Button variant="secondary" className="w-full lg:w-auto">
              Status: {statusFilter}
            </Button>
          }
          items={['All', 'Active', 'Pending', 'Suspended'].map((status) => ({ label: status, onClick: () => { setStatusFilter(status); setPage(1); } }))}
        />
      </div>

      <div className="overflow-hidden rounded-[28px] border border-slate-200 bg-white shadow-xl dark:border-slate-800 dark:bg-slate-950">
        <table className="min-w-full divide-y divide-slate-200 dark:divide-slate-800">
          <thead className="bg-slate-50 dark:bg-slate-900">
            <tr>
              {['Name', 'Email', 'Role', 'Status', 'Last Login', 'Actions'].map((head) => (
                <th key={head} className="px-5 py-4 text-left text-xs font-bold uppercase tracking-[0.18em] text-slate-500 dark:text-slate-400">
                  <button
                    className="inline-flex items-center gap-2"
                    onClick={() => head !== 'Actions' && setSortKey(head === 'Last Login' ? 'lastLogin' : head.toLowerCase())}
                  >
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
                <td className="px-5 py-4 font-semibold text-slate-900 dark:text-white">{row.name}</td>
                <td className="px-5 py-4 text-slate-600 dark:text-slate-300">{row.email}</td>
                <td className="px-5 py-4"><span className="rounded-full bg-primary/10 px-3 py-1 text-xs font-bold text-primary">{row.role}</span></td>
                <td className="px-5 py-4">
                  <span className={`rounded-full px-3 py-1 text-xs font-bold ${row.status === 'Active' ? 'bg-emerald-500/10 text-emerald-600' : row.status === 'Suspended' ? 'bg-rose-500/10 text-rose-600' : 'bg-amber-500/10 text-amber-600'}`}>
                    {row.status}
                  </span>
                </td>
                <td className="px-5 py-4 text-slate-600 dark:text-slate-300">{row.lastLogin}</td>
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
        <p className="text-sm text-slate-500 dark:text-slate-400">Showing {pageRows.length} of {filtered.length} users</p>
        <div className="flex gap-2">
          <Button variant="secondary" size="sm" disabled={page === 1} onClick={() => setPage((p) => Math.max(1, p - 1))}>Prev</Button>
          <Button variant="secondary" size="sm" disabled={page === totalPages} onClick={() => setPage((p) => Math.min(totalPages, p + 1))}>Next</Button>
        </div>
      </div>

      <Modal open={!!selected} onClose={() => setSelected(null)} title="Edit User">
        {selected && (
          <div className="space-y-4">
            <Input label="Name" defaultValue={selected.name} />
            <Input label="Email" defaultValue={selected.email} />
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

export default UserTable;
