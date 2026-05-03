import React, { useState } from 'react';
import { BellIcon, CheckCircleIcon, ExclamationCircleIcon, ClockIcon, XMarkIcon, TrashIcon } from '@heroicons/react/24/outline';
import { useAuth } from '../context/AuthContext';
import { useTranslation } from '../context/LanguageContext';
import { markAsRead, markAllAsRead, deleteNotification, deleteAllNotifications } from '../lib/api';
import NotificationDetail from './NotificationDetail';

const NotificationPanel = ({ notifications, unreadCount, onClose, onRefresh, loading, onNotificationSelect }) => {
  const { token } = useAuth();
  const { t } = useTranslation();

  const handleMarkAsRead = async (id) => {
    try {
      await markAsRead(token, id);
      onRefresh();
    } catch (error) {
      console.error('Failed to mark as read:', error);
    }
  };

  const handleMarkAllAsRead = async () => {
    try {
      await markAllAsRead(token);
      onRefresh();
    } catch (error) {
      console.error('Failed to mark all as read:', error);
    }
  };

  const handleDelete = async (id) => {
    try {
      await deleteNotification(token, id);
      onRefresh();
    } catch (error) {
      console.error('Failed to delete notification:', error);
    }
  };

  const handleDeleteAll = async () => {
    try {
      await deleteAllNotifications(token);
      onRefresh();
    } catch (error) {
      console.error('Failed to delete all notifications:', error);
    }
  };

  const handleNotificationClick = (notification) => {
    if (onNotificationSelect) {
      onNotificationSelect(notification);
    }
    if (!notification.read) {
      handleMarkAsRead(notification.id);
    }
  };

  const getNotificationIcon = (type) => {
    switch (type) {
      case 'DUE_SOON':
      case 'OVERDUE':
        return <ExclamationCircleIcon className="h-5 w-5 text-rose-500" />;
      case 'APPROVED':
        return <CheckCircleIcon className="h-5 w-5 text-emerald-500" />;
      case 'REJECTED':
        return <XMarkIcon className="h-5 w-5 text-rose-500" />;
      case 'AVAILABLE':
      case 'RESERVED':
        return <BellIcon className="h-5 w-5 text-blue-500" />;
      default:
        return <ClockIcon className="h-5 w-5 text-slate-500" />;
    }
  };

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now - date;
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);

    if (diffMins < 1) return t('notification.justNow');
    if (diffMins < 60) return `${diffMins} ${t('notification.minutesAgo')}`;
    if (diffHours < 24) return `${diffHours} ${t('notification.hoursAgo')}`;
    if (diffDays < 7) return `${diffDays} ${t('notification.daysAgo')}`;
    return date.toLocaleDateString();
  };

  return (
    <>
      <div className="absolute right-0 top-12 w-80 max-h-[480px] bg-white dark:bg-slate-900 rounded-xl shadow-xl border border-slate-200 dark:border-slate-800 overflow-hidden z-50">
        <div className="p-3 border-b border-slate-200 dark:border-slate-800 flex items-center justify-between">
          <div>
            <h3 className="font-semibold text-sm text-slate-900 dark:text-white">{t('notification.title')}</h3>
            {unreadCount > 0 && (
              <p className="text-xs text-slate-500 dark:text-slate-400">
                {unreadCount} {t('notification.unread')}
              </p>
            )}
          </div>
          <button onClick={onClose} className="p-1 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-full">
            <XMarkIcon className="h-4 w-4 text-slate-500" />
          </button>
        </div>

        {unreadCount > 0 && (
          <div className="px-3 py-2 bg-slate-50 dark:bg-slate-800 border-b border-slate-200 dark:border-slate-700">
            <button
              onClick={handleMarkAllAsRead}
              className="text-xs text-primary hover:underline"
            >
              {t('notification.markAllAsRead')}
            </button>
          </div>
        )}

        {notifications.length > 0 && (
          <div className="px-3 py-2 bg-slate-50 dark:bg-slate-800 border-b border-slate-200 dark:border-slate-700 flex justify-end">
            <button
              onClick={handleDeleteAll}
              className="text-xs text-rose-500 hover:text-rose-600 flex items-center gap-1"
            >
              <TrashIcon className="h-3 w-3" />
              Xóa tất cả
            </button>
          </div>
        )}

        <div className="overflow-y-auto max-h-[400px]">
          {loading ? (
            <div className="p-6 text-center text-slate-500 dark:text-slate-400">
              {t('notification.loading')}
            </div>
          ) : notifications.length === 0 ? (
            <div className="p-6 text-center text-slate-500 dark:text-slate-400">
              <BellIcon className="h-8 w-8 mx-auto mb-2 opacity-20" />
              <p className="text-sm">{t('notification.noNotifications')}</p>
            </div>
          ) : (
            notifications.map((notification) => (
              <div
                key={notification.id}
                onClick={() => handleNotificationClick(notification)}
                className={`px-3 py-2.5 border-b border-slate-100 dark:border-slate-800 cursor-pointer hover:bg-slate-50 dark:hover:bg-slate-800 transition-colors ${
                  !notification.read ? 'bg-blue-50/30 dark:bg-blue-900/5' : ''
                }`}
              >
                <div className="flex gap-2.5">
                  <div className="flex-shrink-0 mt-0.5">
                    {getNotificationIcon(notification.type)}
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-start justify-between gap-2">
                      <p className={`text-xs font-medium ${!notification.read ? 'text-slate-900 dark:text-white' : 'text-slate-600 dark:text-slate-300'}`}>
                        {notification.title}
                      </p>
                      {!notification.read && (
                        <span className="flex-shrink-0 w-1.5 h-1.5 bg-blue-500 rounded-full mt-1" />
                      )}
                    </div>
                    <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5 line-clamp-2">
                      {notification.content}
                    </p>
                    <p className="text-[10px] text-slate-400 dark:text-slate-500 mt-1">
                      {formatDate(notification.createdAt)}
                    </p>
                  </div>
                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      handleDelete(notification.id);
                    }}
                    className="flex-shrink-0 p-0.5 hover:bg-slate-200 dark:hover:bg-slate-700 rounded opacity-0 group-hover:opacity-100 transition-opacity"
                  >
                    <XMarkIcon className="h-3.5 w-3.5 text-slate-400" />
                  </button>
                </div>
              </div>
            ))
          )}
        </div>
      </div>
    </>
  );
};

export default NotificationPanel;
