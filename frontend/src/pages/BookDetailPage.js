import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import Button from '../components/Button';
import BorrowConfirmModal from '../components/BorrowConfirmModal';
import { useAuth } from '../context/AuthContext';
import { createBorrow, fetchBookById, fetchReviews, addReview, createReservation, getReservationCount, checkBorrowStatus } from '../lib/api';
import { useToast } from '../context/ToastContext';
import { useTranslation } from '../context/LanguageContext';
import {
  ArrowLeftIcon,
  BookOpenIcon,
  HeartIcon,
  ShareIcon,
  StarIcon,
  CalendarIcon,
  IdentificationIcon,
  TagIcon,
  UserIcon,
  CheckCircleIcon,
  XCircleIcon,
  ClockIcon,
  ChatBubbleBottomCenterTextIcon,
  PaperAirplaneIcon,
  BellIcon,
} from '@heroicons/react/24/outline';
import { HeartIcon as HeartSolid, StarIcon as StarSolid } from '@heroicons/react/24/solid';

/* â”€â”€ Palette of gradient covers â”€â”€ */
const COVER_GRADIENTS = [
  'from-teal-600 via-cyan-500 to-sky-600',
  'from-emerald-500 via-teal-500 to-cyan-500',
  'from-cyan-500 via-teal-500 to-emerald-600',
  'from-teal-700 via-slate-700 to-cyan-600',
  'from-sky-600 via-cyan-500 to-teal-500',
  'from-emerald-600 via-cyan-500 to-teal-700',
];

function pickGradient(title = '') {
  const idx = title.charCodeAt(0) % COVER_GRADIENTS.length;
  return COVER_GRADIENTS[idx];
}

const Skeleton = ({ className = '' }) => (
  <div className={`animate-pulse rounded-xl bg-slate-200 dark:bg-slate-800 ${className}`} />
);

const AvailBadge = ({ available, availableLabel, unavailableLabel }) => (
  <span className={`inline-flex items-center gap-1.5 rounded-full px-3 py-1 text-xs font-bold ring-1 ${
    available 
    ? 'bg-emerald-500/15 text-emerald-600 ring-emerald-500/30 dark:text-emerald-400' 
    : 'bg-rose-500/15 text-rose-600 ring-rose-500/30 dark:text-rose-400'
  }`}>
    {available ? <CheckCircleIcon className="h-4 w-4" /> : <XCircleIcon className="h-4 w-4" />}
    {available ? availableLabel : unavailableLabel}
  </span>
);

const StarRating = ({ rating = 0, count = 0, size = "sm", noRatingsLabel = 'No ratings', reviewsLabel = 'reviews' }) => {
  const full = Math.round(rating);
  const iconSize = size === "lg" ? "h-6 w-6" : "h-4 w-4";
  return (
    <div className="flex items-center gap-2">
      <div className="flex gap-0.5">
        {[1, 2, 3, 4, 5].map((i) =>
          i <= full ? (
            <StarSolid key={i} className={`${iconSize} text-amber-400`} />
          ) : (
            <StarIcon key={i} className={`${iconSize} text-slate-300 dark:text-slate-600`} />
          )
        )}
      </div>
      <span className={`${size === "lg" ? "text-xl" : "text-sm"} font-semibold text-slate-700 dark:text-slate-300`}>
        {rating > 0 ? rating.toFixed(1) : noRatingsLabel}
      </span>
      {count > 0 && (
        <span className="text-xs text-slate-400 dark:text-slate-500">({count} {reviewsLabel})</span>
      )}
    </div>
  );
};

const StatCard = ({ icon: Icon, label, value, highlight }) => (
  <div className={`flex flex-col gap-1 rounded-2xl p-4 transition-all ${
    highlight ? 'bg-primary/10 ring-1 ring-primary/30' : 'bg-slate-50 dark:bg-slate-800/60'
  }`}>
    <div className="flex items-center gap-1.5 text-xs font-semibold uppercase tracking-wider text-slate-400 dark:text-slate-500">
      <Icon className="h-3.5 w-3.5" />
      {label}
    </div>
    <p className={`text-lg font-black ${highlight ? 'text-primary' : 'text-slate-900 dark:text-white'}`}>
      {value}
    </p>
  </div>
);

