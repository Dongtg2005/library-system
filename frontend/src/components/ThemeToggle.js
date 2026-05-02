import React from 'react';
import { MoonIcon, SunIcon } from '@heroicons/react/24/outline';
import { useTheme } from '../context/ThemeContext';
import { useTranslation } from '../context/LanguageContext';

const ThemeToggle = ({ className = '' }) => {
  const { darkMode, toggleTheme } = useTheme();
  const { t } = useTranslation();

  return (
    <button
      type="button"
      onClick={toggleTheme}
      className={`inline-flex items-center gap-2 rounded-full border border-slate-200 bg-white px-4 py-2 text-sm font-semibold text-slate-700 shadow-sm transition hover:border-primary hover:text-primary dark:border-slate-700 dark:bg-slate-900 dark:text-slate-100 ${className}`}
      title={t('themeToggle.toggleTheme')}
    >
      {darkMode ? <SunIcon className="h-4 w-4" /> : <MoonIcon className="h-4 w-4" />}
      <span>{darkMode ? t('themeToggle.light') : t('themeToggle.dark')}</span>
    </button>
  );
};

export default ThemeToggle;
