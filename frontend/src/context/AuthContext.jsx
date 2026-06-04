import { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { api } from '../api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem('hotelAuthToken');
    const role = localStorage.getItem('hotelAuthRole');
    const userId = localStorage.getItem('hotelAuthUserId');
    const fullName = localStorage.getItem('hotelAuthName');
    if (token && userId) {
      setUser({ token, role, userId: Number(userId), fullName });
    }
    setLoading(false);
  }, []);

  const login = useCallback(async (email, password) => {
    const data = await api('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    });
    const u = {
      token: data.token,
      role: data.role,
      userId: data.userId,
      fullName: data.fullName,
    };
    localStorage.setItem('hotelAuthToken', data.token);
    localStorage.setItem('hotelAuthRole', data.role);
    localStorage.setItem('hotelAuthUserId', String(data.userId));
    localStorage.setItem('hotelAuthName', data.fullName);
    setUser(u);
    return u;
  }, []);

  const register = useCallback(async (form) => {
    const data = await api('/api/auth/register', {
      method: 'POST',
      body: JSON.stringify(form),
    });
    const u = {
      token: data.token,
      role: data.role,
      userId: data.userId,
      fullName: data.fullName,
    };
    localStorage.setItem('hotelAuthToken', data.token);
    localStorage.setItem('hotelAuthRole', data.role);
    localStorage.setItem('hotelAuthUserId', String(data.userId));
    localStorage.setItem('hotelAuthName', data.fullName);
    setUser(u);
    return u;
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('hotelAuthToken');
    localStorage.removeItem('hotelAuthRole');
    localStorage.removeItem('hotelAuthUserId');
    localStorage.removeItem('hotelAuthName');
    setUser(null);
  }, []);

  const isAdmin = user?.role === 'ADMIN' || user?.role === 'MANAGER';
  const isStaff = user?.role === 'STAFF' || isAdmin;

  return (
    <AuthContext.Provider value={{ user, login, register, logout, loading, isAdmin, isStaff }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
