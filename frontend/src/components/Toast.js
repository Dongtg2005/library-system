import React from 'react';
import { CheckCircleIcon, ExclamationTriangleIcon, XCircleIcon, InformationCircleIcon, XMarkIcon } from '@heroicons/react/24/outline';
import { useToast } from '../context/ToastContext';
import { useTranslation } from '../context/LanguageContext';

const Toast = () => {
  const context = useToast();
  const { t } = useTranslation();

  const iconMap = {
    success: <CheckCircleIcon className="h-5 w-5" />,
    warning: <ExclamationTriangleIcon className="h-5 w-5" />,
    error: <XCircleIcon className="h-5 w-5" />,
    info: <InformationCircleIcon className="h-5 w-5" />,
  };

  const toneMap = {
    success: 'border-emerald-500/30 bg-emerald-500/10 text-emerald-700 dark:text-emerald-300',
    warning: 'border-amber-500/30 bg-amber-500/10 text-amber-700 dark:text-amber-300',
    error: 'border-rose-500/30 bg-rose-500/10 text-rose-700 dark:text-rose-300',
    info: 'border-sky-500/30 bg-sky-500/10 text-sky-700 dark:text-sky-300',
  };

  if (!context) return null;

  return (
    <div className="fixed right-4 top-4 z-[60] flex w-full max-w-sm flex-col gap-3 sm:right-6 sm:top-6">
      {context.toasts.map((toast) => (
        <div
          key={toast.id}
          className={`flex items-start gap-3 rounded-2xl border px-4 py-3 shadow-xl backdrop-blur ${toneMap[toast.type || 'info']}`}
        >
          <div className="mt-0.5">{iconMap[toast.type || 'info']}</div>
          <div className="flex-1">
            <p className="text-sm font-semibold">{toast.title}</p>
            {toast.message && <p className="mt-1 text-sm opacity-90">{toast.message}</p>}
          </div>
          <button 
            onClick={() => context.removeToast(toast.id)} 
            className="rounded-full p-1 transition hover:bg-white/20"
            title={t('modal.close')}
          >
            <XMarkIcon className="h-4 w-4" />
          </button>
        </div>
      ))}
    </div>
  );
};

export default Toast;
