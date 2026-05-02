import React from 'react';
import { useTranslation } from '../context/LanguageContext';

const Input = ({ label, error, required = false, className = '', ...props }) => {
  const { t } = useTranslation();
  
  return (
    <label className={`block ${className}`}>
      <span className="mb-2 block text-sm font-medium text-slate-600 dark:text-slate-300">
        {label}
        {required && <span className="ml-1 text-rose-500">*</span>}
        {!required && <span className="ml-1 text-xs text-slate-400">({t('input.optional')})</span>}
      </span>
      <input
        className={`w-full rounded-2xl border border-slate-200 bg-white/80 px-4 py-3 text-sm text-slate-900 outline-none transition placeholder:text-slate-400 focus:border-primary focus:ring-4 focus:ring-primary/10 dark:border-slate-700 dark:bg-slate-900/70 dark:text-white ${error ? 'border-rose-400 focus:border-rose-500 focus:ring-rose-500/10' : ''}`}
        {...props}
      />
      {error && <p className="mt-2 text-sm text-rose-500">{error}</p>}
    </label>
  );
};

export default Input;
