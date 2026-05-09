import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const OAuth2RedirectHandler = () => {
  const navigate = useNavigate();
  const { loginWithTokens } = useAuth();

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const token = params.get('token');
    const refreshToken = params.get('refreshToken');

    if (token && refreshToken) {
      loginWithTokens(token, refreshToken);
      // Chuyển hướng về trang chủ hoặc dashboard
      navigate('/');
    } else {
      navigate('/login?error=oauth2_failed');
    }
  }, [navigate, loginWithTokens]);

  return (
    <div className="flex min-h-screen items-center justify-center bg-teal-50 text-slate-700 dark:bg-slate-950 dark:text-slate-100">
      <div className="rounded-3xl border border-teal-100 bg-white px-8 py-6 text-center font-semibold shadow-xl shadow-teal-500/10 dark:border-slate-800 dark:bg-slate-900">
        <div className="mb-4 h-12 w-12 animate-spin rounded-full border-4 border-indigo-600 border-t-transparent mx-auto"></div>
        <p className="text-lg text-indigo-600 dark:text-indigo-400">Đang xử lý đăng nhập Google...</p>
        <p className="text-xs text-slate-400 mt-2">Vui lòng chờ trong giây lát</p>
      </div>
    </div>
  );
};

export default OAuth2RedirectHandler;
