import { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Mail, Lock, LogIn, Loader2, AlertCircle, Crown } from 'lucide-react';

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const from = location.state?.from || '/';

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  async function handleSubmit(e) {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      await login(email, password);
      navigate(from, { replace: true });
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
          <p>Welcome back. Sign in to manage your reservations and enjoy exclusive member benefits.</p>
        </div>
      </div>
      <div className="auth-page__form-side">
        <div className="auth-card">
          <h1>Sign In</h1>
          <p className="auth-card__subtitle">Enter your credentials to access your account</p>

          {error && <div className="alert alert--error"><AlertCircle size={16} /> {error}</div>}

          <form onSubmit={handleSubmit} className="auth-form">
            <label className="form-field">
              <span><Mail size={16} /> Email</span>
              <input type="email" placeholder="you@example.com" value={email}
                onChange={e => setEmail(e.target.value)} required autoFocus />
            </label>
            <label className="form-field">
              <span><Lock size={16} /> Password</span>
              <input type="password" placeholder="Enter your password" value={password}
                onChange={e => setPassword(e.target.value)} required />
            </label>
            <button type="submit" className="btn btn--primary btn--lg btn--full" disabled={loading}>
              {loading ? <Loader2 size={18} className="spin" /> : <LogIn size={18} />}
              Sign In
            </button>
          </form>

          <p className="auth-card__footer">
            Don't have an account? <Link to="/register">Create one</Link>
          </p>

          <div className="auth-card__demo">
            <p>Demo accounts:</p>
            <div className="auth-card__demo-accounts">
              <button type="button" onClick={() => { setEmail('guest@example.com'); setPassword('123456'); }}
                className="btn btn--outline btn--sm">Guest Login</button>
              <button type="button" onClick={() => { setEmail('manager@example.com'); setPassword('Admin123!'); }}
                className="btn btn--outline btn--sm">Admin Login</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
