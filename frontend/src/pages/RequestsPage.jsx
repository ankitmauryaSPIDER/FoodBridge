import { useEffect, useState } from 'react';
import { Check, Clock3, ClipboardList, X } from 'lucide-react';
import api, { getErrorMessage } from '../api';
import { useAuth } from '../context/AuthContext';

function statusClass(status) { return `status-pill status-${status.toLowerCase()}`; }
export default function RequestsPage() {
  const { user } = useAuth();
  const [items, setItems] = useState([]); const [loading, setLoading] = useState(true); const [error, setError] = useState(''); const [success, setSuccess] = useState('');
  async function load() { setLoading(true); setError(''); try { const { data } = await api.get('/requests/mine'); setItems(data); } catch (e) { setError(getErrorMessage(e)); } finally { setLoading(false); } }
  useEffect(() => { load(); }, []);
  async function action(id, choice) {
    try { await api.put(`/requests/${id}/${choice}`); setSuccess(choice === 'accept' ? 'Request accepted. A delivery task has been created.' : choice === 'reject' ? 'Request rejected.' : 'Request cancelled.'); await load(); }
    catch (e) { setError(getErrorMessage(e)); }
  }
  const donor = user.role === 'DONOR';
  return <div className="page-stack"><section className="page-heading"><div><div className="eyebrow">REQUEST MANAGEMENT</div><h1>{donor ? 'Requests received' : 'My food requests'}</h1><p>{donor ? 'Review organizations requesting your food donations.' : 'Track the requests your organization has submitted.'}</p></div></section>
    {error && <div className="alert alert-error">{error}</div>}{success && <div className="alert alert-success">{success}<button className="alert-close" onClick={() => setSuccess('')}><X size={15}/></button></div>}
    <section className="panel"><div className="panel-head"><div><h2>{donor ? 'Incoming requests' : 'Request history'}</h2><p>{items.length} total records</p></div></div>
      {loading ? <div className="loading-panel"><span className="spinner"/> Loading requests…</div> : items.length ? <div className="table-wrap"><table><thead><tr><th>Food donation</th><th>{donor ? 'NGO' : 'Donor'}</th><th>Quantity</th><th>Message</th><th>Status</th><th>Requested</th>{donor && <th>Action</th>}{!donor && <th>Action</th>}</tr></thead><tbody>{items.map(item => <tr key={item.id}><td><strong>{item.foodName}</strong><small>Donation #{item.donationId}</small></td><td>{donor ? item.ngoName : item.donorName}</td><td>{item.requestedQuantity}</td><td className="table-message">{item.message || '—'}</td><td><span className={statusClass(item.requestStatus)}>{item.requestStatus}</span></td><td>{new Date(item.createdAt).toLocaleDateString()}</td><td>{donor && item.requestStatus === 'PENDING' ? <div className="inline-actions"><button className="btn btn-small btn-success" onClick={() => action(item.id, 'accept')} title="Accept"><Check size={15}/> Accept</button><button className="btn btn-small btn-danger" onClick={() => action(item.id, 'reject')} title="Reject"><X size={15}/> Reject</button></div> : !donor && item.requestStatus === 'PENDING' ? <button className="btn btn-small btn-outline" onClick={() => action(item.id, 'cancel')}>Cancel</button> : <span className="muted">—</span>}</td></tr>)}</tbody></table></div> : <div className="empty-state"><div className="empty-icon"><ClipboardList size={24}/></div><strong>No requests yet</strong><span>{donor ? 'When an NGO requests one of your donations, it will appear here.' : 'Find available food and submit a request to get started.'}</span></div>}
    </section>
  </div>;
}
