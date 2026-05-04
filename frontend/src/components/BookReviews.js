import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useTranslation } from '../context/LanguageContext';
import { useToast } from '../context/ToastContext';
import Button from './Button';
import {
  fetchBookReviews,
  fetchBookRatingSummary,
  addReview as apiAddReview,
  updateReview as apiUpdateReview,
  deleteReview as apiDeleteReview,
  voteReview as apiVoteReview,
  fetchReviewComments,
  addComment as apiAddComment,
  likeComment as apiLikeComment,
} from '../lib/api';
import {
  ChatBubbleBottomCenterTextIcon,
  HandThumbUpIcon,
  HandThumbDownIcon,
  StarIcon,
} from '@heroicons/react/24/outline';
import { StarIcon as StarSolid, HandThumbUpIcon as HandThumbUpSolid, HandThumbDownIcon as HandThumbDownSolid } from '@heroicons/react/24/solid';

const StarRating = ({ rating, onRate, readonly = false, size = 'md' }) => {
  const { t } = useTranslation();
  const [hover, setHover] = useState(0);

  const sizeClasses = {
    sm: 'h-4 w-4',
    md: 'h-6 w-6',
    lg: 'h-8 w-8',
  };

  const stars = [1, 2, 3, 4, 5];

  return (
    <div className="flex items-center gap-0.5">
      {stars.map((star) => {
        const isFilled = star <= (hover || rating);
        return (
          <button
            key={star}
            type="button"
            disabled={readonly}
            className={`${sizeClasses[size]} ${readonly ? 'cursor-default' : 'cursor-pointer hover:scale-110'} transition-transform`}
            onClick={() => !readonly && onRate?.(star)}
            onMouseEnter={() => !readonly && setHover(star)}
            onMouseLeave={() => !readonly && setHover(0)}
          >
            {isFilled ? (
              <StarSolid className={`${sizeClasses[size]} text-amber-400`} />
            ) : (
              <StarIcon className={`${sizeClasses[size]} text-slate-300`} />
            )}
          </button>
        );
      })}
    </div>
  );
};

const RatingDistribution = ({ distribution, total }) => {
  const stars = [5, 4, 3, 2, 1];

  return (
    <div className="space-y-2">
      {stars.map((star) => {
        const count = distribution?.[star] || 0;
        const percentage = total > 0 ? (count / total) * 100 : 0;

        return (
          <div key={star} className="flex items-center gap-2">
            <span className="text-sm font-medium w-3 text-slate-700 dark:text-slate-300">{star}</span>
            <StarSolid className="h-4 w-4 text-amber-400" />
            <div className="flex-1 h-2 bg-slate-200 dark:bg-slate-700 rounded-full overflow-hidden">
              <div
                className="h-full bg-amber-400 rounded-full"
                style={{ width: `${percentage}%` }}
              />
            </div>
            <span className="text-sm text-slate-500 w-10 text-right">{count}</span>
          </div>
        );
      })}
    </div>
  );
};

const ReviewForm = ({ bookId, existingReview, onSubmit, onCancel }) => {
  const { t } = useTranslation();
  const { token } = useAuth();
  const [rating, setRating] = useState(existingReview?.rating || 0);
  const [title, setTitle] = useState(existingReview?.title || '');
  const [content, setContent] = useState(existingReview?.content || '');
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (rating === 0) return;

    setSubmitting(true);
    try {
      const payload = { rating, title, content };
      if (existingReview) {
        await apiUpdateReview(token, existingReview.id, payload);
      } else {
        await apiAddReview(token, bookId, payload);
      }
      onSubmit?.();
    } catch (err) {
      console.error('Failed to submit review:', err);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div>
        <label className="text-sm font-bold text-slate-500 dark:text-slate-400 mb-2 block">{t('reviews.yourRating')}</label>
        <StarRating rating={rating} onRate={setRating} size="lg" />
      </div>

      <div className="space-y-1">
        <input
          type="text"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          placeholder={t('reviews.reviewTitlePlaceholder')}
          className="w-full rounded-xl border-none bg-white p-3 text-sm shadow-sm dark:bg-slate-800 dark:text-white"
        />
      </div>

      <div className="space-y-1">
        <textarea
          value={content}
          onChange={(e) => setContent(e.target.value)}
          placeholder={t('reviews.reviewContentPlaceholder')}
          rows={4}
          className="w-full rounded-2xl border-none bg-white p-4 text-sm shadow-sm dark:bg-slate-800 dark:text-white"
        />
      </div>

      <div className="flex gap-2">
        <Button type="submit" disabled={rating === 0 || submitting} className="flex items-center justify-center gap-2 rounded-xl bg-slate-900 px-6 py-3 font-bold text-white transition hover:bg-slate-800 disabled:opacity-50 dark:bg-primary">
          {submitting ? t('common.loading') : existingReview ? t('reviews.updateReview') : t('reviews.submitReview')}
        </Button>
        {onCancel && (
          <Button type="button" variant="secondary" onClick={onCancel}>
            {t('reviews.cancel')}
          </Button>
        )}
      </div>
    </form>
  );
};

