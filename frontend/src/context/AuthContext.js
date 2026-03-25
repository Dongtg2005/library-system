import React, { createContext, useContext, useState, useEffect } from 'react';
import { authAPI } from '../services/api';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Initialize auth from localStorage
  useEffect(() => {
    const initializeAuth = async () => {
      try {
        const savedToken = localStorage.getItem('access_token');
        
        // If no token exists, user is not authenticated
        if (!savedToken) {
          setToken(null);
          setUser(null);
          setLoading(false);
          return;
        }
        
        try {
          // Validate the token by calling an authenticated endpoint
          const response = await authAPI.getCurrentUser();
          
          if (response && response.data) {
            // Token is valid, set authenticated state
            setToken(savedToken);
            setUser(response.data);
            localStorage.setItem('user', JSON.stringify(response.data));
          } else {
            // Response was successful but no data, clear auth
            throw new Error('No user data received');
          }
        } catch (err) {
          // Token is invalid, expired, or API error
          console.error('Token validation failed:', err.message);
          
          // Clear all auth data from localStorage
          localStorage.removeItem('access_token');
          localStorage.removeItem('user');
          
          // Clear state
          setToken(null);
          setUser(null);
        }
      } finally {
        setLoading(false);
      }
    };
    
    initializeAuth();
  }, []);

  const register = async (email, password, fullName) => {
    try {
      setError(null);
      const response = await authAPI.register({
        email,
        password,
        fullName,
      });
      
      const { accessToken, ...userData } = response.data;
      
      // Save to localStorage
      localStorage.setItem('access_token', accessToken);
      localStorage.setItem('user', JSON.stringify(userData));
      
      // Update state
      setToken(accessToken);
      setUser(userData);
      
      return userData;
    } catch (err) {
      const message = err.response?.data?.message || 'Registration failed';
      setError(message);
      throw new Error(message);
    }
  };

  const login = async (email, password) => {
    try {
      setError(null);
      const response = await authAPI.login({
        email,
        password,
      });
      
      const { accessToken, ...userData } = response.data;
      
      // Save to localStorage
      localStorage.setItem('access_token', accessToken);
      localStorage.setItem('user', JSON.stringify(userData));
      
      // Update state
      setToken(accessToken);
      setUser(userData);
      
      return userData;
    } catch (err) {
      const message = err.response?.data?.message || 'Login failed';
      setError(message);
      throw new Error(message);
    }
  };

  const logout = async () => {
    try {
      if (token) {
        await authAPI.logout();
      }
    } catch (err) {
      console.error('Logout error:', err);
    } finally {
      // Clear localStorage and state
      localStorage.removeItem('access_token');
      localStorage.removeItem('user');
      setToken(null);
      setUser(null);
      setError(null);
    }
  };

  const updateUser = (userData) => {
    setUser(userData);
    localStorage.setItem('user', JSON.stringify(userData));
  };

  const value = {
    user,
    token,
    loading,
    error,
    register,
    login,
    logout,
    updateUser,
    isAuthenticated: !!token,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
