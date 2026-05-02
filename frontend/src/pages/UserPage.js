import React from 'react';
import UserTable from '../components/UserTable';
import { useTranslation } from '../context/LanguageContext';

const UserPage = () => {
  const { t } = useTranslation();
  
  return (
    <div className="space-y-6">
      <div className="rounded-[2rem] border border-white/70 bg-white/85 p-6 page-fade dark:border-slate-800 dark:bg-slate-900/75">
        <p className="text-sm font-semibold uppercase tracking-[0.2em] text-slate-500 dark:text-slate-400">{t('sidebar.userManagement')}</p>
        <h2 className="mt-2 text-3xl font-black text-slate-900 dark:text-white">{t('userPage.description')}</h2>
      </div>
      <UserTable />
    </div>
  );
};

export default UserPage;
