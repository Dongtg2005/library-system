import React from 'react';
import { useTranslation } from '../context/LanguageContext';
import AuthForms from '../components/AuthForms';

const AuthPage = ({ initialMode = 'login' }) => {
  const { t } = useTranslation();
  
  return (
    <div className="min-h-screen bg-slate-100 p-4 text-slate-900 dark:bg-slate-950 dark:text-slate-100 sm:p-6 lg:p-8">
      <div className="mx-auto grid min-h-[calc(100vh-2rem)] max-w-7xl place-items-center">
        <AuthForms initialMode={initialMode} />
      </div>
    </div>
  );
};

export default AuthPage;
