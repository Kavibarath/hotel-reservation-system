import { useEffect, useState } from 'react';
import { api, money } from '../../api';
import {
  Plus, Loader2, AlertCircle, CheckCircle2, Trash2, Edit3,
  DollarSign, RefreshCw
} from 'lucide-react';

export default function RatePlansPage() {
  const [plans, setPlans] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ name: '', description: '', priceModifier: 0, cancellationPolicy: '' });
  const [editingId, setEditingId] = useState(null);

  useEffect(() => {
    loadPlans();
  }, []);

  async function loadPlans() {
    setLoading(true);
    try {
      const data = await api('/api/admin/catalog/rate-plans');
      setPlans(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    setSuccess('');
    try {
      const payload = { ...form, priceModifier: Number(form.priceModifier) };
      if (editingId) {
        await api(`/api/admin/catalog/rate-plans/${editingId}`, { method: 'PUT', body: JSON.stringify(payload) });
        setSuccess('Rate plan updated');
      } else {
        await api('/api/admin/catalog/rate-plans', { method: 'POST', body: JSON.stringify(payload) });
        setSuccess('Rate plan created');
      }
      setForm({ name: '', description: '', priceModifier: 0, cancellationPolicy: '' });
      setEditingId(null);
      setShowForm(false);
      await loadPlans();
    } catch (err) {
      setError(err.message);
    }
  }

  function startEdit(plan) {
    setForm({
      name: plan.name,
      description: plan.description || '',
      priceModifier: plan.priceModifier,
      cancellationPolicy: plan.cancellationPolicy || '',
    });
    setEditingId(plan.id);
    setShowForm(true);
  }

  async function deletePlan(id) {
    setError('');
    try {
      await api(`/api/admin/catalog/rate-plans/${id}`, { method: 'DELETE' });
      setSuccess('Rate plan deleted');
      await loadPlans();
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <div className="admin-page">
      <div className="admin-page__header">
        <div>
          <h1>Rate Plans</h1>
          <p>Configure pricing packages and rate plans for guests</p>
        </div>
        <div className="admin-page__header-actions">
          <button onClick={() => { setShowForm(!showForm); setEditingId(null); }} className="btn btn--primary btn--sm">
            <Plus size={16} /> Add Rate Plan
          </button>
          <button onClick={loadPlans} className="btn btn--outline btn--sm">
            <RefreshCw size={16} />
          </button>
        </div>
      </div>

      {error && <div className="alert alert--error"><AlertCircle size={16} /> {error}</div>}
      {success && <div className="alert alert--success"><CheckCircle2 size={16} /> {success}</div>}

      {showForm && (
        <form onSubmit={handleSubmit} className="admin-form">
          <h3>{editingId ? 'Edit Rate Plan' : 'Create Rate Plan'}</h3>
          <div className="admin-form__grid">
            <label className="form-field">
              <span>Plan Name</span>
              <input type="text" value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} required
                placeholder="e.g. Bed & Breakfast" />
            </label>
            <label className="form-field">
              <span>Price Modifier ($)</span>
              <input type="number" step="0.01" value={form.priceModifier}
                onChange={e => setForm({ ...form, priceModifier: e.target.value })} required />
            </label>
            <label className="form-field form-field--wide">
              <span>Description</span>
              <input type="text" value={form.description} onChange={e => setForm({ ...form, description: e.target.value })}
                placeholder="Brief description of what's included" />
            </label>
            <label className="form-field form-field--wide">
              <span>Cancellation Policy</span>
              <input type="text" value={form.cancellationPolicy}
                onChange={e => setForm({ ...form, cancellationPolicy: e.target.value })}
                placeholder="e.g. Free cancellation before 48 hours" />
            </label>
          </div>
          <div className="admin-form__actions">
            <button type="submit" className="btn btn--primary btn--sm">
              {editingId ? 'Update' : 'Create'}
            </button>
            <button type="button" onClick={() => { setShowForm(false); setEditingId(null); }} className="btn btn--outline btn--sm">
              Cancel
            </button>
          </div>
        </form>
      )}

      {loading ? (
        <div className="loading-state"><Loader2 size={32} className="spin" /></div>
      ) : (
        <div className="rate-plans-mgmt">
          {plans.map(plan => (
            <div key={plan.id} className="rate-plan-mgmt-card">
              <div className="rate-plan-mgmt-card__icon">
                <DollarSign size={24} />
              </div>
              <div className="rate-plan-mgmt-card__content">
                <h3>{plan.name}</h3>
                {plan.description && <p>{plan.description}</p>}
                <div className="rate-plan-mgmt-card__meta">
                  <span className="rate-plan-mgmt-card__price">+{money(plan.priceModifier)}</span>
                  {plan.cancellationPolicy && <span>{plan.cancellationPolicy}</span>}
                </div>
              </div>
              <div className="rate-plan-mgmt-card__actions">
                <button onClick={() => startEdit(plan)} className="btn btn--outline btn--xs"><Edit3 size={14} /> Edit</button>
                <button onClick={() => deletePlan(plan.id)} className="btn btn--danger btn--xs"><Trash2 size={14} /> Delete</button>
              </div>
            </div>
          ))}
          {plans.length === 0 && (
            <div className="empty-state">
              <DollarSign size={48} strokeWidth={1} />
              <h3>No Rate Plans</h3>
              <p>Create your first rate plan to offer packages to guests.</p>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
