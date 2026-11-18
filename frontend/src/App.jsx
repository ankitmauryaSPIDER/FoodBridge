import { Navigate, Route, Routes } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import LandingPage from './pages/LandingPage';
import AuthPage from './pages/AuthPage';
import AppShell from './components/AppShell';
import DashboardPage from './pages/DashboardPage';
import DonationsPage from './pages/DonationsPage';
import RequestsPage from './pages/RequestsPage';
import DeliveriesPage from './pages/DeliveriesPage';
import AdminPage from './pages/AdminPage';
import NotificationsPage from './pages/NotificationsPage';

function Protected({ children, roles }) {
  const { user, loading } = useAuth();
  if (loading) return <div className="screen-center"><span className="spinner" /> Loading FoodBridge…</div>;
  if (!user) return <Navigate to="/login" replace />;
  if (roles && !roles.includes(user.role)) return <Navigate to="/app" replace />;
  return children;
}

export default function App() {
  return <Routes>
    <Route path="/" element={<LandingPage />} />
    <Route path="/login" element={<AuthPage mode="login" />} />
    <Route path="/register" element={<AuthPage mode="register" />} />
    <Route path="/app" element={<Protected><AppShell /></Protected>}>
      <Route index element={<DashboardPage />} />
      <Route path="donations" element={<DonationsPage />} />
      <Route path="requests" element={<RequestsPage />} />
      <Route path="deliveries" element={<DeliveriesPage />} />
      <Route path="notifications" element={<NotificationsPage />} />
      <Route path="admin" element={<Protected roles={['ADMIN']}><AdminPage /></Protected>} />
    </Route>
    <Route path="*" element={<Navigate to="/" replace />} />
  </Routes>;
}
