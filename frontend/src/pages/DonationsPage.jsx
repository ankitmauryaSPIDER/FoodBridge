import { useEffect, useMemo, useState } from 'react';
import { AlertCircle, CalendarClock, CheckCircle2, Clock3, ImagePlus, MapPin, Package, Plus, Search, Utensils, X } from 'lucide-react';
import api, { apiOrigin, getErrorMessage } from '../api';
import { useAuth } from '../context/AuthContext';

function localDateTime(hoursFromNow = 1) {
  const d = new Date(Date.now() + hoursFromNow * 3600000); d.setMinutes(d.getMinutes() - d.getTimezoneOffset());
  return d.toISOString().slice(0, 16);
}
const initialForm = () => ({ foodName: '', description: '', category: 'Prepared meals', quantity: '10', unit: 'packets', foodType: 'VEGETARIAN', preparationTime: localDateTime(-1), useByTime: localDateTime(14), pickupDeadline: localDateTime(8), storageConditions: '', allergenInformation: '', packagingInformation: '', imageUrl: '', address: '', city: '', state: 'Uttar Pradesh', pincode: '', latitude: '', longitude: '', safetyConfirmed: false });
function imageSrc(url) { return url ? (url.startsWith('http') ? url : `${apiOrigin}${url}`) : ''; }
function formatDate(value) { return value ? new Date(value).toLocaleString([], { dateStyle: 'medium', timeStyle: 'short' }) : '—'; }

function DonationCard({ item, action, actionLabel, secondary, secondaryLabel }) {
  return <article className="donation-card">
    <div className="donation-image">{item.imageUrl ? <img src={imageSrc(item.imageUrl)} alt={item.foodName}/> : <div className="food-placeholder"><Utensils size={27}/><span>Food donation</span></div>}<span className={`status-pill status-${item.status.toLowerCase()}`}>{item.status.replace('_', ' ')}</span></div>
    <div className="donation-content"><div className="donation-meta"><span>{item.category || 'Food donation'}</span><span>·</span><span>{item.foodType?.replace('_', ' ')}</span></div><h3>{item.foodName}</h3><p className="donation-description">{item.description || 'A food donation shared with the community.'}</p><div className="donation-facts"><span><Package size={15}/>{item.quantity} {item.unit}</span><span><MapPin size={15}/>{item.city}</span></div><div className="donation-deadline"><Clock3 size={15}/> Pickup by {formatDate(item.pickupDeadline)}</div><a className="map-link" href={`https://www.openstreetmap.org/search?query=${encodeURIComponent(item.address || item.city)}`} target="_blank" rel="noreferrer"><MapPin size={13}/> View area map ↗</a><div className="donation-card-actions">{action && <button className="btn btn-primary btn-small" onClick={action}>{actionLabel}</button>}{secondary && <button className="btn btn-ghost btn-small" onClick={secondary}>{secondaryLabel}</button>}</div></div>
  </article>;
}

