import { useState } from 'react';
import { ArrowLeft, Eye, EyeOff, HeartHandshake, Leaf, LockKeyhole } from 'lucide-react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { getErrorMessage } from '../api';

export default function AuthPage({ mode }) {
  const isRegister = mode === 'register';
  const navigate = useNavigate();
  const { login, register } = useAuth();
  const [showPassword, setShowPassword] = useState(false);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [form, setForm] = useState({ name: '', email: '', password: '', phone: '', role: 'DONOR', organizationName: '', address: '', city: '', state: 'Uttar Pradesh', pincode: '' });
  const update = (e) => setForm({ ...form, [e.target.name]: e.target.value });
  async function submit(e) {
    e.preventDefault(); setBusy(true); setError('');
    try {
      if (isRegister) {
        const user = await register(form);
        navigate('/app', { replace: true, state: { welcome: user.role === 'NGO' && !user.verified ? 'Your NGO profile has been submitted for admin verification.' : 'Welcome to FoodBridge!' } });
      } else { await login(form.email, form.password); navigate('/app', { replace: true }); }
    } catch (err) { setError(getErrorMessage(err)); }
    finally { setBusy(false); }
  }
  return <div className="auth-page"><div className="auth-left"><Link to="/" className="brand brand-white"><span className="brand-mark"><Leaf size={21}/></span><span>Food<span className="brand-light">Bridge</span></span></Link><div className="auth-message"><div className="auth-illustration"><HeartHandshake size={42}/></div><div className="eyebrow eyebrow-light">A better destination for extra food</div><h1>Every meal shared is a little more hope.</h1><p>Connect with neighbors, community kitchens and volunteers working to make food rescue more coordinated.</p></div><div className="auth-left-bottom">Built for communities, powered by care.</div></div><div className="auth-right"><div className="auth-form-wrap"><Link to="/" className="back-link"><ArrowLeft size={16}/> Back to home</Link><div className="auth-heading"><div className="eyebrow">{isRegister ? 'Join the community' : 'Welcome back'}</div><h2>{isRegister ? 'Create your account' : 'Sign in to FoodBridge'}</h2><p>{isRegister ? 'Choose how you would like to contribute.' : 'Enter your account details to continue.'}</p></div>
    <form onSubmit={submit} className="form-stack">
      {isRegister && <><label>Full name<input name="name" value={form.name} onChange={update} placeholder="Your name" required minLength={2}/></label><label>Account type<select name="role" value={form.role} onChange={update}><option value="DONOR">Food donor</option><option value="NGO">NGO / community organization</option><option value="VOLUNTEER">Volunteer</option></select></label>{form.role === 'NGO' && <label>Organization name<input name="organizationName" value={form.organizationName} onChange={update} placeholder="Registered organization name" required/></label>}<div className="form-row"><label>Phone<input name="phone" value={form.phone} onChange={update} placeholder="Mobile number"/></label><label>City<input name="city" value={form.city} onChange={update} placeholder="City"/></label></div>{form.role !== 'VOLUNTEER' && <label>Address<input name="address" value={form.address} onChange={update} placeholder="Pickup / organization address"/></label>}</>}
      <label>Email address<input type="email" name="email" value={form.email} onChange={update} placeholder="name@example.com" required autoComplete="email"/></label>
      <label>Password<div className="password-wrap"><input type={showPassword ? 'text' : 'password'} name="password" value={form.password} onChange={update} placeholder={isRegister ? 'At least 8 characters' : 'Enter your password'} required minLength={isRegister ? 8 : 1} autoComplete={isRegister ? 'new-password' : 'current-password'}/><button type="button" className="icon-button" onClick={() => setShowPassword(!showPassword)} aria-label="Toggle password visibility">{showPassword ? <EyeOff size={17}/> : <Eye size={17}/>}</button></div></label>
      {error && <div className="alert alert-error">{error}</div>}
      {isRegister && form.role === 'NGO' && <div className="alert alert-info"><LockKeyhole size={16}/> NGO accounts need admin verification before they can request donations.</div>}
      <button className="btn btn-primary btn-full" disabled={busy}>{busy ? 'Please wait…' : isRegister ? 'Create account' : 'Sign in'} {!busy && <span>→</span>}</button>
    </form>
    {!isRegister && <div className="demo-note"><strong>Try the demo accounts</strong><span>Donor, NGO, volunteer and admin logins are listed in the README file.</span></div>}
    <p className="auth-switch">{isRegister ? 'Already have an account?' : 'New to FoodBridge?'} <Link to={isRegister ? '/login' : '/register'}>{isRegister ? 'Sign in' : 'Create an account'}</Link></p>
    </div></div></div>;
}
