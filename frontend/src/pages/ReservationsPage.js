import React from 'react';
import ReservationTable from '../components/ReservationTable';
import { useTranslation } from '../context/LanguageContext';

const ReservationsPage = () => {
  const { t } = useTranslation();
  
  return (
    <div className="space-y-6">
      <div className="rounded-[2rem] border border-white/70 bg-white/85 p-6 page-fade dark:border-slate-800 dark:bg-slate-900/75">
        <h1 className="text-3xl font-black text-slate-950 dark:text-white">{t('reservationsPage.title')}</h1>
        <p className="mt-2 text-sm text-slate-600 dark:text-slate-300">{t('reservationsPage.description')}</p>
      </div>
      <ReservationTable />
    </div>
  );
};

export default ReservationsPage;