export default function DonationsPage() {
  const { user } = useAuth();
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState(initialForm);
  const [saving, setSaving] = useState(false);
  const [query, setQuery] = useState('');
  const [city, setCity] = useState('');
  const [requesting, setRequesting] = useState(null);
  const [requestQty, setRequestQty] = useState('');
  const [requestMessage, setRequestMessage] = useState('');

  async function load() {
    setLoading(true); setError('');
    try { const { data } = await api.get(user.role === 'DONOR' ? '/donations/mine' : '/donations'); setItems(data); }
    catch (err) { setError(getErrorMessage(err)); }
    finally { setLoading(false); }
  }
  useEffect(() => { load(); }, [user.role]);
  const filtered = useMemo(() => items.filter(x => `${x.foodName} ${x.city} ${x.category || ''}`.toLowerCase().includes(query.toLowerCase()) && x.city.toLowerCase().includes(city.toLowerCase())), [items, query, city]);
  const update = e => setForm(f => ({ ...f, [e.target.name]: e.target.type === 'checkbox' ? e.target.checked : e.target.value }));

  async function uploadImage(file) {
    if (!file) return;
    const payload = new FormData(); payload.append('file', file);
    try { const { data } = await api.post('/uploads/image', payload, { headers: { 'Content-Type': 'multipart/form-data' } }); setForm(f => ({ ...f, imageUrl: data.url })); }
    catch (err) { setError(getErrorMessage(err)); }
  }
  async function createDonation(e) {
    e.preventDefault(); setSaving(true); setError(''); setSuccess('');
    try {
      const payload = { ...form, quantity: Number(form.quantity), latitude: form.latitude ? Number(form.latitude) : null, longitude: form.longitude ? Number(form.longitude) : null, safetyConfirmed: true };
      await api.post('/donations', payload); setSuccess('Your food donation has been published.'); setForm(initialForm()); setShowForm(false); await load();
    } catch (err) { setError(getErrorMessage(err)); }
    finally { setSaving(false); }
  }
  async function cancelDonation(id) {
    if (!window.confirm('Cancel this donation?')) return;
    try { await api.delete(`/donations/${id}`); setSuccess('Donation cancelled.'); await load(); }
    catch (err) { setError(getErrorMessage(err)); }
  }
  async function submitRequest(e) {
    e.preventDefault(); if (!requesting) return;
    try { await api.post('/requests', { donationId: requesting.id, requestedQuantity: Number(requestQty), message: requestMessage }); setSuccess('Your request was sent to the donor.'); setRequesting(null); setRequestQty(''); setRequestMessage(''); await load(); }
    catch (err) { setError(getErrorMessage(err)); }
  }

  return <div className="page-stack">
    <section className="page-heading"><div><div className="eyebrow">{user.role === 'DONOR' ? 'DONOR WORKSPACE' : 'COMMUNITY FOOD LISTINGS'}</div><h1>{user.role === 'DONOR' ? 'My donations' : 'Find available food'}</h1><p>{user.role === 'DONOR' ? 'Create a food listing and keep track of every handoff.' : 'Discover food donations shared by restaurants and community members.'}</p></div>{user.role === 'DONOR' && <button className="btn btn-primary" onClick={() => setShowForm(!showForm)}>{showForm ? <X size={17}/> : <Plus size={17}/>} {showForm ? 'Close form' : 'Add food donation'}</button>}</section>
    {error && <div className="alert alert-error"><AlertCircle size={17}/>{error}</div>}{success && <div className="alert alert-success"><CheckCircle2 size={17}/>{success}<button className="alert-close" onClick={() => setSuccess('')}><X size={15}/></button></div>}
    {showForm && user.role === 'DONOR' && <section className="panel form-panel"><div className="panel-head"><div><h2>Create a food donation</h2><p>Provide accurate details so an organization can decide whether it can collect your food.</p></div></div>
      <form onSubmit={createDonation} className="form-grid">
        <label className="span-2">Food name *<input name="foodName" value={form.foodName} onChange={update} placeholder="e.g. Fresh vegetable pulao" required maxLength={120}/></label>
        <label>Description<textarea name="description" value={form.description} onChange={update} placeholder="Describe the food and condition" rows="2"/></label>
        <label>Category<select name="category" value={form.category} onChange={update}><option>Prepared meals</option><option>Fruits</option><option>Vegetables</option><option>Bakery</option><option>Dairy</option><option>Packaged food</option><option>Other</option></select></label>
        <label>Quantity *<input name="quantity" type="number" min="0.1" step="0.1" value={form.quantity} onChange={update} required/></label>
        <label>Unit *<select name="unit" value={form.unit} onChange={update}><option>packets</option><option>meals</option><option>boxes</option><option>kg</option><option>litres</option><option>pieces</option><option>servings</option></select></label>
        <label>Food type<select name="foodType" value={form.foodType} onChange={update}><option value="VEGETARIAN">Vegetarian</option><option value="NON_VEGETARIAN">Non-vegetarian</option><option value="VEGAN">Vegan</option><option value="OTHER">Other</option></select></label>
        <label>Preparation date & time<input type="datetime-local" name="preparationTime" value={form.preparationTime} onChange={update}/></label>
        <label>Pickup deadline *<input type="datetime-local" name="pickupDeadline" value={form.pickupDeadline} onChange={update} required/></label>
        <label>Use-by / expiry time *<input type="datetime-local" name="useByTime" value={form.useByTime} onChange={update} required/></label>
        <label>Storage conditions<input name="storageConditions" value={form.storageConditions} onChange={update} placeholder="e.g. Keep refrigerated"/></label>
        <label>Allergen information<input name="allergenInformation" value={form.allergenInformation} onChange={update} placeholder="e.g. Contains wheat, nuts; or none known"/></label>
        <label>Packaging information<input name="packagingInformation" value={form.packagingInformation} onChange={update} placeholder="e.g. Sealed food-grade containers"/></label>
        <label className="span-2">Pickup address *<input name="address" value={form.address} onChange={update} placeholder="Street, building, area" required/></label>
        <label>City *<input name="city" value={form.city} onChange={update} placeholder="City" required/></label>
        <label>State<input name="state" value={form.state} onChange={update}/></label>
        <label>Pincode<input name="pincode" value={form.pincode} onChange={update} maxLength="12"/></label>
        <label>Food image (optional)<span className="file-field"><ImagePlus size={16}/><input type="file" accept="image/png,image/jpeg,image/webp" onChange={e => uploadImage(e.target.files?.[0])}/></span>{form.imageUrl && <small className="field-help">Image uploaded ✓</small>}</label>
        <div className="span-2 safety-confirm"><input type="checkbox" id="safetyConfirm" checked={form.safetyConfirmed} onChange={update} name="safetyConfirmed" required/><label htmlFor="safetyConfirm">I confirm that the food is suitable for donation to the best of my knowledge, and that the preparation, storage, allergen and deadline details are accurate.</label></div>
        <div className="span-2 form-actions"><button className="btn btn-primary" disabled={saving}>{saving ? 'Publishing…' : 'Publish donation'}</button><button type="button" className="btn btn-outline" onClick={() => setShowForm(false)}>Cancel</button></div>
      </form>
    </section>}
    {user.role === 'NGO' && !user.verified && <div className="alert alert-warning"><Clock3 size={18}/> Your NGO account is pending verification. You can explore listings, but requesting food is disabled until approval.</div>}
    <div className="filter-bar"><div className="search-box"><Search size={17}/><input value={query} onChange={e => setQuery(e.target.value)} placeholder="Search food, category or city"/></div><input className="city-filter" value={city} onChange={e => setCity(e.target.value)} placeholder="Filter by city"/><span className="result-count">{filtered.length} {filtered.length === 1 ? 'listing' : 'listings'}</span></div>
    {loading ? <div className="loading-panel"><span className="spinner"/> Loading donations…</div> : filtered.length ? <div className="donation-grid">{filtered.map(item => <DonationCard key={item.id} item={item} action={user.role === 'NGO' && item.status === 'AVAILABLE' && user.verified ? () => { setRequesting(item); setRequestQty(String(item.quantity)); } : null} actionLabel="Request food" secondary={user.role === 'DONOR' && item.status === 'AVAILABLE' ? () => cancelDonation(item.id) : null} secondaryLabel="Cancel listing" />)}</div> : <div className="empty-state panel"><div className="empty-icon"><Package size={25}/></div><strong>No food listings found</strong><span>{user.role === 'DONOR' ? 'Your donations will appear here after publishing.' : 'Try another search or check back later.'}</span></div>}
    {requesting && <div className="modal-backdrop" onClick={() => setRequesting(null)}><div className="modal-card" onClick={e => e.stopPropagation()}><div className="modal-head"><div><div className="eyebrow">REQUEST FOOD</div><h2>{requesting.foodName}</h2></div><button className="icon-button" onClick={() => setRequesting(null)}><X size={19}/></button></div><form className="form-stack" onSubmit={submitRequest}><label>Requested quantity<input type="number" min="0.1" max={requesting.quantity} step="0.1" value={requestQty} onChange={e => setRequestQty(e.target.value)} required/><small className="field-help">Available: {requesting.quantity} {requesting.unit}</small></label><label>Message to donor<textarea rows="3" value={requestMessage} onChange={e => setRequestMessage(e.target.value)} placeholder="Tell the donor how your organization plans to use this food."/></label><div className="form-actions"><button className="btn btn-primary">Send request</button><button type="button" className="btn btn-outline" onClick={() => setRequesting(null)}>Cancel</button></div></form></div></div>}
  </div>;
}
