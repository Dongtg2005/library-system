import React from 'react';
import BookTable from '../components/BookTable';
import { useTranslation } from '../context/LanguageContext';

const BookPage = () => {
  const { t } = useTranslation();
  
  return (
    <div className="space-y-6">
      <div className="rounded-[2rem] border border-white/70 bg-white/85 p-6 page-fade dark:border-slate-800 dark:bg-slate-900/75">
        <p className="text-sm font-semibold uppercase tracking-[0.2em] text-slate-500 dark:text-slate-400">{t('sidebar.bookService')}</p>
        <h2 className="mt-2 text-3xl font-black text-slate-900 dark:text-white">{t('bookPage.description')}</h2>
      </div>
      <BookTable />
    </div>
  );
};

export default BookPage;
