import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import Button from '../components/Button';
import { useAuth } from '../context/AuthContext';
import { createBorrow, fetchBookById, checkBorrowStatus } from '../lib/api';
import { useToast } from '../context/ToastContext';
import { useTranslation } from '../context/LanguageContext';

const BorrowBookPage = () => {
  const { bookId } = useParams();
  const navigate = useNavigate();
  const { token } = useAuth();
  const toast = useToast();
  const { t } = useTranslation();

  const [book, setBook] = useState(null);
  const [notes, setNotes] = useState('');
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [borrowStatus, setBorrowStatus] = useState(null);
  const [checkingStatus, setCheckingStatus] = useState(false);

  useEffect(() => {
    let mounted = true;

    const load = async () => {
      setLoading(true);
      setCheckingStatus(true);
      try {
        const [data, status] = await Promise.all([
          fetchBookById(bookId),
          token ? checkBorrowStatus(token, bookId) : Promise.resolve(null)
        ]);
        if (mounted) {
          setBook(data);
          setBorrowStatus(status);
        }
      } catch {
        if (mounted) setBook(null);
      } finally {
        if (mounted) {
          setLoading(false);
          setCheckingStatus(false);
        }
      }
    };

    load();

    return () => {
      mounted = false;
    };
  }, [bookId]);

  const submitBorrow = async () => {
    if (!token || !book?.id) return;

    setSubmitting(true);
    try {
      await createBorrow(token, book.id, notes);
      toast?.addToast({ 
        type: 'success', 
        title: t('borrowConfirm.title'), 
        message: t('borrowBookPage.borrowSuccess') 
      });
      navigate('/my-books');
    } catch (error) {
      toast?.addToast({ 
        type: 'error', 
        title: t('borrowConfirm.borrowFailed'), 
        message: error.message || t('common.error') 
      });
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <div className="rounded-3xl bg-slate-100 p-8 text-sm dark:bg-slate-800">{t('common.loading')}</div>;

  if (!book) return <div className="rounded-3xl bg-rose-50 p-8 text-sm text-rose-700 dark:bg-rose-500/10 dark:text-rose-200">{t('bookDetail.bookNotFound')}</div>;

  const available = book.status === 'AVAILABLE' && book.availableQty > 0;
  const isAlreadyBorrowed = borrowStatus !== null;

  // Show already borrowed message
  if (isAlreadyBorrowed) {
    return (
      <div className="rounded-[2rem] border border-white/70 bg-white/85 p-6 page-fade dark:border-slate-800 dark:bg-slate-900/75">
        <h1 className="text-3xl font-black text-slate-950 dark:text-white">{t('borrowBookPage.title')}</h1>
        <p className="mt-2 text-sm text-slate-600 dark:text-slate-300">{book.title} - {book.author}</p>

        <div className="mt-6 rounded-2xl bg-emerald-50 p-6 dark:bg-emerald-900/20">
          <div className="flex items-center gap-3">
            <div className="flex h-12 w-12 items-center justify-center rounded-full bg-emerald-100 dark:bg-emerald-800">
              <svg className="h-6 w-6 text-emerald-600 dark:text-emerald-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
              </svg>
            </div>
            <div>
              <p className="font-bold text-emerald-700 dark:text-emerald-400">
                {borrowStatus?.borrowStatus === 'PENDING_APPROVAL' 
                  ? t('borrowBookPage.pendingApproval') 
                  : t('borrowBookPage.alreadyBorrowed')}
              </p>
              <p className="text-sm text-emerald-600 dark:text-emerald-300">
                {borrowStatus?.borrowStatus === 'PENDING_APPROVAL'
                  ? t('borrowBookPage.pendingApprovalMessage')
                  : t('borrowBookPage.alreadyBorrowedMessage')}
              </p>
            </div>
          </div>
        </div>

        <div className="mt-6 flex gap-3">
          <Button onClick={() => navigate('/my-books')}>
            {t('borrowBookPage.viewMyBooks')}
          </Button>
          <Button variant="ghost" onClick={() => navigate(`/books/${book.id}`)}>
            {t('common.back')}
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className="rounded-[2rem] border border-white/70 bg-white/85 p-6 page-fade dark:border-slate-800 dark:bg-slate-900/75">
      <h1 className="text-3xl font-black text-slate-950 dark:text-white">{t('borrowBookPage.title')}</h1>
      <p className="mt-2 text-sm text-slate-600 dark:text-slate-300">{book.title} - {book.author}</p>

      <div className="mt-6 rounded-2xl bg-slate-100 p-4 text-sm dark:bg-slate-800">
        <p className="text-sm font-medium text-slate-600 dark:text-slate-300">{t('common.status')}: {book.status} {t('bookDetail.available')}: {book.availableQty}/{book.totalQuantity}</p>
      </div>

      <label className="mt-6 block text-sm font-medium text-slate-600 dark:text-slate-300">
        {t('borrowConfirm.borrowDetails')}
        <textarea
          value={notes}
          onChange={(event) => setNotes(event.target.value)}
          rows={4}
          className="mt-2 w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm outline-none focus:border-primary dark:border-slate-700 dark:bg-slate-950"
          placeholder={t('borrowManagement.rejectReason')}
        />
      </label>

      <div className="mt-6 flex gap-3">
        <Button onClick={submitBorrow} disabled={!available || submitting}>
          {submitting ? t('borrowConfirm.borrowing') : t('borrowBookPage.confirmBorrow')}
        </Button>
        <Button variant="ghost" onClick={() => navigate(`/books/${book.id}`)}>
          {t('common.cancel')}
        </Button>
      </div>
    </div>
  );
};

export default BorrowBookPage;