const ReviewItem = ({ review, noRatingsLabel, reviewsLabel }) => (
  <div className="rounded-2xl border border-slate-100 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900/50">
    <div className="flex items-center justify-between">
      <div className="flex items-center gap-3">
        <div className="flex h-10 w-10 items-center justify-center rounded-full bg-slate-100 font-bold text-primary dark:bg-slate-800">
          {review.reviewerName?.charAt(0) || 'U'}
        </div>
        <div>
          <p className="text-sm font-bold text-slate-900 dark:text-white">{review.reviewerName}</p>
          <p className="text-xs text-slate-500 dark:text-slate-400">{new Date(review.createdAt).toLocaleDateString()}</p>
        </div>
      </div>
      <StarRating rating={review.rating} noRatingsLabel={noRatingsLabel} reviewsLabel={reviewsLabel} />
    </div>
    {review.title && <h4 className="mt-3 font-bold text-slate-900 dark:text-white">{review.title}</h4>}
    <p className="mt-2 text-sm text-slate-600 dark:text-slate-300">{review.content}</p>
  </div>
);

const BookDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { isAuthenticated, token, user } = useAuth();
  const toast = useToast();
  const { t } = useTranslation();

  const [book, setBook] = useState(null);
  const [reviews, setReviews] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [borrowing, setBorrowing] = useState(false);
  const [favorite, setFavorite] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const [imgError, setImgError] = useState(false);
  const [reservationCount, setReservationCount] = useState(0);
  const [reserving, setReserving] = useState(false);
  const [userBorrowStatus, setUserBorrowStatus] = useState(null); // null = not borrowed, object = borrowed

  // Review Form state
  const [newRating, setNewRating] = useState(5);
  const [reviewTitle, setReviewTitle] = useState('');
  const [reviewContent, setReviewContent] = useState('');
  const [submittingReview, setSubmittingReview] = useState(false);

  const loadData = async () => {
    setLoading(true);
    setError('');
    try {
      const [bookData, reviewData] = await Promise.all([
        fetchBookById(id),
        fetchReviews(id)
      ]);
      setBook(bookData);
      setReviews(reviewData.content || []);
      
      // Load reservation count and borrow status if authenticated
      if (isAuthenticated && token) {
        try {
          const [count, borrowStatus] = await Promise.all([
            getReservationCount(token, id),
            checkBorrowStatus(token, id)
          ]);
          setReservationCount(count);
          setUserBorrowStatus(borrowStatus || null);
        } catch (err) {
          console.error('Failed to load reservation count or borrow status:', err);
        }
      }
    } catch (err) {
      setError(err.message || t('bookDetail.failedLoadBook'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (id) loadData();
  }, [id]);

  useEffect(() => {
    const stored = JSON.parse(localStorage.getItem('favoriteBookIds') || '[]');
    setFavorite(stored.includes(id));
  }, [id]);

  const role = (user?.role || 'GUEST').toUpperCase();
  const isAvailable = book?.status === 'AVAILABLE' && (book?.availableQty ?? 0) > 0;
  const isAlreadyBorrowed = userBorrowStatus !== null;

  const handleBorrow = () => {
    if (!isAuthenticated || !token) { navigate('/login'); return; }
    setShowModal(true);
  };

  const confirmBorrow = async () => {
    setBorrowing(true);
    try {
      await createBorrow(token, book.id);
      setShowModal(false);
      toast?.addToast({ type: 'success', title: t('bookDetail.bookReserved'), message: t('bookDetail.bookReservedMessage') });
      loadData(); // Reload to update availability
    } catch (err) {
      toast?.addToast({ type: 'error', title: t('bookDetail.borrowFailed'), message: err.message });
    } finally {
      setBorrowing(false);
    }
  };

  const handleReserve = async () => {
    if (!isAuthenticated || !token) { navigate('/login'); return; }
    setReserving(true);
    try {
      await createReservation(token, { bookId: book.id, priority: 1 });
      toast?.addToast({ type: 'success', title: t('bookDetail.reserved'), message: t('bookDetail.reservedMessage') });
      setReservationCount(prev => prev + 1);
    } catch (err) {
      toast?.addToast({ type: 'error', title: t('bookDetail.reservationFailed'), message: err.message });
    } finally {
      setReserving(false);
    }
  };

  const toggleFavorite = () => {
    const stored = JSON.parse(localStorage.getItem('favoriteBookIds') || '[]');
    const next = favorite ? stored.filter((x) => x !== id) : [...new Set([...stored, id])];
    localStorage.setItem('favoriteBookIds', JSON.stringify(next));
    setFavorite(!favorite);
    toast?.addToast({ type: 'success', title: favorite ? t('bookDetail.removedFromFavorites') : t('bookDetail.addedToFavorites') });
  };

  const submitReview = async (e) => {
    e.preventDefault();
    if (!token) return;
    setSubmittingReview(true);
    try {
      await addReview(token, id, {
        rating: newRating,
        title: reviewTitle,
        content: reviewContent
      });
      toast?.addToast({ type: 'success', title: t('bookDetail.reviewSubmitted'), message: t('bookDetail.reviewSubmittedMessage') });
      setReviewTitle('');
      setReviewContent('');
      loadData(); // Reload to show new review and update average rating
    } catch (err) {
      toast?.addToast({ type: 'error', title: t('bookDetail.failedToSubmit'), message: err.message });
    } finally {
      setSubmittingReview(false);
    }
  };

  if (loading) return (
    <div className="space-y-6 animate-pulse">
      <div className="grid gap-6 lg:grid-cols-[0.38fr_0.62fr]">
        <Skeleton className="h-[480px] rounded-[2rem]" />
        <div className="space-y-4">
          <Skeleton className="h-10 w-3/4" />
          <Skeleton className="h-6 w-1/2" />
          <div className="grid grid-cols-2 gap-3 mt-6">
            {[1,2,3,4].map(i => <Skeleton key={i} className="h-20" />)}
          </div>
        </div>
      </div>
    </div>
  );

  if (error || !book) return (
    <div className="flex flex-col items-center gap-4 rounded-3xl bg-rose-50 p-12 text-center dark:bg-rose-900/10">
      <XCircleIcon className="h-16 w-16 text-rose-400" />
      <p className="text-lg font-bold text-rose-700 dark:text-rose-400">{error || t('bookDetail.bookNotFound')}</p>
      <Button variant="secondary" onClick={() => navigate('/books')}>{t('bookDetail.backToBooks')}</Button>
    </div>
  );

  const gradient = pickGradient(book.title);
  const showCover = book.coverImageUrl && !imgError;
  const categoryTags = book.categories?.map(c => typeof c === 'string' ? c : c.name) || [book.category].filter(Boolean);

  return (
    <>
      <BorrowConfirmModal isOpen={showModal} onClose={() => setShowModal(false)} onConfirm={confirmBorrow} book={book} loading={borrowing} />
      
      <button onClick={() => navigate('/books')} className="mb-4 inline-flex items-center gap-1.5 text-sm font-semibold text-slate-500 hover:text-primary dark:text-slate-400">
        <ArrowLeftIcon className="h-4 w-4" /> {t('bookDetail.backToBooks')}
      </button>

      <div className="grid gap-8 lg:grid-cols-[0.38fr_0.62fr]">
        {/* LEFT: Cover & Quick Actions */}
        <div className="flex flex-col gap-4">
          <div className="relative overflow-hidden rounded-[2rem] shadow-2xl">
            {showCover ? (
              <img src={book.coverImageUrl} alt={book.title} onError={() => setImgError(true)} className="h-[440px] w-full object-cover transition-transform duration-700 hover:scale-105" />
            ) : (
              <div className={`flex h-[440px] w-full flex-col items-center justify-center bg-gradient-to-br ${gradient} p-8 text-white`}>
                <BookOpenIcon className="mb-4 h-16 w-16 opacity-40" />
                <p className="text-center text-3xl font-black">{book.title}</p>
              </div>
            )}
            <div className="absolute left-4 top-4">
              <AvailBadge
                available={isAvailable}
                availableLabel={t('bookDetail.available')}
                unavailableLabel={t('bookDetail.unavailable')}
              />
            </div>
          </div>

          <div className="flex gap-3">
            <button onClick={toggleFavorite} className={`flex flex-1 items-center justify-center gap-2 rounded-2xl border py-3 font-bold transition-all ${favorite ? 'border-rose-200 bg-rose-50 text-rose-600' : 'bg-white text-slate-600'}`}>
              {favorite ? <HeartSolid className="h-5 w-5" /> : <HeartIcon className="h-5 w-5" />} {favorite ? t('bookDetail.saved') : t('bookDetail.save')}
            </button>
            <button onClick={() => { navigator.clipboard.writeText(window.location.href); toast.addToast({type:'success', title:t('bookDetail.linkCopied')}); }} className="flex flex-1 items-center justify-center gap-2 rounded-2xl border bg-white py-3 text-slate-600"><ShareIcon className="h-5 w-5" /> {t('bookDetail.share')}</button>
          </div>
        </div>

        {/* RIGHT: Detail Info */}
        <div className="flex flex-col gap-6 rounded-[2.5rem] bg-white/50 p-8 dark:bg-slate-900/50">
          <div className="space-y-4">
            <div className="flex flex-wrap gap-2">
              {categoryTags.map(cat => <span key={cat} className="rounded-full bg-primary/10 px-3 py-1 text-xs font-bold text-primary"># {cat}</span>)}
            </div>
            <h1 className="text-4xl font-black text-slate-900 dark:text-white">{book.title}</h1>
            <div className="flex items-center gap-2 text-slate-500 font-medium"><UserIcon className="h-5 w-5" /> {book.author}</div>
            <StarRating
              rating={book.averageRating || 0}
              count={book.ratingCount || 0}
              size="lg"
              noRatingsLabel={t('bookDetail.noRatings')}
              reviewsLabel={t('bookDetail.reviews')}
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <StatCard icon={BookOpenIcon} label={t('bookDetail.quantity')} value={`${book.availableQty} / ${book.totalQuantity}`} highlight={isAvailable} />
            <StatCard icon={CheckCircleIcon} label={t('common.status')} value={book.status} />
            <StatCard icon={CalendarIcon} label={t('bookDetail.year')} value={book.publishedYear || 'N/A'} />
            <StatCard icon={IdentificationIcon} label={t('bookDetail.isbn')} value={book.isbn} />
          </div>

          {book.description && (
            <div className="rounded-3xl bg-slate-50 p-6 dark:bg-slate-800/40">
              <h3 className="text-sm font-bold uppercase tracking-wider text-slate-400 mb-2">{t('bookDetail.description')}</h3>
              <p className="text-slate-600 dark:text-slate-300 leading-relaxed">{book.description}</p>
            </div>
          )}

          <div className="mt-auto">
            {role === 'USER' && (
              <>
                {isAlreadyBorrowed ? (
                  <div className="space-y-2">
                    <div className="w-full rounded-2xl py-4 text-lg font-black bg-emerald-500 text-white shadow-xl shadow-emerald-500/20 flex items-center justify-center gap-2">
                      <CheckCircleIcon className="h-6 w-6" />
                      {userBorrowStatus?.borrowStatus === 'PENDING_APPROVAL' 
                        ? t('bookDetail.pendingApproval') 
                        : t('bookDetail.alreadyBorrowed')}
                    </div>
                    <p className="text-center text-sm text-slate-500 dark:text-slate-400">
                      {userBorrowStatus?.borrowStatus === 'PENDING_APPROVAL'
                        ? t('bookDetail.pendingApprovalMessage')
                        : t('bookDetail.alreadyBorrowedMessage')}
                    </p>
                  </div>
                ) : isAvailable ? (
                  <button onClick={handleBorrow} disabled={borrowing} className={`w-full rounded-2xl py-4 text-lg font-black transition-all bg-primary text-white shadow-xl shadow-primary/20 hover:scale-[1.02] ${borrowing ? 'opacity-50' : ''}`}>
                    {borrowing ? t('bookDetail.processing') : t('bookDetail.borrowNow')}
                  </button>
                ) : (
                  <div className="space-y-3">
                    <button onClick={handleReserve} disabled={reserving} className={`w-full rounded-2xl py-4 text-lg font-black transition-all bg-amber-500 text-white shadow-xl shadow-amber-500/20 hover:scale-[1.02] ${reserving ? 'opacity-50' : ''}`}>
                      {reserving ? t('bookDetail.processing') : <><BellIcon className="h-5 w-5 inline mr-2" /> {t('bookDetail.reserveBook')}</>}
                    </button>
                    {reservationCount > 0 && (
                      <p className="text-center text-sm text-slate-500 dark:text-slate-400">
                        {t('bookDetail.peopleInQueue', { count: reservationCount })}
                      </p>
                    )}
                  </div>
                )}
              </>
            )}
          </div>
        </div>
      </div>

      {/* REVIEWS SECTION */}
      <div className="mt-12 grid gap-8 lg:grid-cols-2">
        {/* Write a Review */}
        <div className="rounded-[2.5rem] bg-slate-50 p-8 dark:bg-slate-900/30">
          <div className="mb-6 flex items-center gap-3">
            <ChatBubbleBottomCenterTextIcon className="h-8 w-8 text-primary" />
            <h2 className="text-2xl font-black text-slate-900 dark:text-white">{t('bookDetail.writeReview')}</h2>
          </div>
          
          {isAuthenticated ? (
            <form onSubmit={submitReview} className="space-y-4">
              <div>
                <label className="text-sm font-bold text-slate-500">{t('bookDetail.howRate')}</label>
                <div className="mt-2 flex gap-2">
                  {[1, 2, 3, 4, 5].map((s) => (
                    <button key={s} type="button" onClick={() => setNewRating(s)} className="transition hover:scale-110">
                      {s <= newRating ? <StarSolid className="h-8 w-8 text-amber-400" /> : <StarIcon className="h-8 w-8 text-slate-300" />}
                    </button>
                  ))}
                </div>
              </div>
              <div className="space-y-1">
                <input value={reviewTitle} onChange={e => setReviewTitle(e.target.value)} placeholder={t('bookDetail.reviewTitlePlaceholder')} className="w-full rounded-xl border-none bg-white p-3 text-sm shadow-sm dark:bg-slate-800" />
              </div>
              <div className="space-y-1">
                <textarea value={reviewContent} onChange={e => setReviewContent(e.target.value)} rows="4" placeholder={t('bookDetail.reviewContentPlaceholder')} required className="w-full rounded-2xl border-none bg-white p-4 text-sm shadow-sm dark:bg-slate-800" />
              </div>
              <button type="submit" disabled={submittingReview} className="flex items-center justify-center gap-2 rounded-xl bg-slate-900 px-6 py-3 font-bold text-white transition hover:bg-slate-800 disabled:opacity-50 dark:bg-primary">
                {submittingReview ? t('bookDetail.posting') : <><PaperAirplaneIcon className="h-4 w-4" /> {t('bookDetail.postReview')}</>}
              </button>
            </form>
          ) : (
            <div className="flex flex-col items-center justify-center h-48 rounded-2xl border-2 border-dashed border-slate-200 text-slate-400">
              <p>{t('bookDetail.loginToReview')}</p>
              <Button size="sm" variant="secondary" onClick={() => navigate('/login')} className="mt-4">{t('auth.login')}</Button>
            </div>
          )}
        </div>

        {/* Recent Reviews */}
        <div className="space-y-6">
          <h2 className="text-2xl font-black text-slate-900 dark:text-white">{t('bookDetail.recentReviews')}</h2>
          {reviews.length === 0 ? (
            <div className="flex flex-col items-center justify-center h-48 text-slate-400">
              <ChatBubbleBottomCenterTextIcon className="h-12 w-12 opacity-20" />
              <p className="mt-2">{t('bookDetail.noReviewsYet')}</p>
            </div>
          ) : (
            <div className="space-y-4 max-h-[500px] overflow-y-auto pr-2 custom-scrollbar">
              {reviews.map(r => (
                <ReviewItem
                  key={r.id}
                  review={r}
                  noRatingsLabel={t('bookDetail.noRatings')}
                  reviewsLabel={t('bookDetail.reviews')}
                />
              ))}
            </div>
          )}
        </div>
      </div>
    </>
  );
};

export default BookDetailPage;
