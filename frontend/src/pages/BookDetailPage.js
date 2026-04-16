import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import Button from '../components/Button';
import BorrowConfirmModal from '../components/BorrowConfirmModal';
import { useAuth } from '../context/AuthContext';
import { createBorrow, fetchBookById } from '../lib/api';
import { useToast } from '../context/ToastContext';

const BookDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { isAuthenticated, token, user } = useAuth();
  const toast = useToast();

  const [book, setBook] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [borrowing, setBorrowing] = useState(false);
  const [favorite, setFavorite] = useState(false);
  const [showModal, setShowModal] = useState(false);

  useEffect(() => {
    let mounted = true;

    const load = async () => {
      setLoading(true);
      setError('');
      try {
        const data = await fetchBookById(id);
        if (mounted) setBook(data);
      } catch (err) {
        if (mounted) setError(err.message || 'Failed to load book');
      } finally {
        if (mounted) setLoading(false);
      }
    };

    if (id) load();

    return () => {
      mounted = false;
    };
  }, [id]);

  useEffect(() => {
    const stored = JSON.parse(localStorage.getItem('favoriteBookIds') || '[]');
    setFavorite(stored.includes(id));
  }, [id]);

  const role = (user?.role || 'GUEST').toUpperCase();

  const handleBorrow = () => {
    if (!isAuthenticated || !token || !book?.id) {
      navigate('/login');
      return;
    }
    setShowModal(true);
  };

  const confirmBorrow = async () => {
    setBorrowing(true);
    try {
      await createBorrow(token, book.id);
      setShowModal(false);
      toast?.addToast({ type: 'success', title: 'Borrow requested', message: 'Your request was sent successfully.' });
    } catch (err) {
      toast?.addToast({ type: 'error', title: 'Borrow failed', message: err.message || 'Please try again.' });
    } finally {
      setBorrowing(false);
    }
  };

  const toggleFavorite = () => {
    const stored = JSON.parse(localStorage.getItem('favoriteBookIds') || '[]');
    const next = favorite ? stored.filter((item) => item !== id) : [...new Set([...stored, id])];
    localStorage.setItem('favoriteBookIds', JSON.stringify(next));
    setFavorite(!favorite);
    toast?.addToast({
      type: 'success',
      title: favorite ? 'Removed from favorites' : 'Added to favorites',
      message: book?.title || 'Book updated',
    });
  };

  if (loading) {
    return <div className="rounded-3xl bg-slate-100 p-8 text-sm dark:bg-slate-800">Loading book details...</div>;
  }

  if (error || !book) {
    return <div className="rounded-3xl bg-rose-50 p-8 text-sm text-rose-700 dark:bg-rose-500/10 dark:text-rose-200">{error || 'Book not found'}</div>;
  }

  const isAvailable = book.status === 'AVAILABLE' && book.availableQty > 0;

  return (
    <>
      <BorrowConfirmModal
        isOpen={showModal}
        onClose={() => setShowModal(false)}
        onConfirm={confirmBorrow}
        book={book}
        loading={borrowing}
      />
    <div className="grid gap-6 lg:grid-cols-[0.4fr_0.6fr] page-fade">
      <div className="rounded-[2rem] border border-white/70 bg-white/85 p-6 dark:border-slate-800 dark:bg-slate-900/75">
        <div className="flex h-[28rem] items-end rounded-[1.5rem] bg-[linear-gradient(145deg,#0f172a,#334155_55%,#fb923c_140%)] text-white overflow-hidden relative">
          {book.coverImageUrl ? (
            <img 
              src={book.coverImageUrl} 
              alt={book.title} 
              className="absolute inset-0 w-full h-full object-cover transition-transform duration-500 hover:scale-105"
            />
          ) : (
            <div className="p-6">
              <p className="text-xs uppercase tracking-[0.25em] text-white/70">{book.category || 'General'}</p>
              <p className="mt-2 text-3xl font-black">{book.title?.slice(0, 1) || 'B'}</p>
            </div>
          )}
        </div>
      </div>

      <div className="rounded-[2rem] border border-white/70 bg-white/85 p-6 dark:border-slate-800 dark:bg-slate-900/75">
        <h1 className="text-3xl font-black text-slate-950 dark:text-white">{book.title}</h1>
        <p className="mt-2 text-base text-slate-600 dark:text-slate-300">{book.author}</p>

        <div className="mt-5 grid gap-3 sm:grid-cols-2">
          <div className="rounded-2xl bg-slate-100 p-4 text-sm dark:bg-slate-800">
            <p className="text-slate-500 dark:text-slate-400">Category</p>
            <p className="mt-1 font-semibold">{book.category || 'Uncategorized'}</p>
          </div>
          <div className="rounded-2xl bg-slate-100 p-4 text-sm dark:bg-slate-800">
            <p className="text-slate-500 dark:text-slate-400">Status</p>
            <p className="mt-1 font-semibold">{book.status}</p>
          </div>
          <div className="rounded-2xl bg-slate-100 p-4 text-sm dark:bg-slate-800">
            <p className="text-slate-500 dark:text-slate-400">Available</p>
            <p className="mt-1 font-semibold">{book.availableQty}/{book.totalQuantity}</p>
          </div>
          <div className="rounded-2xl bg-slate-100 p-4 text-sm dark:bg-slate-800">
            <p className="text-slate-500 dark:text-slate-400">ISBN</p>
            <p className="mt-1 font-semibold">{book.isbn}</p>
          </div>
        </div>

        <div className="mt-6 rounded-2xl border border-slate-200 px-4 py-3 text-sm text-slate-600 dark:border-slate-800 dark:text-slate-300">
          Description API is not exposed in `BookResponse` yet. Add it in backend DTO if you want full detail text here.
        </div>

        <div className="mt-6 flex flex-wrap gap-3">
          {!isAuthenticated && <p className="text-sm text-slate-500 dark:text-slate-400">Guest users cannot borrow books.</p>}

          {role === 'USER' && (
            <>
              <Button onClick={handleBorrow} disabled={!isAvailable || borrowing}>
                {borrowing ? 'Submitting...' : isAvailable ? 'Borrow' : 'Unavailable'}
              </Button>
              <Button variant="secondary" onClick={toggleFavorite}>
                {favorite ? 'Unfavorite' : 'Favorite'}
              </Button>
            </>
          )}

          {role === 'LIBRARIAN' && <Button variant="secondary">Update status</Button>}
          {role === 'ADMIN' && <Button variant="secondary">Edit / Delete</Button>}

          <Button variant="ghost" onClick={() => navigate('/books')}>Back to books</Button>
        </div>
      </div>
    </div>
    </>
  );
};

export default BookDetailPage;