const CommentItem = ({ comment, reviewId, onReply, currentUser, onUpdate }) => {
  const { t } = useTranslation();
  const { token } = useAuth();
  const [liked, setLiked] = useState(false);

  const handleLike = async () => {
    if (!token) return;
    try {
      await apiLikeComment(token, comment.id);
      setLiked(true);
      onUpdate?.();
    } catch (err) {
      console.error('Failed to like comment:', err);
    }
  };

  const isOwner = currentUser?.id === comment.userId;

  return (
    <div className="py-3 border-b last:border-0">
      <div className="flex items-center gap-2 mb-1">
        <span className="font-semibold text-sm">{comment.commenterName}</span>
        <span className="text-xs text-gray-500">
          {new Date(comment.createdAt).toLocaleDateString()}
        </span>
      </div>
      <p className="text-sm text-gray-700 mb-2">{comment.content}</p>
      <div className="flex items-center gap-4">
        <button
          onClick={handleLike}
          disabled={!token || liked}
          className={`text-xs flex items-center gap-1 ${liked ? 'text-blue-600' : 'text-gray-500 hover:text-blue-600'}`}
        >
          <svg className="w-4 h-4" fill={liked ? 'currentColor' : 'none'} stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M14 10h4.764a2 2 0 011.789 2.894l-3.5 7A2 2 0 0115.263 21h-4.017c-.163 0-.326-.02-.485-.06L7 20m7-10V5a2 2 0 00-2-2h-.095c-.5 0-.905.405-.905.905 0 .714-.211 1.344-.627 1.882C11.348 6.627 10.828 7 10.276 7H7m7 0V5a2 2 0 00-2-2h-.095c-.5 0-.905.405-.905.905 0 .714-.211 1.344-.627 1.882C11.348 6.627 10.828 7 10.276 7H7m0 0v7" />
          </svg>
          {comment.likeCount || 0}
        </button>
        <button
          onClick={() => onReply?.(comment.id)}
          className="text-xs text-gray-500 hover:text-blue-600"
        >
          {t('reviews.reply')}
        </button>
      </div>

      {comment.replies?.length > 0 && (
        <div className="ml-4 mt-3 pl-4 border-l-2 border-gray-200">
          {comment.replies.map((reply) => (
            <CommentItem
              key={reply.id}
              comment={reply}
              reviewId={reviewId}
              currentUser={currentUser}
              onUpdate={onUpdate}
            />
          ))}
        </div>
      )}
    </div>
  );
};

const CommentForm = ({ reviewId, parentId, onSubmit, onCancel }) => {
  const { t } = useTranslation();
  const { token } = useAuth();
  const [content, setContent] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!content.trim()) return;

    setSubmitting(true);
    try {
      await apiAddComment(token, { reviewId, parentId, content });
      setContent('');
      onSubmit?.();
    } catch (err) {
      console.error('Failed to add comment:', err);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="flex gap-2 mt-3">
      <input
        type="text"
        value={content}
        onChange={(e) => setContent(e.target.value)}
        placeholder={t('reviews.writeComment')}
        className="flex-1 px-3 py-2 text-sm border rounded-lg focus:ring-2 focus:ring-blue-500"
      />
      <Button type="submit" size="sm" disabled={!content.trim() || submitting}>
        {submitting ? t('common.loading') : t('reviews.submitComment')}
      </Button>
      {onCancel && (
        <Button type="button" size="sm" variant="secondary" onClick={onCancel}>
          {t('reviews.cancel')}
        </Button>
      )}
    </form>
  );
};

