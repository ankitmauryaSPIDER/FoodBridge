import { useEffect, useState } from 'react';
import { Bell, CheckCheck, Clock3, Package, Truck } from 'lucide-react';
import api, { getErrorMessage } from '../api';

function IconFor({ type }) { if (type === 'DELIVERY') return <Truck size={18}/>; if (type === 'REQUEST') return <Package size={18}/>; return <Bell size={18}/>; }
export default function NotificationsPage() {
  const [items, setItems] = useState([]); const [loading, setLoading] = useState(true); const [error, setError] = useState('');
  async function load() { setLoading(true); try { const { data } = await api.get('/notifications'); setItems(data); } catch (e) { setError(getErrorMessage(e)); } finally { setLoading(false); } }
  useEffect(() => { load(); }, []);
  async function markRead(id) { await api.put(`/notifications/${id}/read`); await load(); }
  async function markAll() { await api.put('/notifications/read-all'); await load(); }
  const unread = items.filter(i => !i.read).length;
  return <div className="page-stack"><section className="page-heading"><div><div className="eyebrow">UPDATES & ACTIVITY</div><h1>Notifications</h1><p>Stay up to date with requests, verification and delivery changes.</p></div>{unread > 0 && <button className="btn btn-outline" onClick={markAll}><CheckCheck size={17}/> Mark all as read</button>}</section>
    {error && <div className="alert alert-error">{error}</div>}
    <section className="panel"><div className="panel-head"><div><h2>Recent activity</h2><p>{unread} unread notifications</p></div></div>{loading ? <div className="loading-panel"><span className="spinner"/> Loading notifications…</div> : items.length ? <div className="notification-list">{items.map(item => <article key={item.id} className={`notification-row ${item.read ? '' : 'notification-unread'}`}><span className="notification-icon"><IconFor type={item.type}/></span><div className="notification-copy"><strong>{item.title}</strong><p>{item.message}</p><small><Clock3 size={13}/> {new Date(item.createdAt).toLocaleString()}</small></div>{!item.read && <button className="btn btn-small btn-ghost" onClick={() => markRead(item.id)}>Mark read</button>}</article>)}</div> : <div className="empty-state"><div className="empty-icon"><Bell size={24}/></div><strong>You're all caught up</strong><span>New activity will appear here.</span></div>}</section>
  </div>;
}
