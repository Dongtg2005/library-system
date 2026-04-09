import React from 'react';
import BookTable from '../components/BookTable';

const BookPage = () => {
  return (
    <div className="space-y-6">
      <div>
        <p className="text-sm font-semibold uppercase tracking-[0.2em] text-slate-500 dark:text-slate-400">Book Service</p>
        <h2 className="mt-2 text-3xl font-black text-slate-900 dark:text-white">Books, authors, categories & Redis caching</h2>
      </div>
      <BookTable />
    </div>
  );
};

export default BookPage;
