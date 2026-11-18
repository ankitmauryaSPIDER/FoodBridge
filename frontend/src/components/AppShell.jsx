import { useEffect, useState } from 'react';
import { NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { Bell, ClipboardList, HandHeart, LayoutDashboard, Leaf, LogOut, Menu, Package, ShieldCheck, Truck, Users, X } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import api from '../api';

export default function AppShell() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [mobileOpen, setMobileOpen] = useState(false);
  const [unread, setUnread] = useState(0);
  useEffect(() => { api.get('/notifications').then(({ data }) => setUnread(data.filter(n => !n.read).length)).catch(() => {}); }, [location.pathname]);
  const roleLinks = {
    DONOR: [
      { to: '/app', label: 'Overview', icon: LayoutDashboard, end: true },
      { to: '/app/donations', label: 'My donations', icon: Package },
      { to: '/app/requests', label: 'Donation requests', icon: ClipboardList },
      { to: '/app/deliveries', label: 'Deliveries', icon: Truck },
      { to: '/app/notifications', label: 'Notifications', icon: Bell, badge: unread },
    ],
    NGO: [
      { to: '/app', label: 'Overview', icon: LayoutDashboard, end: true },
      { to: '/app/donations', label: 'Find food', icon: Package },
      { to: '/app/requests', label: 'My requests', icon: ClipboardList },
      { to: '/app/deliveries', label: 'Deliveries', icon: Truck },
      { to: '/app/notifications', label: 'Notifications', icon: Bell, badge: unread },
    ],
    VOLUNTEER: [
      { to: '/app', label: 'Overview', icon: LayoutDashboard, end: true },
      { to: '/app/deliveries', label: 'Delivery tasks', icon: Truck },
      { to: '/app/notifications', label: 'Notifications', icon: Bell, badge: unread },
    ],
    ADMIN: [
      { to: '/app', label: 'Overview', icon: LayoutDashboard, end: true },
      { to: '/app/admin', label: 'Administration', icon: ShieldCheck },
      { to: '/app/notifications', label: 'Notifications', icon: Bell, badge: unread },
    ],
  };
  const links = roleLinks[user?.role] || [];
  const title = user?.role === 'NGO' ? (user.organizationName || user.name) : user?.name;
  const signOut = () => { logout(); navigate('/'); };
  return <div className="app-layout">
    {mobileOpen && <button className="mobile-overlay" onClick={() => setMobileOpen(false)} aria-label="Close navigation"/>}
    <aside className={`sidebar ${mobileOpen ? 'sidebar-open' : ''}`}>
      <div className="sidebar-brand-row"><NavLink to="/" className="brand"><span className="brand-mark"><Leaf size={20}/></span><span>Food<span className="brand-light">Bridge</span></span></NavLink><button className="icon-button mobile-close" onClick={() => setMobileOpen(false)}><X size={19}/></button></div>
      <div className="workspace-label">WORKSPACE</div>
      <nav className="side-nav">{links.map(item => <NavLink key={item.to} to={item.to} end={item.end} onClick={() => setMobileOpen(false)} className={({ isActive }) => `side-link ${isActive ? 'active' : ''}`}><item.icon size={18}/><span>{item.label}</span>{item.badge > 0 && <span className="nav-badge">{item.badge}</span>}</NavLink>)}</nav>
      <div className="sidebar-help"><div className="help-icon"><HandHeart size={19}/></div><strong>Every contribution counts</strong><p>Thank you for helping good food find a good home.</p><a href="mailto:hello@foodbridge.local">Contact support ↗</a></div>
      <div className="sidebar-user"><div className="avatar">{(user?.name || 'U').slice(0, 1).toUpperCase()}</div><div className="user-info"><strong>{title}</strong><span>{user?.role?.toLowerCase()}</span></div><button className="icon-button logout-button" onClick={signOut} title="Log out"><LogOut size={17}/></button></div>
    </aside>
    <main className="app-main"><header className="topbar"><button className="icon-button mobile-menu" onClick={() => setMobileOpen(true)}><Menu size={20}/></button><div><span className="topbar-kicker">FOODBRIDGE WORKSPACE</span><strong>{links.find(l => l.to === location.pathname)?.label || 'Community dashboard'}</strong></div><div className="topbar-right"><span className="status-chip"><span className="status-dot"/> Platform online</span><button className="topbar-profile" onClick={() => navigate('/app/notifications')}><span className="avatar avatar-small">{(user?.name || 'U').slice(0, 1).toUpperCase()}</span><span>{user?.name}</span></button></div></header><div className="content-wrap"><Outlet/></div><footer className="app-footer"><span>FoodBridge · Community food rescue</span><span>Use good judgement and follow local food-safety guidance.</span></footer></main>
  </div>;
}
