import { PencilSquareIcon, TrashIcon, BellIcon, CheckIcon, XMarkIcon as CloseIcon } from '@heroicons/react/24/outline';
import React, { useEffect, useState } from 'react';
import Button from './Button';
import { getAllReservations, cancelReservation, fulfillReservation } from '../lib/api';
import { useAuth } from '../context/AuthContext';
import { useTranslation } from '../context/LanguageContext';

const ReservationTable = () => {
  const { token } = useAuth();
  const { t } = useTranslation();
  const [reservations, setReservations] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [statusFilter, setStatusFilter] = useState('');

  const statusColors = {
    ACTIVE: 'bg-teal-100 text-teal-700 dark:bg-teal-500/15 dark:text-teal-200',
    FULFILLED: 'bg-emerald-100 text-emerald-700 dark:bg-emerald-500/15 dark:text-emerald-200',
    CANCELLED: 'bg-slate-200 text-slate-700 dark:bg-slate-700 dark:text-slate-200',
    EXPIRED: 'bg-rose-100 text-rose-700 dark:bg-rose-500/15 dark:text-rose-200',
  };

  const priorityColors = {
    1: 'bg-slate-100 text-slate-600 dark:bg-slate-800 dark:text-slate-300',
    2: 'bg-amber-100 text-amber-700 dark:bg-amber-500/15 dark:text-amber-200',
    3: 'bg-rose-100 text-rose-700 dark:bg-rose-500/15 dark:text-rose-200',
  };

  const loadReservations = async () => {
    setLoading(true);
    setError('');
    try {
      const params = statusFilter ? { status: statusFilter } : {};
      const data = await getAllReservations(token, params);
      setReservations(data.content || []);
    } catch (err) {
      setError(err.message || t('reservationTable.failedToLoad'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadReservations();
  }, [statusFilter, token]);

  const handleCancel = async (reservationId) => {
    if (!window.confirm(t('reservationTable.confirmCancel'))) return;
    try {
      await cancelReservation(token, reservationId);
      loadReservations();
    } catch (err) {
      setError(err.message);
    }
  };

  const handleFulfill = async (reservationId) => {
    if (!window.confirm(t('reservationTable.confirmFulfill'))) return;
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
        <h2 className="text-2xl font-black text-slate-900 dark:text-white">{t('reservationTable.bookReservations')}</h2>
        <select
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
          className="rounded-xl border border-slate-200 bg-white px-4 py-2 text-sm dark:border-slate-700 dark:bg-slate-950"
        >
          <option value="">{t('reservationTable.allStatuses')}</option>
          <option value="ACTIVE">{t('reservationTable.active')}</option>
          <option value="FULFILLED">{t('reservationTable.fulfilled')}</option>
          <option value="CANCELLED">{t('reservationTable.cancelled')}</option>
          <option value="EXPIRED">{t('reservationTable.expired')}</option>
        </select>
      </div>

      {error && <p className="text-sm text-rose-600">{error}</p>}

      <div className="overflow-hidden rounded-[28px] border border-slate-200 bg-white shadow-xl dark:border-slate-800 dark:bg-slate-950">
        <div className="overflow-x-auto">
        <table className="min-w-[860px] w-full divide-y divide-slate-200 dark:divide-slate-800">
          <thead className="bg-slate-50 dark:bg-slate-900">
            <tr>
              <th className="px-5 py-4 text-left text-xs font-bold uppercase tracking-[0.18em] text-slate-500 dark:text-slate-400">{t('reservationTable.book')}</th>
              <th className="px-5 py-4 text-left text-xs font-bold uppercase tracking-[0.18em] text-slate-500 dark:text-slate-400">{t('reservationTable.user')}</th>
              <th className="px-5 py-4 text-left text-xs font-bold uppercase tracking-[0.18em] text-slate-500 dark:text-slate-400">{t('reservationTable.priority')}</th>
              <th className="px-5 py-4 text-left text-xs font-bold uppercase tracking-[0.18em] text-slate-500 dark:text-slate-400">{t('reservationTable.status')}</th>
              <th className="px-5 py-4 text-left text-xs font-bold uppercase tracking-[0.18em] text-slate-500 dark:text-slate-400">{t('reservationTable.reservedAt')}</th>
              <th className="px-5 py-4 text-left text-xs font-bold uppercase tracking-[0.18em] text-slate-500 dark:text-slate-400">{t('reservationTable.expiresAt')}</th>
              <th className="px-5 py-4 text-left text-xs font-bold uppercase tracking-[0.18em] text-slate-500 dark:text-slate-400">{t('reservationTable.actions')}</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
            {loading ? (
              <tr>
                <td colSpan={7} className="px-5 py-6 text-sm text-slate-500">
                  {t('reservationTable.loading')}
                </td>
              </tr>
            ) : reservations.length === 0 ? (
              <tr>
                <td colSpan={7} className="px-5 py-6 text-sm text-slate-500 text-center">
                  {t('reservationTable.noReservations')}
                </td>
              </tr>
            ) : (
              reservations.map((reservation) => (
                <tr key={reservation.id} className="transition hover:bg-slate-50/70 dark:hover:bg-slate-900/50">
                  <td className="px-5 py-4 font-semibold text-slate-900 dark:text-white">{reservation.bookTitle || 'N/A'}</td>
                  <td className="px-5 py-4 text-slate-600 dark:text-slate-300">{reservation.userName || 'N/A'}</td>
                  <td className="px-5 py-4">
                    <span className={`rounded-full px-3 py-1 text-xs font-bold ${priorityColors[reservation.priority] || priorityColors[1]}`}>
                      {t(`reservationTable.${reservation.priority === 1 ? 'normal' : reservation.priority === 2 ? 'high' : 'urgent'}`)}
                    </span>
                  </td>
                  <td className="px-5 py-4">
                    <span className={`rounded-full px-3 py-1 text-xs font-bold ${statusColors[reservation.status] || statusColors.ACTIVE}`}>
                      {t(`status.${reservation.status.toLowerCase()}`)}
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
                            <CheckIcon className="h-4 w-4" /> {t('reservationTable.fulfill')}
                          </Button>
                          <Button variant="ghost" size="sm" className="text-rose-500 hover:bg-rose-500/10" onClick={() => handleCancel(reservation.id)}>
                            <CloseIcon className="h-4 w-4" /> {t('reservationTable.cancel')}
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
    </div>
  );
};

export default ReservationTable;
