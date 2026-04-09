import React from 'react';
import AuthForms from '../components/AuthForms';

const AuthPage = () => {
  return (
    <div className="min-h-screen bg-slate-950 p-4 text-white sm:p-6 lg:p-8">
      <div className="mx-auto grid min-h-[calc(100vh-2rem)] max-w-7xl place-items-center">
        <AuthForms />
      </div>
    </div>
  );
};

export default AuthPage;
