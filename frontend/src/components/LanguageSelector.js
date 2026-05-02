import React from 'react';
import { useTranslation } from '../context/LanguageContext';

const LanguageSelector = ({ className = '' }) => {
  const { language, changeLanguage, availableLanguages } = useTranslation();

  const getLanguageLabel = (lang) => {
    const labels = {
      en: 'English',
      vi: 'Tiếng Việt',
    };
    return labels[lang] || lang;
  };

  return (
    <div className={`relative ${className}`}>
      <select
        aria-label="Select language"
        value={language}
        onChange={(e) => changeLanguage(e.target.value)}
        className="block w-full min-w-32 rounded-full border border-slate-200 bg-white px-3 py-2 text-sm font-semibold text-slate-700 shadow-sm transition focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary dark:border-slate-700 dark:bg-slate-900 dark:text-slate-100"
      >
        {availableLanguages.map((lang) => (
          <option key={lang} value={lang}>
            {getLanguageLabel(lang)}
          </option>
        ))}
      </select>
    </div>
  );
};

export default LanguageSelector;
