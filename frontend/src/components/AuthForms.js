import React, { useEffect, useState } from 'react';
import { EyeIcon, EyeSlashIcon, ShieldCheckIcon } from '@heroicons/react/24/outline';
import { Link } from 'react-router-dom';
import Button from './Button';
import Input from './Input';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { useTranslation } from '../context/LanguageContext';

const AuthForms = ({ initialMode = 'login' }) => {
  const [mode, setMode] = useState(initialMode === 'register' ? 'register' : 'login');
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState({ fullName: '', email: '', password: '' });
  const [errors, setErrors] = useState({});
  const { login, register } = useAuth();
  const toast = useToast();
  const { t } = useTranslation();

  useEffect(() => {
    setMode(initialMode === 'register' ? 'register' : 'login');
  }, [initialMode]);

  const validate = () => {
    const next = {};
    if (mode === 'register' && !form.fullName.trim()) next.fullName = t('auth.fullNameRequired');
    if (!form.email.trim()) next.email = t('auth.emailRequired');
    else if (!/\S+@\S+\.\S+/.test(form.email)) next.email = t('auth.invalidEmail');
    if (!form.password.trim()) next.password = t('auth.passwordRequired');
    else if (form.password.length < 6) next.password = t('auth.passwordMinLength');
    setErrors(next);
    return Object.keys(next).length === 0;
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (!validate()) {
      toast?.addToast({ type: 'warning', title: t('auth.validationFailed'), message: t('auth.fixFields') });
      return;
    }

    setLoading(true);
    try {
      if (mode === 'login') {
        await login(form.email, form.password);
        toast?.addToast({ type: 'success', title: t('auth.welcomeBack'), message: t('auth.signInSuccess') });
      } else {
        await register(form.email, form.password, form.fullName);
        toast?.addToast({ type: 'success', title: t('auth.accountCreated'), message: t('auth.accountReady') });
      }
    } catch (error) {
      toast?.addToast({ type: 'error', title: t('auth.authFailed'), message: error.message || t('auth.unableToContinue') });
      setErrors((prev) => ({ ...prev, form: error.message || t('auth.authFailed') }));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="grid gap-8 lg:grid-cols-[1.1fr_0.9fr]">
      <div className="rounded-[32px] bg-[linear-gradient(140deg,#042f2e,#14b8a6_52%,#06b6d4_100%)] p-6 text-white shadow-2xl sm:p-8 lg:p-10">
        <div className="inline-flex items-center gap-2 rounded-full bg-white/15 px-4 py-2 text-sm font-semibold backdrop-blur">
          <ShieldCheckIcon className="h-5 w-5" /> {t('auth.jwtAuthentication')}
        </div>
        <h2 className="mt-6 text-3xl font-black leading-tight tracking-tight sm:text-4xl">{t('auth.libraryAccess')}</h2>
        <p className="mt-4 max-w-xl text-base text-white/85">
          {t('auth.secureAuth')}
        </p>
        <div className="mt-8 grid gap-4 sm:grid-cols-3">
          {[
            [t('auth.authApi'), t('auth.authApiDesc')],
            [t('auth.booksApi'), t('auth.booksApiDesc')],
            [t('auth.borrowApi'), t('auth.borrowApiDesc')],
          ].map(([title, text]) => (
            <div key={title} className="rounded-3xl bg-white/12 p-4 backdrop-blur">
              <p className="text-sm font-bold">{title}</p>
              <p className="mt-2 text-sm text-white/80">{text}</p>
            </div>
          ))}
        </div>
      </div>

      <div className="rounded-[32px] border border-slate-200 bg-white/95 p-5 shadow-2xl shadow-slate-200/40 backdrop-blur-xl dark:border-slate-800 dark:bg-slate-950/80 dark:shadow-slate-950/30 sm:p-8">
        <div className="flex items-center justify-between gap-4">
          <div>
            <p className="text-sm font-semibold uppercase tracking-[0.2em] text-slate-500">{t('auth.secureAccess')}</p>
            <h3 className="mt-2 text-2xl font-black text-slate-900 dark:text-white">{mode === 'login' ? t('auth.loginTitle') : t('auth.createAccount')}</h3>
          </div>
          <div className="rounded-2xl bg-primary/10 p-3 text-primary">
            <ShieldCheckIcon className="h-6 w-6" />
          </div>
        </div>

        <div className="mt-6 inline-flex rounded-2xl bg-slate-100 p-1 dark:bg-slate-900">
          <Link to="/login" className={`rounded-2xl px-4 py-2 text-sm font-semibold transition ${mode === 'login' ? 'bg-white text-primary shadow dark:bg-slate-800 dark:text-white' : 'text-slate-500'}`}>{t('auth.login')}</Link>
          <Link to="/register" className={`rounded-2xl px-4 py-2 text-sm font-semibold transition ${mode === 'register' ? 'bg-white text-primary shadow dark:bg-slate-800 dark:text-white' : 'text-slate-500'}`}>{t('auth.register')}</Link>
        </div>

        {errors.form && <div className="mt-6 rounded-2xl border border-rose-500/20 bg-rose-500/10 px-4 py-3 text-sm text-rose-600 dark:text-rose-300">{errors.form}</div>}

        <form onSubmit={handleSubmit} className="mt-6 space-y-5">
          {mode === 'register' && (
            <Input 
              label={t('auth.fullName')} 
              value={form.fullName} 
              onChange={(e) => setForm({ ...form, fullName: e.target.value })} 
              error={errors.fullName} 
              placeholder={t('auth.fullNamePlaceholder')} 
            />
          )}
          <Input 
            label={t('auth.email')} 
            type="email" 
            value={form.email} 
            onChange={(e) => setForm({ ...form, email: e.target.value })} 
            error={errors.email} 
            placeholder={t('auth.emailPlaceholder')} 
          />
          <div className="relative">
            <Input 
              label={t('auth.password')} 
              type={showPassword ? 'text' : 'password'} 
              value={form.password} 
              onChange={(e) => setForm({ ...form, password: e.target.value })} 
              error={errors.password} 
              placeholder={t('auth.passwordPlaceholder')} 
            />
            <button type="button" onClick={() => setShowPassword((prev) => !prev)} className="absolute right-4 top-9 rounded-xl p-2 text-slate-500 transition hover:bg-slate-100 dark:hover:bg-slate-800">
              {showPassword ? <EyeSlashIcon className="h-5 w-5" /> : <EyeIcon className="h-5 w-5" />}
            </button>
          </div>

          <Button type="submit" size="lg" className="w-full" disabled={loading}>
            {loading ? t('auth.pleaseWait') : mode === 'login' ? t('auth.signIn') : t('auth.createAccountButton')}
          </Button>
        </form>

        <div className="mt-6 flex items-center justify-between gap-4">
          <span className="h-px flex-1 bg-slate-200 dark:bg-slate-800"></span>
          <span className="text-xs uppercase tracking-wider text-slate-400">hoặc</span>
          <span className="h-px flex-1 bg-slate-200 dark:bg-slate-800"></span>
        </div>

        <button
          type="button"
          onClick={() => window.location.href = '/oauth2/authorization/google'}
          className="mt-4 w-full flex items-center justify-center gap-3 rounded-2xl border border-slate-200 bg-white px-5 py-3 text-sm font-semibold text-slate-700 shadow-sm hover:bg-slate-50 dark:border-slate-800 dark:bg-slate-900 dark:text-slate-100 dark:hover:bg-slate-800/80 transition-all active:scale-[0.98]"
        >
          <svg className="h-5 w-5 flex-shrink-0" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4"/>
            <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
            <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z" fill="#FBBC05"/>
            <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335"/>
          </svg>
          <span>Đăng nhập với Google</span>
        </button>
      </div>
    </div>
  );
};

export default AuthForms;
