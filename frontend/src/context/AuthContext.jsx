import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import api from '../api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    try { return JSON.parse(localStorage.getItem('foodbridge_user') || 'null'); } catch { return null; }
  });
  const [loading, setLoading] = useState(Boolean(localStorage.getItem('foodbridge_token')));

  useEffect(() => {
    const token = localStorage.getItem('foodbridge_token');
    if (!token) { setLoading(false); return; }
    api.get('/auth/me')
      .then(({ data }) => { setUser(data); localStorage.setItem('foodbridge_user', JSON.stringify(data)); })
      .catch(() => { localStorage.removeItem('foodbridge_token'); localStorage.removeItem('foodbridge_user'); setUser(null); })
      .finally(() => setLoading(false));
  }, []);

  const value = useMemo(() => ({
    user, loading,
    async login(email, password) {
      const { data } = await api.post('/auth/login', { email, password });
      localStorage.setItem('foodbridge_token', data.token);
      localStorage.setItem('foodbridge_user', JSON.stringify(data.user));
      setUser(data.user); return data.user;
    },
    async register(form) {
      const { data } = await api.post('/auth/register', form);
      localStorage.setItem('foodbridge_token', data.token);
      localStorage.setItem('foodbridge_user', JSON.stringify(data.user));
      setUser(data.user); return data.user;
    },
    logout() {
      localStorage.removeItem('foodbridge_token'); localStorage.removeItem('foodbridge_user'); setUser(null);
    },
    refreshUser: async () => {
      const { data } = await api.get('/auth/me'); setUser(data); localStorage.setItem('foodbridge_user', JSON.stringify(data));
    }
  }), [user, loading]);
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const value = useContext(AuthContext);
  if (!value) throw new Error('useAuth must be used inside AuthProvider');
  return value;
}
