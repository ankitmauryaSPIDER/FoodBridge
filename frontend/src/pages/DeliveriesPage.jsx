import { useEffect, useState } from 'react';
import { CheckCircle2, Clock3, MapPin, Package, Truck, X } from 'lucide-react';
import api, { getErrorMessage } from '../api';
import { useAuth } from '../context/AuthContext';

function pill(status) { return `status-pill status-${status.toLowerCase()}`; }
export default function DeliveriesPage() {
  const { user } = useAuth();
  const [available, setAvailable] = useState([]); const [mine, setMine] = useState([]); const [loading, setLoading] = useState(true); const [error, setError] = useState(''); const [success, setSuccess] = useState(''); const [codes, setCodes] = useState({});
  async function load() {
    setLoading(true); setError('');
    try {
      const minePromise = api.get('/deliveries/mine');
      if (user.role === 'VOLUNTEER') { const [a, m] = await Promise.all([api.get('/deliveries/available'), minePromise]); setAvailable(a.data); setMine(m.data); }
      else { const m = await minePromise; setMine(m.data); setAvailable([]); }
    } catch (e) { setError(getErrorMessage(e)); } finally { setLoading(false); }
  }
  useEffect(() => { load(); }, [user.role]);
  async function run(action, id, body) {
    setError(''); setSuccess('');
    try { await api.put(`/deliveries/${id}/${action}`, body || {}); setSuccess(action === 'claim' ? 'Delivery task assigned to you.' : action === 'pickup' ? 'Pickup marked successfully.' : 'Delivery completed successfully.'); await load(); }
    catch (e) { setError(getErrorMessage(e)); }
  }
  const volunteer = user.role === 'VOLUNTEER';
  return <div className="page-stack"><section className="page-heading"><div><div className="eyebrow">DELIVERY COORDINATION</div><h1>{volunteer ? 'Delivery tasks' : 'Delivery tracking'}</h1><p>{volunteer ? 'Claim an available task, collect the food and confirm delivery.' : 'Track the collection and delivery status for your food handoffs.'}</p></div></section>
    {error && <div className="alert alert-error">{error}</div>}{success && <div className="alert alert-success">{success}<button className="alert-close" onClick={() => setSuccess('')}><X size={15}/></button></div>}
    {volunteer && <section className="panel"><div className="panel-head"><div><h2>Available tasks</h2><p>Accepted donations that still need a volunteer.</p></div><span className="count-badge">{available.length}</span></div>{loading ? <div className="loading-panel"><span className="spinner"/> Loading tasks…</div> : available.length ? <div className="task-grid">{available.map(task => <article className="task-card" key={task.id}><div className="task-top"><span className="task-icon"><Truck size={19}/></span><span className={pill(task.status)}>{task.status.replaceAll('_', ' ')}</span></div><h3>{task.foodName}</h3><p className="task-sub">For {task.ngoName}</p><div className="task-detail"><MapPin size={15}/><span>{task.pickupAddress}, {task.city}</span></div><a className="map-link" href={`https://www.openstreetmap.org/search?query=${encodeURIComponent(task.pickupAddress + ", " + task.city)}`} target="_blank" rel="noreferrer"><MapPin size={13}/> Open pickup area in map ↗</a><div className="task-detail"><Package size={15}/><span>Pickup from {task.donorName}</span></div><button className="btn btn-primary btn-full" onClick={() => run('claim', task.id)}><Truck size={16}/> Claim this task</button></article>)}</div> : <div className="empty-state"><div className="empty-icon"><Truck size={24}/></div><strong>No tasks available</strong><span>New tasks appear when a donor accepts an NGO request.</span></div>}</section>}
    <section className="panel"><div className="panel-head"><div><h2>{volunteer ? 'My assignments' : 'Delivery records'}</h2><p>{mine.length} total deliveries</p></div></div>{loading ? <div className="loading-panel"><span className="spinner"/> Loading deliveries…</div> : mine.length ? <div className="delivery-list">{mine.map(task => <article className="delivery-row" key={task.id}><div className="delivery-symbol"><Truck size={20}/></div><div className="delivery-info"><strong>{task.foodName}</strong><span>{task.donorName} → {task.ngoName}</span><small><MapPin size={13}/> {task.pickupAddress}, {task.city}</small><a className="map-link" href={`https://www.openstreetmap.org/search?query=${encodeURIComponent(task.pickupAddress + ", " + task.city)}`} target="_blank" rel="noreferrer">Open map ↗</a>{task.volunteerName && <small>Volunteer: {task.volunteerName}</small>}</div><div className="delivery-state"><span className={pill(task.status)}>{task.status.replaceAll('_', ' ')}</span>{task.status === 'PICKED_UP' && <small>Picked up {task.pickupTime ? new Date(task.pickupTime).toLocaleString() : ''}</small>}{task.status === 'COMPLETED' && <small>Delivered {task.deliveredTime ? new Date(task.deliveredTime).toLocaleString() : ''}</small>}</div>
      {volunteer && task.volunteerName && task.status === 'READY_FOR_PICKUP' && <button className="btn btn-small btn-primary" onClick={() => run('pickup', task.id)}>Mark picked up</button>}
      {volunteer && task.volunteerName && task.status === 'PICKED_UP' && <div className="complete-delivery"><input value={codes[task.id] || ''} onChange={e => setCodes(c => ({ ...c, [task.id]: e.target.value }))} placeholder="6-digit code" maxLength={6}/><button className="btn btn-small btn-success" onClick={() => run('complete', task.id, { verificationCode: codes[task.id] })}><CheckCircle2 size={15}/> Complete</button></div>}
      {!volunteer && ['READY_FOR_PICKUP', 'PICKED_UP'].includes(task.status) && task.verificationCode && <div className="verification-note"><span>Delivery code</span><strong>{task.verificationCode}</strong><small>Share this code only with the assigned recipient at handoff.</small></div>}
    </article>)}</div> : <div className="empty-state"><div className="empty-icon"><Clock3 size={24}/></div><strong>No delivery records yet</strong><span>Accepted donation requests will create delivery records here.</span></div>}</section>
  </div>;
}
