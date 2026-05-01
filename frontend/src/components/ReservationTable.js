import { PencilSquareIcon, TrashIcon, BellIcon, CheckIcon, XMarkIcon as CloseIcon } from '@heroicons/react/24/outline';
import React, { useEffect, useState } from 'react';
import Button from './Button';
import { getAllReservations, cancelReservation, fulfillReservation } from '../lib/api';
import { useAuth } from '../context/AuthContext';

const statusColors = {
  ACTIVE: 'bg-teal-100 text-teal-700 dark:bg-teal-500/15 dark:text-teal-200',
  FULFILLED: 'bg-emerald-100 text-emerald-700 dark:bg-emerald-500/15 dark:text-emerald-200',
  CANCELLED: 'bg-slate-200 text-slate-700 dark:bg-slate-700 dark:text-slate-200',
  EXPIRED: 'bg-rose-100 text-rose-700 dark:bg-rose-500/15 dark:text-rose-200',
};

const priorityLabels = {
  1: 'Normal',
  2: 'High',
  3: 'Urgent',
};

const priorityColors = {
  1: 'bg-slate-100 text-slate-600 dark:bg-slate-800 dark:text-slate-300',
  2: 'bg-amber-100 text-amber-700 dark:bg-amber-500/15 dark:text-amber-200',
  3: 'bg-rose-100 text-rose-700 dark:bg-rose-500/15 dark:text-rose-200',
};

const ReservationTable = () => {
  const { token } = useAuth();
  const [reservations, setReservations] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [statusFilter, setStatusFilter] = useState('');

  const loadReservations = async () => {
    setLoading(true);
    setError('');
    try {
      const params = statusFilter ? { status: statusFilter } : {};
      const data = await getAllReservations(token, params);
      setReservations(data.content || []);
    } catch (err) {
      setError(err.message || 'Failed to load reservations');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadReservations();
  }, [statusFilter, token]);

  const handleCancel = async (reservationId) => {
    if (!window.confirm('Are you sure you want to cancel this reservation?')) return;
    try {
      await cancelReservation(token, reservationId);
      loadReservations();
    } catch (err) {
      setError(err.message);
    }
  };

  const handleFulfill = async (reservationId) => {
    if (!window.confirm('Mark this reservation as fulfilled?')) return;
    try {
      await fulfillReservation(token, reservationId);
      loadReservations();
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <h2 className="text-2xl font-black text-slate-900 dark:text-white">Book Reservations</h2>
        <select
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
          className="rounded-xl border border-slate-200 bg-white px-4 py-2 text-sm dark:border-slate-700 dark:bg-slate-950"
        >
          <option value="">All Statuses</option>
          <option value="ACTIVE">Active</option>
          <option value="FULFILLED">Fulfilled</option>
          <option value="CANCELLED">Cancelled</option>
          <option value="EXPIRED">Expired</option>
        </select>
      </div>

      {error && <p className="text-sm text-rose-600">{error}</p>}

      <div className="overflow-hidden rounded-[28px] border border-slate-200 bg-white shadow-xl dark:border-slate-800 dark:bg-slate-950">
        <table className="min-w-full divide-y divide-slate-200 dark:divide-slate-800">
          <thead className="bg-slate-50 dark:bg-slate-900">
            <tr>
              <th className="px-5 py-4 text-left text-xs font-bold uppercase tracking-[0.18em] text-slate-500 dark:text-slate-400">Book</th>
              <th className="px-5 py-4 text-left text-xs font-bold uppercase tracking-[0.18em] text-slate-500 dark:text-slate-400">User</th>
              <th className="px-5 py-4 text-left text-xs font-bold uppercase tracking-[0.18em] text-slate-500 dark:text-slate-400">Priority</th>
              <th className="px-5 py-4 text-left text-xs font-bold uppercase tracking-[0.18em] text-slate-500 dark:text-slate-400">Status</th>
              <th className="px-5 py-4 text-left text-xs font-bold uppercase tracking-[0.18em] text-slate-500 dark:text-slate-400">Reserved At</th>
              <th className="px-5 py-4 text-left text-xs font-bold uppercase tracking-[0.18em] text-slate-500 dark:text-slate-400">Expires At</th>
              <th className="px-5 py-4 text-left text-xs font-bold uppercase tracking-[0.18em] text-slate-500 dark:text-slate-400">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
            {loading ? (
              <tr>
                <td colSpan={7} className="px-5 py-6 text-sm text-slate-500">
                  Loading reservations...
                </td>
              </tr>
            ) : reservations.length === 0 ? (
              <tr>
                <td colSpan={7} className="px-5 py-6 text-sm text-slate-500 text-center">
                  No reservations found.
                </td>
              </tr>
            ) : (
              reservations.map((reservation) => (
                <tr key={reservation.id} className="transition hover:bg-slate-50/70 dark:hover:bg-slate-900/50">
                  <td className="px-5 py-4 font-semibold text-slate-900 dark:text-white">{reservation.bookTitle || 'N/A'}</td>
                  <td className="px-5 py-4 text-slate-600 dark:text-slate-300">{reservation.userName || 'N/A'}</td>
                  <td className="px-5 py-4">
                    <span className={`rounded-full px-3 py-1 text-xs font-bold ${priorityColors[reservation.priority] || priorityColors[1]}`}>
                      {priorityLabels[reservation.priority] || 'Normal'}
                    </span>
                  </td>
                  <td className="px-5 py-4">
                    <span className={`rounded-full px-3 py-1 text-xs font-bold ${statusColors[reservation.status] || statusColors.ACTIVE}`}>
                      {reservation.status}
                    </span>
                  </td>
                  <td className="px-5 py-4 text-sm text-slate-600 dark:text-slate-300">
                    {reservation.reservedAt ? new Date(reservation.reservedAt).toLocaleDateString() : 'N/A'}
                  </td>
                  <td className="px-5 py-4 text-sm text-slate-600 dark:text-slate-300">
                    {reservation.expiresAt ? new Date(reservation.expiresAt).toLocaleDateString() : 'N/A'}
                  </td>
                  <td className="px-5 py-4">
                    <div className="flex items-center gap-2">
                      {reservation.status === 'ACTIVE' && (
                        <>
                          <Button variant="ghost" size="sm" onClick={() => handleFulfill(reservation.id)}>
                            <CheckIcon className="h-4 w-4" /> Fulfill
                          </Button>
                          <Button variant="ghost" size="sm" className="text-rose-500 hover:bg-rose-500/10" onClick={() => handleCancel(reservation.id)}>
                            <CloseIcon className="h-4 w-4" /> Cancel
                          </Button>
                        </>
                      )}
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default ReservationTable;