const ReviewItem = ({ review, bookId, currentUser, onUpdate }) => {
  const { t } = useTranslation();
  const { token } = useAuth();
  const toast = useToast();
  const [showComments, setShowComments] = useState(false);
  const [comments, setComments] = useState([]);
  const [replyingTo, setReplyingTo] = useState(null);
  const [editing, setEditing] = useState(false);
  const [loadingComments, setLoadingComments] = useState(false);

  const isOwner = currentUser?.id === review.userId;
  
  // Local state for immediate UI feedback
  const [liked, setLiked] = useState(review.userVoteType === 'LIKE');
  const [disliked, setDisliked] = useState(review.userVoteType === 'DISLIKE');
  const [likeCount, setLikeCount] = useState(review.likeCount || 0);
  const [dislikeCount, setDislikeCount] = useState(review.dislikeCount || 0);

  const loadComments = async () => {
    if (!showComments) {
      setLoadingComments(true);
      try {
        const data = await fetchReviewComments(review.id);
        setComments(data);
      } catch (err) {
        console.error('Failed to load comments:', err);
      } finally {
        setLoadingComments(false);
      }
    }
    setShowComments(!showComments);
  };

  const handleVote = async (voteType) => {
    if (!token) {
      toast?.addToast({
        type: 'info',
        title: t('common.info'),
        message: t('reviews.loginRequired'),
      });
      return;
    }

    try {
      await apiVoteReview(token, { reviewId: review.id, voteType });
      
      // Immediate UI toggle feedback
      if (voteType === 'LIKE') {
        if (liked) {
          setLiked(false);
          setLikeCount(c => c - 1);
        } else {
          setLiked(true);
          setLikeCount(c => c + 1);
          if (disliked) {
            setDisliked(false);
            setDislikeCount(c => c - 1);
          }
        }
      } else {
        if (disliked) {
          setDisliked(false);
          setDislikeCount(c => c - 1);
        } else {
          setDisliked(true);
          setDislikeCount(c => c + 1);
          if (liked) {
            setLiked(false);
            setLikeCount(c => c - 1);
          }
        }
      }
      
      toast?.addToast({
        type: 'success',
        title: t('common.success'),
        message: t('reviews.voteSubmitted'),
      });
      onUpdate?.();
    } catch (err) {
      console.error('Failed to vote:', err);
    }
  };

  const handleDelete = async () => {
    if (!window.confirm(t('reviews.confirmDeleteReview'))) return;

    try {
      await apiDeleteReview(token, review.id);
      toast?.addToast({
        type: 'success',
        title: t('common.success'),
        message: t('reviews.reviewDeleted'),
      });
      onUpdate?.();
    } catch (err) {
      console.error('Failed to delete review:', err);
    }
  };

  if (editing) {
    return (
      <div className="rounded-2xl border border-slate-100 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900/50">
        <ReviewForm
          bookId={bookId}
          existingReview={review}
          onSubmit={() => {
            setEditing(false);
            onUpdate?.();
          }}
          onCancel={() => setEditing(false)}
        />
      </div>
    );
  }

  return (
    <div className="rounded-2xl border border-slate-100 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900/50">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-full bg-slate-100 font-bold text-primary dark:bg-slate-800">
            {review.reviewerName?.charAt(0).toUpperCase()}
          </div>
          <div>
            <p className="text-sm font-bold text-slate-900 dark:text-white">{review.reviewerName}</p>
            <p className="text-xs text-slate-500 dark:text-slate-400">{new Date(review.createdAt).toLocaleDateString()}</p>
          </div>
        </div>
        <div className="flex items-center gap-0.5">
          {[1, 2, 3, 4, 5].map((i) =>
            i <= review.rating ? (
              <StarSolid key={i} className="h-4 w-4 text-amber-400" />
            ) : (
              <StarIcon key={i} className="h-4 w-4 text-slate-300" />
            )
          )}
        </div>
      </div>

      {review.title && <h4 className="mt-3 font-bold text-slate-900 dark:text-white">{review.title}</h4>}
      <p className="mt-2 text-sm text-slate-600 dark:text-slate-300">{review.content}</p>

      <div className="mt-3 flex items-center gap-4">
        <button
          onClick={() => handleVote('LIKE')}
          className={`flex items-center gap-1 text-sm transition-colors ${liked ? 'text-blue-600' : 'text-slate-500 hover:text-blue-600'}`}
        >
          {liked ? <HandThumbUpSolid className="h-4 w-4" /> : <HandThumbUpIcon className="h-4 w-4" />}
          {t('reviews.helpful')} ({likeCount})
        </button>

        <button
          onClick={() => handleVote('DISLIKE')}
          className={`flex items-center gap-1 text-sm transition-colors ${disliked ? 'text-red-600' : 'text-slate-500 hover:text-red-600'}`}
        >
          {disliked ? <HandThumbDownSolid className="h-4 w-4" /> : <HandThumbDownIcon className="h-4 w-4" />}
          {t('reviews.notHelpful')} ({dislikeCount})
        </button>

        <button
          onClick={loadComments}
          className="flex items-center gap-1 text-sm text-slate-500 hover:text-blue-600"
        >
          <ChatBubbleBottomCenterTextIcon className="h-4 w-4" />
          {t('reviews.commentCount', { count: review.commentCount || 0 })}
        </button>

        {/* Reply button - available for all authenticated users */}
        {token && (
          <button
            onClick={async () => {
              if (!showComments) {
                setLoadingComments(true);
                try {
                  const data = await fetchReviewComments(review.id);
                  setComments(data);
                } catch (err) {
                  console.error('Failed to load comments:', err);
                } finally {
                  setLoadingComments(false);
                }
              }
              setShowComments(true);
            }}
            className="flex items-center gap-1 text-sm text-slate-500 hover:text-blue-600"
          >
            <ChatBubbleBottomCenterTextIcon className="h-4 w-4" />
            {t('reviews.reply')}
          </button>
        )}

        {isOwner && (
          <>
            <button
              onClick={() => setEditing(true)}
              className="text-sm text-slate-500 hover:text-blue-600"
            >
              {t('common.edit')}
            </button>
            <button
              onClick={handleDelete}
              className="text-sm text-slate-500 hover:text-red-600"
            >
              {t('common.delete')}
            </button>
          </>
        )}
      </div>

      {showComments && (
        <div className="mt-4 pl-4 border-l-2 border-slate-200 dark:border-slate-700">
          {loadingComments ? (
            <p className="text-sm text-slate-500">{t('common.loading')}</p>
          ) : (
            <>
              {comments.map((comment) => (
                <CommentItem
                  key={comment.id}
                  comment={comment}
                  reviewId={review.id}
                  currentUser={currentUser}
                  onUpdate={loadComments}
                  onReply={(id) => setReplyingTo(id)}
                />
              ))}

              {token && (
                <CommentForm
                  reviewId={review.id}
                  parentId={replyingTo}
                  onSubmit={() => {
                    setReplyingTo(null);
                    loadComments();
                  }}
                  onCancel={replyingTo ? () => setReplyingTo(null) : undefined}
                />
              )}
            </>
          )}
        </div>
      )}
    </div>
  );
};

