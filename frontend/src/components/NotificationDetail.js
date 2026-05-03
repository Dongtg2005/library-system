import React from 'react';
import { CheckCircleIcon, ExclamationCircleIcon, BellIcon, XMarkIcon } from '@heroicons/react/24/outline';
import { useAuth } from '../context/AuthContext';
import { useTranslation } from '../context/LanguageContext';
import { useNavigate } from 'react-router-dom';

const NotificationDetail = ({ notification, onClose }) => {
  const { t } = useTranslation();
  const navigate = useNavigate();

  const getNotificationIcon = (type) => {
    switch (type) {
      case 'DUE_SOON':
      case 'OVERDUE':
        return <ExclamationCircleIcon className="h-12 w-12 text-rose-500" />;
      case 'APPROVED':
        return <CheckCircleIcon className="h-12 w-12 text-emerald-500" />;
      case 'REJECTED':
        return <XMarkIcon className="h-12 w-12 text-rose-500" />;
      case 'AVAILABLE':
      case 'RESERVED':
        return <BellIcon className="h-12 w-12 text-blue-500" />;
      case 'NEW_BORROW_REQUEST':
        return <BellIcon className="h-12 w-12 text-amber-500" />;
      case 'OVERDUE_REMINDER':
        return <ExclamationCircleIcon className="h-12 w-12 text-orange-500" />;
      case 'BOOK_RETURNED':
        return <CheckCircleIcon className="h-12 w-12 text-green-500" />;
      case 'SYSTEM_ALERT':
        return <ExclamationCircleIcon className="h-12 w-12 text-red-500" />;
      case 'REPORT_GENERATED':
        return <BellIcon className="h-12 w-12 text-purple-500" />;
      default:
        return <BellIcon className="h-12 w-12 text-slate-500" />;
    }
  };

  const handleNavigate = () => {
    try {
      const type = notification.type;

      // Librarian notifications
      if (type === 'NEW_BORROW_REQUEST') {
        navigate('/admin/borrow'); // Navigate to borrow management page
      } else if (type === 'OVERDUE_REMINDER') {
        navigate('/admin/borrow'); // Navigate to borrow management page
      } else if (type === 'BOOK_RETURNED') {
        navigate('/history'); // Navigate to borrow history
      }
      // User notifications
      else if (type === 'AVAILABLE') {
        if (notification.resourceType === 'BOOK' && notification.resourceId) {
          navigate(`/books/${notification.resourceId}`);
        }
      } else if (type === 'APPROVED' || type === 'REJECTED') {
        navigate('/my-books'); // Navigate to my borrows page
      } else if (type === 'DUE_SOON' || type === 'OVERDUE') {
        navigate('/my-books'); // Navigate to my borrows page
      } else if (type === 'RESERVED') {
        navigate('/admin/reservations'); // Navigate to reservations page
      }
      // Admin notifications
      else if (type === 'REPORT_GENERATED') {
        navigate('/dashboard'); // Navigate to dashboard
      } else if (type === 'SYSTEM_ALERT') {
        navigate('/dashboard'); // Navigate to dashboard
      }
      // Fallback for other types
      else if (notification.resourceType === 'BOOK' && notification.resourceId) {
        navigate(`/books/${notification.resourceId}`);
      } else if (notification.resourceType === 'RESERVATION') {
        navigate('/admin/reservations');
      } else if (notification.resourceType === 'FINE') {
        navigate('/profile');
      }
    } catch (error) {
      console.error('Navigation failed:', error);
    }
    onClose();
  };

  return (
    <div
      className="fixed inset-0 bg-black/50 flex items-center justify-center z-[9999] p-4"
      onClick={onClose}
      style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, zIndex: 9999 }}
    >
      <div
        className="bg-white dark:bg-slate-900 rounded-2xl shadow-2xl w-full max-w-md overflow-hidden relative"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="p-6">
          <div className="flex items-center justify-between mb-4">
            <h3 className="font-bold text-lg text-slate-900 dark:text-white">
              {t('notification.title')}
            </h3>
            <button
              onClick={onClose}
              className="p-2 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-full transition-colors"
            >
              <XMarkIcon className="h-5 w-5 text-slate-500" />
            </button>
          </div>

          <div className="flex gap-4 mb-6">
            <div className="flex-shrink-0">
              {getNotificationIcon(notification.type)}
            </div>
            <div className="flex-1">
              <h4 className="font-semibold text-base text-slate-900 dark:text-white mb-2">
                {notification.title}
              </h4>
              <p className="text-sm text-slate-600 dark:text-slate-300 mb-3 leading-relaxed">
                {notification.content}
              </p>
              <p className="text-xs text-slate-400 dark:text-slate-500">
                {new Date(notification.createdAt).toLocaleString()}
              </p>
            </div>
          </div>

          {notification.resourceId && (
            <button
              onClick={handleNavigate}
              className="w-full py-3 px-4 bg-primary text-white rounded-xl font-medium hover:bg-primary/90 transition-colors"
            >
              {t('notification.viewDetails')}
            </button>
          )}
        </div>
      </div>
    </div>
  );
};

export default NotificationDetail;
