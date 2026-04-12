import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import Button from '../components/Button';
import { useAuth } from '../context/AuthContext';
import { createBorrow, fetchBookById } from '../lib/api';
import { useToast } from '../context/ToastContext';

const BorrowBookPage = () => {
  const { bookId } = useParams();
  const navigate = useNavigate();
  const { token } = useAuth();
  const toast = useToast();

  const [book, setBook] = useState(null);
  const [notes, setNotes] = useState('');
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    let mounted = true;

    const load = async () => {
      setLoading(true);
      try {
        const data = await fetchBookById(bookId);
        if (mounted) setBook(data);
      } catch {
        if (mounted) setBook(null);
      } finally {
        if (mounted) setLoading(false);
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
      toast?.addToast({ type: 'success', title: 'Requested', message: 'Borrow request submitted.' });
      navigate('/my-books');
    } catch (error) {
      toast?.addToast({ type: 'error', title: 'Borrow failed', message: error.message || 'Please try again.' });
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <div className="rounded-3xl bg-slate-100 p-8 text-sm dark:bg-slate-800">Loading...</div>;

  if (!book) return <div className="rounded-3xl bg-rose-50 p-8 text-sm text-rose-700 dark:bg-rose-500/10 dark:text-rose-200">Book not found.</div>;

  const available = book.status === 'AVAILABLE' && book.availableQty > 0;

  return (
    <div className="rounded-[2rem] border border-white/70 bg-white/85 p-6 page-fade dark:border-slate-800 dark:bg-slate-900/75">
      <h1 className="text-3xl font-black text-slate-950 dark:text-white">Borrow Book</h1>
      <p className="mt-2 text-sm text-slate-600 dark:text-slate-300">{book.title} · {book.author}</p>

      <div className="mt-6 rounded-2xl bg-slate-100 p-4 text-sm dark:bg-slate-800">
        Status: {book.status} · Availability: {book.availableQty}/{book.totalQuantity}
      </div>

      <label className="mt-6 block text-sm font-medium text-slate-600 dark:text-slate-300">
        Notes
        <textarea
          value={notes}
          onChange={(event) => setNotes(event.target.value)}
          rows={4}
          className="mt-2 w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm outline-none focus:border-primary dark:border-slate-700 dark:bg-slate-950"
          placeholder="Optional notes for librarian"
        />
      </label>

      <div className="mt-6 flex gap-3">
        <Button onClick={submitBorrow} disabled={!available || submitting}>{submitting ? 'Submitting...' : 'Confirm Borrow'}</Button>
        <Button variant="ghost" onClick={() => navigate(`/books/${book.id}`)}>Cancel</Button>
      </div>
    </div>
  );
};

export default BorrowBookPage;