const BookReviews = ({ bookId }) => {
  const { t } = useTranslation();
  const { isAuthenticated, token, user } = useAuth();
  const navigate = useNavigate();
  const toast = useToast();
  const [reviews, setReviews] = useState([]);
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [writing, setWriting] = useState(false);
  const [userReview, setUserReview] = useState(null);

  const loadReviews = async (pageNum = 0) => {
    try {
      const data = await fetchBookReviews(bookId, { page: pageNum, size: 5 });
      if (pageNum === 0) {
        setReviews(data.content);
      } else {
        setReviews((prev) => [...prev, ...data.content]);
      }
      setHasMore(data.content.length === 5 && data.hasNext);

      // Check if current user has already reviewed
      if (user) {
        const existing = data.content.find((r) => r.userId === user.id);
        setUserReview(existing);
      }
    } catch (err) {
      console.error('Failed to load reviews:', err);
    }
  };

  const loadSummary = async () => {
    try {
      const data = await fetchBookRatingSummary(bookId);
      setSummary(data);
    } catch (err) {
      console.error('Failed to load rating summary:', err);
    }
  };

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      await Promise.all([loadReviews(0), loadSummary()]);
      setLoading(false);
    };
    load();
  }, [bookId]);

  const handleLoadMore = () => {
    const nextPage = page + 1;
    setPage(nextPage);
    loadReviews(nextPage);
  };

  const handleReviewSubmitted = () => {
    setWriting(false);
    setPage(0);
    loadReviews(0);
    loadSummary();
  };

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center h-48 text-slate-400">
        <ChatBubbleBottomCenterTextIcon className="h-12 w-12 opacity-20" />
        <p className="mt-2">{t('reviews.loading')}</p>
      </div>
    );
  }

  return (
    <div className="grid gap-8 lg:grid-cols-2">
      {/* Write a Review */}
      <div className="rounded-[2.5rem] bg-slate-50 p-8 dark:bg-slate-900/30">
        <div className="mb-6 flex items-center gap-3">
          <ChatBubbleBottomCenterTextIcon className="h-8 w-8 text-primary" />
          <h2 className="text-2xl font-black text-slate-900 dark:text-white">{t('reviews.writeReview')}</h2>
        </div>
        
        {isAuthenticated ? (
          writing || userReview ? (
            <ReviewForm
              bookId={bookId}
              existingReview={userReview}
              onSubmit={handleReviewSubmitted}
              onCancel={userReview ? () => setWriting(false) : () => setWriting(false)}
            />
          ) : (
            <Button onClick={() => setWriting(true)} className="flex items-center justify-center gap-2 rounded-xl bg-slate-900 px-6 py-3 font-bold text-white transition hover:bg-slate-800 dark:bg-primary">
              {t('reviews.writeReview')}
            </Button>
          )
        ) : (
          <div className="flex flex-col items-center justify-center h-48 rounded-2xl border-2 border-dashed border-slate-200 text-slate-400">
            <p>{t('reviews.loginRequired')}</p>
            <Button size="sm" variant="secondary" onClick={() => navigate('/login')} className="mt-4">{t('auth.login')}</Button>
          </div>
        )}

        {/* Rating Summary */}
        {summary && (
          <div className="mt-8 pt-8 border-t border-slate-200 dark:border-slate-700">
            <h3 className="text-lg font-bold text-slate-900 dark:text-white mb-4">{t('reviews.averageRating')}</h3>
            <div className="flex flex-col gap-4">
              <div className="flex items-center gap-4">
                <div className="text-4xl font-black text-slate-900 dark:text-white">
                  {summary.averageRating?.toFixed(1) || '0.0'}
                </div>
                <div>
                  <StarRating rating={Math.round(summary.averageRating || 0)} readonly size="sm" />
                  <p className="text-sm text-slate-500 mt-1">
                    {t('reviews.basedOn', { count: summary.totalReviews || 0 })}
                  </p>
                </div>
              </div>
              <RatingDistribution distribution={summary.ratingDistribution} total={summary.totalReviews} />
            </div>
          </div>
        )}
      </div>

      {/* Recent Reviews */}
      <div className="space-y-6">
        <h2 className="text-2xl font-black text-slate-900 dark:text-white">{t('bookDetail.recentReviews')}</h2>
        {reviews.length === 0 ? (
          <div className="flex flex-col items-center justify-center h-48 text-slate-400">
            <ChatBubbleBottomCenterTextIcon className="h-12 w-12 opacity-20" />
            <p className="mt-2">{t('reviews.noReviews')}</p>
          </div>
        ) : (
          <div className="space-y-4 max-h-[500px] overflow-y-auto pr-2 custom-scrollbar">
            {reviews.map((review) => (
              <ReviewItem
                key={review.id}
                review={review}
                bookId={bookId}
                currentUser={user}
                onUpdate={handleReviewSubmitted}
              />
            ))}

            {hasMore && (
              <button
                onClick={handleLoadMore}
                className="w-full py-2 text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
              >
                {t('reviews.loadMore')}
              </button>
            )}
          </div>
        )}
      </div>
    </div>
  );
};

export default BookReviews;
