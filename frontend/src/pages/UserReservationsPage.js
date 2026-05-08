import React, { useEffect, useState } from 'react';
import { getMyReservations, cancelReservation } from '../lib/api';
import { useAuth } from '../context/AuthContext';
import { useTranslation } from '../context/LanguageContext';
import { useToast } from '../context/ToastContext';
import { XMarkIcon, BellIcon } from '@heroicons/react/24/outline';

const UserReservationsPage = () => {
  const { token } = useAuth();
  const { t } = useTranslation();
  const toast = useToast();
  const [reservations, setReservations] = useState([]);
  const [loading, setLoading] = useState(false);

  const loadReservations = async () => {
    setLoading(true);
    try {
      const data = await getMyReservations(token);
      setReservations(data || []);
    } catch (err) {
      console.error('Failed to load reservations:', err);
      toast?.addToast({ type: 'error', title: t('common.error'), message: err.message });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (token) loadReservations();
  }, [token]);

  const handleCancel = async (id) => {
    if (!window.confirm(t('reservationTable.confirmCancel'))) return;
    try {
      await cancelReservation(token, id);
      toast?.addToast({ type: 'success', title: t('common.success'), message: t('reservationTable.cancelled') });
      loadReservations();
    } catch (err) {
      toast?.addToast({ type: 'error', title: t('common.error'), message: err.message });
    }
  };

  const statusColors = {
    ACTIVE: 'bg-teal-100 text-teal-700 dark:bg-teal-500/15 dark:text-teal-200',
    FULFILLED: 'bg-emerald-100 text-emerald-700 dark:bg-emerald-500/15 dark:text-emerald-200',
    CANCELLED: 'bg-slate-200 text-slate-700 dark:bg-slate-700 dark:text-slate-200',
    EXPIRED: 'bg-rose-100 text-rose-700 dark:bg-rose-500/15 dark:text-rose-200',
  };

  return (
    <div className="space-y-6">
      <div className="rounded-[2rem] border border-white/70 bg-white/85 p-6 page-fade dark:border-slate-800 dark:bg-slate-900/75">
        <h1 className="text-3xl font-black text-slate-950 dark:text-white">{t('myReservationsPage.title')}</h1>
        <p className="mt-2 text-sm text-slate-600 dark:text-slate-300">{t('myReservationsPage.description')}</p>
      </div>

      {loading ? (
        <div className="text-center py-12 text-slate-500">{t('common.loading')}</div>
      ) : reservations.length === 0 ? (
        <div className="rounded-3xl border border-slate-200 bg-white p-12 text-center dark:border-slate-800 dark:bg-slate-950">
          <BellIcon className="mx-auto h-12 w-12 text-slate-300" />
          <p className="mt-4 text-slate-500">{t('myReservationsPage.noReservations')}</p>
        </div>
      ) : (
        <div className="grid gap-4">
          {reservations.map((r) => (
            <div key={r.id} className="rounded-3xl border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-950">
              <div className="flex items-start justify-between">
                <div className="space-y-1">
                  <h3 className="font-bold text-slate-900 dark:text-white">{r.bookTitle || 'N/A'}</h3>
                  <span className={`inline-block rounded-full px-3 py-1 text-xs font-bold ${statusColors[r.status] || statusColors.ACTIVE}`}>
                    {t(`status.${r.status?.toLowerCase()}`)}
                  </span>
                  {r.status === 'ACTIVE' && (
                    <p className="text-xs text-teal-600">
                      {t('myReservationsPage.waitingForBook')}
                    </p>
                  )}
                  {r.status === 'FULFILLED' && (
                    <p className="text-xs text-emerald-600">
                      {t('myReservationsPage.borrowRequestCreated')}
                    </p>
                  )}
                  <p className="text-xs text-slate-500">{t('reservationTable.reservedAt')}: {r.reservedAt ? new Date(r.reservedAt).toLocaleDateString() : 'N/A'}</p>
                </div>
                <div className="flex gap-2">
                  {r.status === 'ACTIVE' && (
                    <button
                      onClick={() => handleCancel(r.id)}
                      className="flex items-center gap-1 rounded-xl bg-slate-100 px-4 py-2 text-sm font-bold text-slate-700 hover:bg-slate-200 transition dark:bg-slate-800 dark:text-slate-200"
                    >
                      <XMarkIcon className="h-4 w-4" />
                      {t('reservationTable.cancel')}
                    </button>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default UserReservationsPage;
