import React, { useEffect, useMemo, useState } from 'react';
import { PencilSquareIcon, TrashIcon, MagnifyingGlassIcon, PlusIcon } from '@heroicons/react/24/outline';
import Button from './Button';
import Input from './Input';
import Modal from './Modal';
import Dropdown from './Dropdown';
import { useAuth } from '../context/AuthContext';
import { createUser, deleteUser, fetchUsers, updateUser } from '../lib/api';
import { useTranslation } from '../context/LanguageContext';

const pageSize = 6;

const UserTable = () => {
  const { token } = useAuth();
  const { t } = useTranslation();
  const [query, setQuery] = useState('');
  const [page, setPage] = useState(1);
  const [sortKey, setSortKey] = useState('fullName');
  const [statusFilter, setStatusFilter] = useState('All');
  const [users, setUsers] = useState([]);
  const [totalItems, setTotalItems] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [selected, setSelected] = useState(null);
  const [creating, setCreating] = useState(false);
  const [form, setForm] = useState({ fullName: '', email: '', role: 'USER', status: 'ACTIVE', password: '' });
  const [formError, setFormError] = useState('');

  const loadUsers = async () => {
    if (!token) return;

    setLoading(true);
    setError('');

    try {
      const payload = await fetchUsers(token, {
        page: Math.max(page - 1, 0),
        size: pageSize,
        query: query.trim() || undefined,
      });

      const content = Array.isArray(payload?.content) ? payload.content : [];
      const normalized = content
        .filter((row) => statusFilter === 'All' || String(row.status || '').toUpperCase() === statusFilter.toUpperCase())
        .map((row) => ({
          id: row.id,
          fullName: row.fullName || '-',
          email: row.email || '-',
          role: String(row.role || 'USER').toUpperCase(),
          status: String(row.status || 'ACTIVE').toUpperCase(),
          lastLogin: row.lastLoginAt ? new Date(row.lastLoginAt).toLocaleString() : '-',
        }));

      setUsers(normalized);
      setTotalItems(Number(payload?.totalElements || normalized.length));
    } catch (apiError) {
      setError(apiError.message || t('userTable.loadFailed'));
      setUsers([]);
      setTotalItems(0);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadUsers();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token, page, query, statusFilter]);

  const sortedRows = useMemo(() => {
    return [...users].sort((a, b) => String(a[sortKey] || '').localeCompare(String(b[sortKey] || '')));
  }, [users, sortKey]);

  const totalPages = Math.max(1, Math.ceil(totalItems / pageSize));

  const closeModal = () => {
    setSelected(null);
    setCreating(false);
    setForm({ fullName: '', email: '', role: 'USER', status: 'ACTIVE', password: '' });
    setFormError('');
  };

  const openCreate = () => {
    setCreating(true);
    setSelected(null);
    setForm({ fullName: '', email: '', role: 'USER', status: 'ACTIVE', password: '' });
    setFormError('');
  };

  const openEdit = (row) => {
    setSelected(row);
    setCreating(false);
    setForm({
      fullName: row.fullName,
      email: row.email,
      role: row.role,
      status: row.status,
      password: '',
    });
    setFormError('');
  };

  const handleSave = async () => {
    if (!form.fullName.trim() || !form.email.trim()) {
      setFormError(t('userTable.fullNameEmailRequired'));
      return;
    }

    if (creating && !form.password.trim()) {
      setFormError(t('userTable.passwordRequiredCreate'));
      return;
    }

    try {
      if (creating) {
        await createUser(token, {
          fullName: form.fullName.trim(),
          email: form.email.trim(),
          password: form.password,
          role: form.role,
          status: form.status,
        });
      } else if (selected?.id) {
        const payload = {
          fullName: form.fullName.trim(),
          email: form.email.trim(),
          role: form.role,
          status: form.status,
        };

        if (form.password.trim()) {
          payload.password = form.password;
        }

        await updateUser(token, selected.id, payload);
      }

      closeModal();
      loadUsers();
    } catch (saveError) {
      setFormError(saveError.message || t('userTable.saveFailed'));
    }
  };

  const handleDelete = async (id) => {
    try {
      await deleteUser(token, id);
      if (selected?.id === id) closeModal();
      loadUsers();
    } catch (deleteError) {
      setError(deleteError.message || t('userTable.deleteFailed'));
    }
  };

  return (
    <div className="space-y-5">
      <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
        <Input
          label={t('userTable.searchUsers')}
          value={query}
          onChange={(e) => {
            setQuery(e.target.value);
            setPage(1);
          }}
          placeholder={t('userTable.searchNameEmail')}
        />
        <div className="flex flex-wrap gap-3 lg:justify-end">
          <Dropdown
            button={
              <Button variant="secondary" className="w-full lg:w-auto">
                {t('userTable.status')}: {statusFilter}
              </Button>
            }
            items={['All', 'ACTIVE', 'INACTIVE', 'SUSPENDED'].map((status) => ({
              label: status,
              onClick: () => {
                setStatusFilter(status);
                setPage(1);
              },
            }))}
          />
          <Button className="w-full lg:w-auto" onClick={openCreate}>
            <PlusIcon className="h-4 w-4" />{t('userTable.addUserButton')}
          </Button>
        </div>
      </div>

      {error && <p className="text-sm text-amber-600">{error}</p>}

      <div className="overflow-hidden rounded-[28px] border border-slate-200 bg-white shadow-xl dark:border-slate-800 dark:bg-slate-950">
        <div className="overflow-x-auto">
        <table className="min-w-[720px] w-full divide-y divide-slate-200 dark:divide-slate-800">
          <thead className="bg-slate-50 dark:bg-slate-900">
            <tr>
              {[
                { label: t('userTable.fullNameHeader'), sort: 'fullName' },
                { label: t('userTable.emailHeader'), sort: 'email' },
                { label: t('userTable.roleHeader'), sort: 'role' },
                { label: t('userTable.statusHeader'), sort: 'status' },
                { label: t('userTable.lastLoginHeader'), sort: 'lastLogin' },
                { label: t('userTable.actionsHeader') },
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
                <td colSpan={6} className="px-5 py-6 text-sm text-slate-500">{t('userTable.loadingUsers')}</td>
              </tr>
            ) : (
              sortedRows.map((row) => (
                <tr key={row.id} className="transition hover:bg-slate-50/70 dark:hover:bg-slate-900/50">
                  <td className="px-5 py-4 font-semibold text-slate-900 dark:text-white">{row.fullName}</td>
                  <td className="px-5 py-4 text-slate-600 dark:text-slate-300">{row.email}</td>
                  <td className="px-5 py-4"><span className="rounded-full bg-primary/10 px-3 py-1 text-xs font-bold text-primary">{row.role}</span></td>
                  <td className="px-5 py-4">
                    <span className={`rounded-full px-3 py-1 text-xs font-bold ${row.status === 'ACTIVE' ? 'bg-emerald-500/10 text-emerald-600' : row.status === 'SUSPENDED' ? 'bg-rose-500/10 text-rose-600' : 'bg-amber-500/10 text-amber-600'}`}>
                      {row.status}
                    </span>
                  </td>
                  <td className="px-5 py-4 text-slate-600 dark:text-slate-300">{row.lastLogin}</td>
                  <td className="px-5 py-4">
                    <div className="flex items-center gap-2">
                      <Button variant="ghost" size="sm" onClick={() => openEdit(row)}><PencilSquareIcon className="h-4 w-4" />{t('userTable.editButton')}</Button>
                      <Button variant="ghost" size="sm" className="text-rose-500 hover:bg-rose-500/10" onClick={() => handleDelete(row.id)}><TrashIcon className="h-4 w-4" />{t('userTable.deleteButton')}</Button>
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
        <p className="text-sm text-slate-500 dark:text-slate-400">{t('userTable.showing', { count: sortedRows.length, total: totalItems })}</p>
        <div className="flex flex-wrap gap-2">
          <Button variant="secondary" size="sm" disabled={page === 1 || loading} onClick={() => setPage((p) => Math.max(1, p - 1))}>{t('bookTable.prev')}</Button>
          <Button variant="secondary" size="sm" disabled={page === totalPages || loading} onClick={() => setPage((p) => Math.min(totalPages, p + 1))}>{t('bookTable.next')}</Button>
        </div>
      </div>

      <Modal open={creating || !!selected} onClose={closeModal} title={creating ? t('userTable.addUserTitle') : t('userTable.editUserTitle')}>
        <div className="space-y-4">
          <Input label={t('auth.fullName')} value={form.fullName} onChange={(e) => setForm((prev) => ({ ...prev, fullName: e.target.value }))} />
          <Input label={t('auth.email')} value={form.email} onChange={(e) => setForm((prev) => ({ ...prev, email: e.target.value }))} />
          <Input
            label={creating ? t('auth.password') : t('userTable.passwordOptional')}
            type="password"
            value={form.password}
            onChange={(e) => setForm((prev) => ({ ...prev, password: e.target.value }))}
          />
          <div className="grid gap-4 md:grid-cols-2">
            <label className="block">
              <span className="mb-2 block text-sm font-medium text-slate-600 dark:text-slate-300">{t('auth.role')}</span>
              <select
                className="w-full rounded-2xl border border-slate-200 bg-white/80 px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-primary focus:ring-4 focus:ring-primary/10 dark:border-slate-700 dark:bg-slate-900/70 dark:text-white"
                value={form.role}
                onChange={(e) => setForm((prev) => ({ ...prev, role: e.target.value }))}
              >
                <option value="ADMIN">{t('role.admin')}</option>
                <option value="LIBRARIAN">{t('role.librarian')}</option>
                <option value="USER">{t('role.user')}</option>
              </select>
            </label>
            <label className="block">
              <span className="mb-2 block text-sm font-medium text-slate-600 dark:text-slate-300">{t('common.status')}</span>
              <select
                className="w-full rounded-2xl border border-slate-200 bg-white/80 px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-primary focus:ring-4 focus:ring-primary/10 dark:border-slate-700 dark:bg-slate-900/70 dark:text-white"
                value={form.status}
                onChange={(e) => setForm((prev) => ({ ...prev, status: e.target.value }))}
              >
                <option value="ACTIVE">{t('status.active')}</option>
                <option value="INACTIVE">{t('status.inactive')}</option>
                <option value="SUSPENDED">{t('status.suspended')}</option>
              </select>
            </label>
          </div>
          {formError && <p className="text-sm text-rose-500">{formError}</p>}
          <div className="flex justify-end gap-3">
            <Button variant="secondary" onClick={closeModal}>{t('common.cancel')}</Button>
            <Button onClick={handleSave}>{t('userTable.saveUser')}</Button>
          </div>
        </div>
      </Modal>
    </div>
  );
};

export default UserTable;
