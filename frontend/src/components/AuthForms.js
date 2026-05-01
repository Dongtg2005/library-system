import React, { useEffect, useState } from 'react';
import { EyeIcon, EyeSlashIcon, ShieldCheckIcon } from '@heroicons/react/24/outline';
import { Link } from 'react-router-dom';
import Button from './Button';
import Input from './Input';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';

const AuthForms = ({ initialMode = 'login' }) => {
  const [mode, setMode] = useState(initialMode === 'register' ? 'register' : 'login');
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState({ fullName: '', email: '', password: '' });
  const [errors, setErrors] = useState({});
  const { login, register } = useAuth();
  const toast = useToast();

  useEffect(() => {
    setMode(initialMode === 'register' ? 'register' : 'login');
  }, [initialMode]);

  const validate = () => {
    const next = {};
    if (mode === 'register' && !form.fullName.trim()) next.fullName = 'Full name is required';
    if (!form.email.trim()) next.email = 'Email is required';
    else if (!/\S+@\S+\.\S+/.test(form.email)) next.email = 'Invalid email format';
    if (!form.password.trim()) next.password = 'Password is required';
    else if (form.password.length < 6) next.password = 'Password must be at least 6 characters';
    setErrors(next);
    return Object.keys(next).length === 0;
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (!validate()) {
      toast?.addToast({ type: 'warning', title: 'Validation failed', message: 'Please fix the highlighted fields.' });
      return;
    }

    setLoading(true);
    try {
      if (mode === 'login') {
        await login(form.email, form.password);
        toast?.addToast({ type: 'success', title: 'Welcome back', message: 'You have been signed in successfully.' });
      } else {
        await register(form.email, form.password, form.fullName);
        toast?.addToast({ type: 'success', title: 'Account created', message: 'Your account is ready to use.' });
      }
    } catch (error) {
      toast?.addToast({ type: 'error', title: 'Authentication failed', message: error.message || 'Unable to continue.' });
      setErrors((prev) => ({ ...prev, form: error.message || 'Authentication failed' }));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="grid gap-8 lg:grid-cols-[1.1fr_0.9fr]">
      <div className="rounded-[32px] bg-[linear-gradient(140deg,#042f2e,#14b8a6_52%,#06b6d4_100%)] p-6 text-white shadow-2xl sm:p-8 lg:p-10">
        <div className="inline-flex items-center gap-2 rounded-full bg-white/15 px-4 py-2 text-sm font-semibold backdrop-blur">
          <ShieldCheckIcon className="h-5 w-5" /> JWT Authentication
        </div>
        <h2 className="mt-6 text-3xl font-black leading-tight tracking-tight sm:text-4xl">Library access for users, librarians, and admins.</h2>
        <p className="mt-4 max-w-xl text-base text-white/85">
          Secure authentication with role-aware navigation and real backend integration.
        </p>
        <div className="mt-8 grid gap-4 sm:grid-cols-3">
          {[
            ['Auth API', 'Register and login endpoints'],
            ['Books API', 'Live catalog and detail data'],
            ['Borrow API', 'Borrow, return, and history flow'],
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
            <p className="text-sm font-semibold uppercase tracking-[0.2em] text-slate-500">Secure access</p>
            <h3 className="mt-2 text-2xl font-black text-slate-900 dark:text-white">{mode === 'login' ? 'Login' : 'Create account'}</h3>
          </div>
          <div className="rounded-2xl bg-primary/10 p-3 text-primary">
            <ShieldCheckIcon className="h-6 w-6" />
          </div>
        </div>

        <div className="mt-6 inline-flex rounded-2xl bg-slate-100 p-1 dark:bg-slate-900">
          <Link to="/login" className={`rounded-2xl px-4 py-2 text-sm font-semibold transition ${mode === 'login' ? 'bg-white text-primary shadow dark:bg-slate-800 dark:text-white' : 'text-slate-500'}`}>Login</Link>
          <Link to="/register" className={`rounded-2xl px-4 py-2 text-sm font-semibold transition ${mode === 'register' ? 'bg-white text-primary shadow dark:bg-slate-800 dark:text-white' : 'text-slate-500'}`}>Register</Link>
        </div>

        {errors.form && <div className="mt-6 rounded-2xl border border-rose-500/20 bg-rose-500/10 px-4 py-3 text-sm text-rose-600 dark:text-rose-300">{errors.form}</div>}

        <form onSubmit={handleSubmit} className="mt-6 space-y-5">
          {mode === 'register' && (
            <Input label="Full Name" value={form.fullName} onChange={(e) => setForm({ ...form, fullName: e.target.value })} error={errors.fullName} placeholder="Nguyen Hoang Phung" />
          )}
          <Input label="Email" type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} error={errors.email} placeholder="you@library.com" />
          <div className="relative">
            <Input label="Password" type={showPassword ? 'text' : 'password'} value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} error={errors.password} placeholder="********" />
            <button type="button" onClick={() => setShowPassword((prev) => !prev)} className="absolute right-4 top-9 rounded-xl p-2 text-slate-500 transition hover:bg-slate-100 dark:hover:bg-slate-800">
              {showPassword ? <EyeSlashIcon className="h-5 w-5" /> : <EyeIcon className="h-5 w-5" />}
            </button>
          </div>

          <Button type="submit" size="lg" className="w-full" disabled={loading}>
            {loading ? 'Please wait...' : mode === 'login' ? 'Sign in' : 'Create account'}
          </Button>
        </form>
      </div>
    </div>
  );
};

export default AuthForms;
