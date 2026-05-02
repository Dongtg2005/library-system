import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useTranslation } from '../context/LanguageContext';
import './Login.css';

const Login = ({ onLoginSuccess }) => {
  const [isLogin, setIsLogin] = useState(true);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [fullName, setFullName] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login, register } = useAuth();
  const { t } = useTranslation();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      if (isLogin) {
        await login(email, password);
      } else {
        if (!fullName.trim()) {
          setError(t('auth.fullNameRequired'));
          setLoading(false);
          return;
        }
        await register(email, password, fullName);
      }
      onLoginSuccess();
    } catch (err) {
      setError(err.message || t('auth.authFailed'));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container">
      <div className="login-form">
        <h1>{t('login.title')}</h1>
        <h2>{isLogin ? t('auth.login') : t('auth.createAccount')}</h2>

        {error && <div className="error-message">{error}</div>}

        <form onSubmit={handleSubmit}>
          {!isLogin && (
            <div className="form-group">
              <label htmlFor="fullName">{t('auth.fullName')}</label>
              <input
                id="fullName"
                type="text"
                value={fullName}
                onChange={(e) => setFullName(e.target.value)}
                placeholder={t('auth.fullNamePlaceholder')}
                disabled={loading}
                required
              />
            </div>
          )}

          <div className="form-group">
            <label htmlFor="email">{t('auth.email')}</label>
            <input
              id="email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder={t('auth.emailPlaceholder')}
              disabled={loading}
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">{t('auth.password')}</label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder={t('auth.passwordPlaceholder')}
              disabled={loading}
              required
            />
          </div>

          <button type="submit" disabled={loading} className="submit-btn">
            {loading ? (isLogin ? t('auth.pleaseWait') : t('auth.pleaseWait')) : (isLogin ? t('auth.login') : t('auth.register'))}
          </button>
        </form>

        <div className="auth-toggle">
          <text>
            {isLogin ? t('auth.dontHaveAccount') : t('auth.alreadyHaveAccount')}
          </text>
          <button
            type="button"
            onClick={() => {
              setIsLogin(!isLogin);
              setError('');
            }}
            className="toggle-btn"
            disabled={loading}
          >
            {isLogin ? t('auth.register') : t('auth.login')}
          </button>
        </div>
      </div>
    </div>
  );
};

export default Login;
