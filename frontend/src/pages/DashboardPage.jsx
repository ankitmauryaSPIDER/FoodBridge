import { useEffect, useState } from 'react';
import { ArrowRight, CheckCircle2, Clock3, HandHeart, HeartHandshake, MapPin, Package, Plus, ShieldCheck, Truck, Utensils, Users, ClipboardList } from 'lucide-react';
import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api, { getErrorMessage } from '../api';

function Stat({ icon: Icon, label, value, tone = 'green', hint }) {
  return <div className="stat-card"><div className={`stat-icon stat-${tone}`}><Icon size={19}/></div><div className="stat-copy"><span>{label}</span><strong>{value ?? '—'}</strong>{hint && <small>{hint}</small>}</div></div>;
}
function fmt(date) { return date ? new Date(date).toLocaleString([], { dateStyle: 'medium', timeStyle: 'short' }) : 'Not specified'; }

export default function DashboardPage() {
  const { user } = useAuth();
  const location = useLocation();
  const [data, setData] = useState({ donations: [], requests: [], deliveries: [], available: [], admin: null });
  const [busy, setBusy] = useState(true);
  const [error, setError] = useState('');
  useEffect(() => {
    let active = true;
    async function load() {
      setBusy(true); setError('');
      try {
        if (user.role === 'ADMIN') {
          const { data: admin } = await api.get('/admin/overview');
          if (active) setData(d => ({ ...d, admin }));
        } else if (user.role === 'DONOR') {
          const [donations, requests, deliveries] = await Promise.all([api.get('/donations/mine'), api.get('/requests/mine'), api.get('/deliveries/mine')]);
          if (active) setData({ donations: donations.data, requests: requests.data, deliveries: deliveries.data, available: [], admin: null });
        } else if (user.role === 'NGO') {
          const [available, requests, deliveries] = await Promise.all([api.get('/donations'), api.get('/requests/mine'), api.get('/deliveries/mine')]);
          if (active) setData({ donations: [], requests: requests.data, deliveries: deliveries.data, available: available.data, admin: null });
        } else {
          const [available, deliveries] = await Promise.all([api.get('/deliveries/available'), api.get('/deliveries/mine')]);
          if (active) setData({ donations: [], requests: [], deliveries: deliveries.data, available: available.data, admin: null });
        }
      } catch (err) { if (active) setError(getErrorMessage(err)); }
      finally { if (active) setBusy(false); }
    }
    load(); return () => { active = false; };
  }, [user, location.key]);

  const greeting = user?.name?.split(' ')[0] || 'there';
  const subtitle = user.role === 'DONOR' ? 'Here is what is happening with your food donations.' : user.role === 'NGO' ? 'Discover surplus food and manage your community requests.' : user.role === 'VOLUNTEER' ? 'Your time helps food reach people who need it.' : 'Here is an overview of activity across FoodBridge.';
  const delivered = data.deliveries.filter(d => d.status === 'COMPLETED').length;
  return <div className="page-stack">
    <section className="welcome-row"><div><div className="eyebrow">{new Date().toLocaleDateString([], { weekday: 'long', month: 'long', day: 'numeric' })}</div><h1>Good day, {greeting} <span className="wave">✦</span></h1><p>{subtitle}</p></div>{user.role === 'DONOR' && <Link to="/app/donations" className="btn btn-primary"><Plus size={17}/> Add a donation</Link>}{user.role === 'NGO' && <Link to="/app/donations" className="btn btn-primary"><Package size={17}/> Find food</Link>}{user.role === 'VOLUNTEER' && <Link to="/app/deliveries" className="btn btn-primary"><Truck size={17}/> View tasks</Link>}{user.role === 'ADMIN' && <Link to="/app/admin" className="btn btn-primary"><ShieldCheck size={17}/> Admin controls</Link>}</section>
    {user.role === 'NGO' && !user.verified && <div className="alert alert-warning"><Clock3 size={18}/> Your organization profile is awaiting administrator verification. You can browse donations, but you cannot submit requests until approved.</div>}
    {error && <div className="alert alert-error">{error}</div>}
    {busy ? <div className="loading-panel"><span className="spinner"/> Loading dashboard…</div> : <>
      {user.role === 'ADMIN' && data.admin && <div className="stats-grid"><Stat icon={UsersIcon} label="Total users" value={data.admin.users}/><Stat icon={Utensils} label="Donors" value={data.admin.donors} tone="orange"/><Stat icon={HeartHandshake} label="Verified NGOs" value={data.admin.verifiedNgos} tone="purple"/><Stat icon={Truck} label="Completed deliveries" value={data.admin.completedDeliveries} tone="blue"/><Stat icon={Package} label="Total donations" value={data.admin.donations}/><Stat icon={Clock3} label="Pending requests" value={data.admin.pendingRequests} tone="orange"/></div>}
      {user.role === 'DONOR' && <div className="stats-grid"><Stat icon={Package} label="Total donations" value={data.donations.length}/><Stat icon={CheckCircle2} label="Available now" value={data.donations.filter(d => d.status === 'AVAILABLE').length} tone="blue"/><Stat icon={Clock3} label="Requests received" value={data.requests.length} tone="orange"/><Stat icon={Truck} label="Delivered" value={delivered} tone="purple"/></div>}
      {user.role === 'NGO' && <div className="stats-grid"><Stat icon={Package} label="Available listings" value={data.available.length}/><Stat icon={ClipboardIcon} label="My requests" value={data.requests.length} tone="orange"/><Stat icon={CheckCircle2} label="Accepted" value={data.requests.filter(r => r.requestStatus === 'ACCEPTED').length} tone="blue"/><Stat icon={Truck} label="Completed deliveries" value={delivered} tone="purple"/></div>}
      {user.role === 'VOLUNTEER' && <div className="stats-grid"><Stat icon={Truck} label="Available tasks" value={data.available.length}/><Stat icon={Package} label="My assignments" value={data.deliveries.length} tone="orange"/><Stat icon={CheckCircle2} label="Completed" value={delivered} tone="blue"/><Stat icon={Clock3} label="In progress" value={data.deliveries.filter(d => d.status !== 'COMPLETED').length} tone="purple"/></div>}
      <div className="dashboard-grid"><section className="panel panel-main"><div className="panel-head"><div><h2>{user.role === 'DONOR' ? 'Your recent donations' : user.role === 'NGO' ? 'Recently available food' : user.role === 'VOLUNTEER' ? 'Available delivery tasks' : 'Platform health'}</h2><p>{user.role === 'DONOR' ? 'A quick look at the food you have listed.' : 'Keep an eye on current activity.'}</p></div><Link to={user.role === 'DONOR' || user.role === 'NGO' ? '/app/donations' : user.role === 'VOLUNTEER' ? '/app/deliveries' : '/app/admin'} className="text-link">View all <ArrowRight size={15}/></Link></div>
        {(user.role === 'DONOR' ? data.donations.slice(0, 4) : user.role === 'NGO' ? data.available.slice(0, 4) : user.role === 'VOLUNTEER' ? data.available.slice(0, 4) : []).length > 0 ? (user.role === 'DONOR' ? data.donations.slice(0, 4) : user.role === 'NGO' ? data.available.slice(0, 4) : data.available.slice(0, 4)).map(item => <div className="mini-list-row" key={item.id}><div className="mini-food-icon"><Utensils size={18}/></div><div className="mini-row-text"><strong>{item.foodName}</strong><span><MapPin size={13}/> {item.city} · {item.quantity} {item.unit}</span></div><span className={`status-pill status-${(item.status || 'AVAILABLE').toLowerCase()}`}>{(item.status || 'AVAILABLE').replace('_', ' ')}</span></div>) : <div className="empty-state"><div className="empty-icon"><Package size={23}/></div><strong>Nothing to show just yet</strong><span>{user.role === 'DONOR' ? 'Add your first food donation to get started.' : user.role === 'NGO' ? 'No available listings found at the moment.' : user.role === 'VOLUNTEER' ? 'There are no unassigned tasks right now.' : 'The platform is ready for activity.'}</span></div>}
      </section><aside className="panel impact-panel"><div className="impact-panel-art"><div className="impact-art-circle"><HandHeart size={30}/></div><span className="art-leaf">✳</span></div><div className="eyebrow">A little reminder</div><h2>Good food can do good things.</h2><p>Clear details and timely collection make every food handoff more responsible.</p><div className="impact-check"><CheckCircle2 size={16}/> Share accurate pickup deadlines</div><div className="impact-check"><CheckCircle2 size={16}/> Include storage and allergen details</div><div className="impact-check"><CheckCircle2 size={16}/> Confirm each handoff</div></aside></div>
    </>}
  </div>;
}
function UsersIcon(props) { return <Users {...props}/>; }
function ClipboardIcon(props) { return <ClipboardList {...props}/>; }
