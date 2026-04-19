import React from 'react';
import Button from './Button';
import {
  BookOpenIcon,
  CalendarIcon,
  ClockIcon,
  ArrowPathIcon,
  XMarkIcon,
} from '@heroicons/react/24/outline';

const BorrowConfirmModal = ({ isOpen, onClose, onConfirm, book, loading }) => {
  if (!isOpen || !book) return null;

  const loanDays = 14;
  const borrowDate = new Date();
  const dueDate = new Date();
  dueDate.setDate(dueDate.getDate() + loanDays);

  const fmt = (d) =>
    d.toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' });

  return (
    /* Backdrop */
    <div
      className="fixed inset-0 z-50 flex items-end justify-center bg-black/60 backdrop-blur-sm sm:items-center"
      onClick={(e) => { if (e.target === e.currentTarget && !loading) onClose(); }}
    >
      {/* Modal card */}
      <div className="w-full max-w-md overflow-hidden rounded-t-[2rem] bg-white shadow-2xl dark:bg-slate-900 sm:rounded-[2rem]">

        {/* Header */}
        <div className="relative flex items-center justify-between bg-gradient-to-r from-primary to-indigo-600 px-6 py-5 text-white">
          <div className="flex items-center gap-3">
            <div className="rounded-xl bg-white/20 p-2">
              <BookOpenIcon className="h-6 w-6" />
            </div>
            <div>
              <p className="text-xs font-semibold uppercase tracking-widest opacity-75">Confirm Request</p>
              <h2 className="text-lg font-black">Borrow this book</h2>
            </div>
          </div>
          {!loading && (
            <button
              onClick={onClose}
              className="rounded-xl p-2 transition hover:bg-white/20"
            >
              <XMarkIcon className="h-5 w-5" />
            </button>
          )}
        </div>

        {/* Book info */}
        <div className="px-6 pt-5">
          <div className="flex gap-4 rounded-2xl border border-slate-100 bg-slate-50 p-4 dark:border-slate-800 dark:bg-slate-800/60">
            {/* Mini cover */}
            <div className="flex h-16 w-12 shrink-0 items-center justify-center rounded-xl bg-gradient-to-br from-primary to-indigo-600 text-white">
              <BookOpenIcon className="h-6 w-6 opacity-80" />
            </div>
            <div className="min-w-0">
              <p className="truncate font-bold text-slate-900 dark:text-white">{book.title}</p>
              <p className="mt-0.5 truncate text-sm text-slate-500 dark:text-slate-400">{book.author}</p>
              {book.isbn && (
                <p className="mt-1 text-xs text-slate-400 dark:text-slate-500">ISBN: {book.isbn}</p>
              )}
            </div>
          </div>
        </div>

        {/* Loan details */}
        <div className="grid grid-cols-3 gap-3 px-6 pt-4">
          <div className="flex flex-col items-center gap-1.5 rounded-2xl bg-slate-50 p-3 text-center dark:bg-slate-800/60">
            <CalendarIcon className="h-5 w-5 text-slate-400" />
            <p className="text-xs text-slate-400 dark:text-slate-500">Borrow</p>
            <p className="text-xs font-bold text-slate-800 dark:text-slate-200">{fmt(borrowDate)}</p>
          </div>
          <div className="flex flex-col items-center gap-1.5 rounded-2xl bg-orange-50 p-3 text-center dark:bg-orange-900/20">
            <ClockIcon className="h-5 w-5 text-orange-500" />
            <p className="text-xs text-orange-400">Due</p>
            <p className="text-xs font-bold text-orange-600 dark:text-orange-400">{fmt(dueDate)}</p>
          </div>
          <div className="flex flex-col items-center gap-1.5 rounded-2xl bg-blue-50 p-3 text-center dark:bg-blue-900/20">
            <ArrowPathIcon className="h-5 w-5 text-blue-500" />
            <p className="text-xs text-blue-400">Extensions</p>
            <p className="text-xs font-bold text-blue-600 dark:text-blue-400">Up to 2</p>
          </div>
        </div>

        {/* Notice */}
        <div className="mx-6 mt-4 rounded-2xl border border-amber-200 bg-amber-50/70 p-3 dark:border-amber-800/30 dark:bg-amber-900/10">
          <p className="text-xs leading-relaxed text-amber-700 dark:text-amber-400">
            ⏳ Your request will be reviewed by a librarian before the book is confirmed.
            Late returns may incur overdue fines.
          </p>
        </div>

        {/* Actions */}
        <div className="flex gap-3 p-6">
          <Button
            variant="secondary"
            onClick={onClose}
            disabled={loading}
            className="flex-1"
          >
            Cancel
          </Button>
          <button
            onClick={onConfirm}
            disabled={loading}
            className="group relative flex flex-1 items-center justify-center gap-2 overflow-hidden rounded-2xl bg-primary px-5 py-3 font-bold text-white shadow-md shadow-primary/30 transition-all duration-300 hover:-translate-y-0.5 hover:shadow-lg hover:shadow-primary/40 disabled:cursor-not-allowed disabled:opacity-60"
          >
            <span className="absolute inset-0 -translate-x-full bg-gradient-to-r from-transparent via-white/20 to-transparent transition-transform duration-700 group-hover:translate-x-full" />
            <BookOpenIcon className="relative h-5 w-5" />
            <span className="relative">{loading ? 'Submitting...' : 'Confirm Borrow'}</span>
          </button>
        </div>
      </div>
    </div>
  );
};

export default BorrowConfirmModal;
