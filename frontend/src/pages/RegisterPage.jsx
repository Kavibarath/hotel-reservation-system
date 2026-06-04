import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Mail, Lock, User, Phone, UserPlus, Loader2, AlertCircle, Crown } from 'lucide-react';

export default function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();

  const [form, setForm] = useState({ email: '', password: '', fullName: '', phone: '' });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  function update(field, value) {
    setForm(prev => ({ ...prev, [field]: value }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      await register(form);
      navigate('/booking');
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-page__side">
        <div className="auth-page__brand">
          <Crown size={36} strokeWidth={1.5} />
          <h2>Aurelia Grand</h2>
          <p>Create your account to book luxury rooms, earn loyalty points, and receive exclusive offers.</p>
        </div>
      </div>
      <div className="auth-page__form-side">
        <div className="auth-card">
          <h1>Create Account</h1>
          <p className="auth-card__subtitle">Join us and start your luxury journey</p>

          {error && <div className="alert alert--error"><AlertCircle size={16} /> {error}</div>}

          <form onSubmit={handleSubmit} className="auth-form">
            <label className="form-field">
              <span><User size={16} /> Full Name</span>
              <input type="text" placeholder="John Smith" value={form.fullName}
                onChange={e => update('fullName', e.target.value)} required autoFocus />
            </label>
            <label className="form-field">
              <span><Mail size={16} /> Email</span>
              <input type="email" placeholder="you@example.com" value={form.email}
                onChange={e => update('email', e.target.value)} required />
            </label>
            <label className="form-field">
              <span><Phone size={16} /> Phone</span>
              <input type="tel" placeholder="+94 77 123 4567" value={form.phone}
                onChange={e => update('phone', e.target.value)} required />
            </label>
            <label className="form-field">
              <span><Lock size={16} /> Password</span>
              <input type="password" placeholder="Create a strong password" value={form.password}
                onChange={e => update('password', e.target.value)} required minLength={6} />
            </label>
            <button type="submit" className="btn btn--primary btn--lg btn--full" disabled={loading}>
              {loading ? <Loader2 size={18} className="spin" /> : <UserPlus size={18} />}
              Create Account
            </button>
          </form>

          <p className="auth-card__footer">
            Already have an account? <Link to="/login">Sign in</Link>
          </p>
        </div>
      </div>
    </div>
  );
}
