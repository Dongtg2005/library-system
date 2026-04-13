import React from 'react';
import Button from './Button';

const BorrowConfirmModal = ({ isOpen, onClose, onConfirm, book, loading }) => {
  if (!isOpen || !book) return null;

  const loanDays = 14; // TODO: Get from API policy
  const dueDate = new Date();
  dueDate.setDate(dueDate.getDate() + loanDays);

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
      <div className="w-full max-w-md rounded-2xl border border-white/70 bg-white/95 p-6 shadow-2xl dark:border-slate-700 dark:bg-slate-900/95">
        <h2 className="text-xl font-bold text-slate-900 dark:text-white">Confirm Borrow Request</h2>

        <div className="mt-4 space-y-3 rounded-xl bg-slate-50 p-4 dark:bg-slate-800/50">
          <div>
            <p className="text-xs text-slate-500 dark:text-slate-400">Book</p>
            <p className="font-semibold text-slate-900 dark:text-white">{book.title}</p>
            <p className="text-sm text-slate-600 dark:text-slate-300">{book.author}</p>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <p className="text-xs text-slate-500 dark:text-slate-400">Borrow Date</p>
              <p className="text-sm font-medium">{new Date().toLocaleDateString()}</p>
            </div>
            <div>
              <p className="text-xs text-slate-500 dark:text-slate-400">Due Date</p>
              <p className="text-sm font-medium text-orange-600 dark:text-orange-400">
                {dueDate.toLocaleDateString()}
              </p>
            </div>
          </div>

          <div>
            <p className="text-xs text-slate-500 dark:text-slate-400">Extensions</p>
            <p className="text-sm">Up to <span className="font-medium">2</span> extensions allowed</p>
          </div>

          <div className="rounded-lg bg-amber-50 p-3 text-xs text-amber-800 dark:bg-amber-900/20 dark:text-amber-200">
            <p>After confirmation, your request will be sent to a librarian for approval.</p>
          </div>
        </div>

        <div className="mt-6 flex gap-3">
          <Button variant="secondary" onClick={onClose} disabled={loading}>
            Cancel
          </Button>
          <Button onClick={onConfirm} disabled={loading}>
            {loading ? 'Submitting...' : 'Confirm Borrow'}
          </Button>
        </div>
      </div>
    </div>
  );
};

export default BorrowConfirmModal;
