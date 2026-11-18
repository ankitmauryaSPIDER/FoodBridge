import { ArrowRight, HeartHandshake, Leaf, MapPin, ShieldCheck, Utensils, Users, Truck } from 'lucide-react';
import { Link } from 'react-router-dom';

const steps = [
  { icon: Utensils, title: 'Share extra food', text: 'Restaurants and individuals list surplus food with pickup details and food-safety information.' },
  { icon: HeartHandshake, title: 'Connect with NGOs', text: 'Verified organizations discover suitable donations and request what their communities need.' },
  { icon: Truck, title: 'Collect and deliver', text: 'Volunteers help coordinate collection and confirm delivery with a secure code.' },
];

export default function LandingPage() {
  return <div className="landing">
    <header className="public-nav container">
      <Link to="/" className="brand"><span className="brand-mark"><Leaf size={21} /></span><span>Food<span className="brand-light">Bridge</span></span></Link>
      <nav><a href="#how-it-works">How it works</a><a href="#impact">Our mission</a><Link to="/login" className="nav-login">Log in</Link><Link to="/register" className="btn btn-primary btn-small">Join FoodBridge <ArrowRight size={16}/></Link></nav>
    </header>
    <main>
      <section className="hero container">
        <div className="hero-copy">
          <div className="eyebrow"><span className="pulse-dot"/> A community-powered food rescue network</div>
          <h1>Share food.<br/><span>Reduce waste.</span><br/>Feed hope.</h1>
          <p className="hero-text">Good food deserves a better destination. FoodBridge connects surplus food from donors with trusted NGOs and volunteers who help get it to people who need it.</p>
          <div className="hero-actions"><Link to="/register" className="btn btn-primary">Start making a difference <ArrowRight size={17}/></Link><Link to="/login" className="btn btn-outline">Explore your dashboard</Link></div>
          <div className="hero-note"><ShieldCheck size={17}/> Built around verified organizations and responsible food handoffs</div>
        </div>
        <div className="hero-art">
          <div className="hero-image-card"><div className="hero-art-top"><span className="tag tag-green">Food rescued, hope shared</span><span className="art-spark">✳</span></div><div className="food-illustration"><div className="food-plate"><div className="food-leaf leaf-one"/><div className="food-leaf leaf-two"/><div className="food-tomato"/><div className="food-carrot"/><div className="food-grain"/></div></div><div className="art-caption"><span className="caption-icon"><HeartHandshake size={19}/></span><span><strong>Small actions. Shared impact.</strong><small>One pickup can make a difference.</small></span><span className="caption-arrow">↗</span></div></div>
          <div className="floating-card floating-top"><span className="float-icon"><Utensils size={17}/></span><span><strong>Surplus food</strong><small>Find a new home</small></span></div>
          <div className="floating-card floating-bottom"><span className="float-icon float-icon-orange"><Users size={17}/></span><span><strong>Local communities</strong><small>Connected through care</small></span></div>
          <div className="hero-orbit orbit-one"/><div className="hero-orbit orbit-two"/>
        </div>
      </section>
      <section className="trust-strip"><div className="container trust-inner"><span><ShieldCheck size={17}/> Verified NGO participation</span><span><MapPin size={17}/> Location-aware discovery</span><span><HeartHandshake size={17}/> Community-first coordination</span></div></section>
      <section className="section container" id="how-it-works"><div className="section-heading"><div className="eyebrow">A simple way to help</div><h2>From extra food to shared good</h2><p>FoodBridge brings donors, NGOs and volunteers together around one clear process.</p></div><div className="steps-grid">{steps.map((step, index) => <article className="step-card" key={step.title}><div className="step-top"><span className="step-icon"><step.icon size={22}/></span><span className="step-number">0{index + 1}</span></div><h3>{step.title}</h3><p>{step.text}</p></article>)}</div></section>
      <section className="impact-band" id="impact"><div className="container impact-content"><div><div className="eyebrow eyebrow-light">The idea behind FoodBridge</div><h2>Less waste.<br/>More care in every connection.</h2><p>FoodBridge is a coordination platform. It helps people find one another, share accurate donation details and complete handoffs responsibly.</p><Link to="/register" className="btn btn-white">Become part of the bridge <ArrowRight size={17}/></Link></div><div className="impact-points"><div><HeartHandshake size={23}/><strong>People helping people</strong><span>Bring local donors and community organizations together.</span></div><div><Leaf size={23}/><strong>Responsible food rescue</strong><span>Promote clear deadlines, storage details and safe handling.</span></div><div><Truck size={23}/><strong>Coordinated handoffs</strong><span>Give volunteers a clear pickup and delivery workflow.</span></div></div></div></section>
    </main>
    <footer className="public-footer"><div className="container footer-inner"><Link to="/" className="brand"><span className="brand-mark"><Leaf size={19}/></span><span>Food<span className="brand-light">Bridge</span></span></Link><span>Share food. Reduce waste. Feed hope.</span><span>© {new Date().getFullYear()} FoodBridge · Student project</span></div></footer>
  </div>;
}
