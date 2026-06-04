import { useEffect, useState } from 'react';
import { api } from '../../api';
import {
  Plus, Loader2, AlertCircle, CheckCircle2, Trash2, Edit3,
  BedDouble, RefreshCw, Wrench, Check
} from 'lucide-react';

const STATUS_COLORS = {
  AVAILABLE: 'status--confirmed',
  OCCUPIED: 'status--active',
  MAINTENANCE: 'status--pending',
  OUT_OF_SERVICE: 'status--cancelled',
  RESERVED: 'status--pending',
};

export default function RoomsManagementPage() {
  const [rooms, setRooms] = useState([]);
  const [roomTypes, setRoomTypes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ roomNumber: '', floor: 1, roomTypeId: '', status: 'AVAILABLE' });
  const [editingId, setEditingId] = useState(null);

  useEffect(() => {
    Promise.all([api('/api/admin/catalog/rooms'), api('/api/room-types')])
      .then(([r, rt]) => { setRooms(r); setRoomTypes(rt); if (rt[0]) setForm(f => ({ ...f, roomTypeId: String(rt[0].id) })); })
      .catch(err => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  async function reload() {
    const r = await api('/api/admin/catalog/rooms');
    setRooms(r);
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    setSuccess('');
    try {
      const payload = { ...form, floor: Number(form.floor), roomTypeId: Number(form.roomTypeId) };
      if (editingId) {
        await api(`/api/admin/catalog/rooms/${editingId}`, { method: 'PUT', body: JSON.stringify(payload) });
        setSuccess('Room updated');
      } else {
        await api('/api/admin/catalog/rooms', { method: 'POST', body: JSON.stringify(payload) });
        setSuccess('Room created');
      }
      setForm({ roomNumber: '', floor: 1, roomTypeId: String(roomTypes[0]?.id || ''), status: 'AVAILABLE' });
      setEditingId(null);
      setShowForm(false);
      await reload();
    } catch (err) {
      setError(err.message);
    }
  }

  function startEdit(room) {
    setForm({ roomNumber: room.roomNumber, floor: room.floor, roomTypeId: String(room.roomTypeId), status: room.status });
    setEditingId(room.id);
    setShowForm(true);
  }

  async function deleteRoom(id) {
    setError('');
    try {
      await api(`/api/admin/catalog/rooms/${id}`, { method: 'DELETE' });
      setSuccess('Room deleted');
      await reload();
    } catch (err) {
      setError(err.message);
    }
  }

  async function toggleMaintenance(room) {
    setError('');
    try {
      if (room.status === 'MAINTENANCE') {
        await api(`/api/admin/rooms/${room.id}/mark-available`, { method: 'POST' });
      } else {
        await api(`/api/admin/rooms/${room.id}/mark-maintenance`, { method: 'POST' });
      }
      setSuccess(`Room ${room.roomNumber} status updated`);
      await reload();
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <div className="admin-page">
      <div className="admin-page__header">
        <div>
          <h1>Room Management</h1>
          <p>Manage physical room inventory and status</p>
        </div>
        <div className="admin-page__header-actions">
          <button onClick={() => { setShowForm(!showForm); setEditingId(null); }} className="btn btn--primary btn--sm">
            <Plus size={16} /> Add Room
          </button>
          <button onClick={reload} className="btn btn--outline btn--sm">
            <RefreshCw size={16} />
          </button>
        </div>
      </div>

      {error && <div className="alert alert--error"><AlertCircle size={16} /> {error}</div>}
      {success && <div className="alert alert--success"><CheckCircle2 size={16} /> {success}</div>}

      {showForm && (
        <form onSubmit={handleSubmit} className="admin-form">
          <h3>{editingId ? 'Edit Room' : 'Add New Room'}</h3>
          <div className="admin-form__grid">
            <label className="form-field">
              <span>Room Number</span>
              <input type="text" value={form.roomNumber} onChange={e => setForm({ ...form, roomNumber: e.target.value })} required />
            </label>
            <label className="form-field">
              <span>Floor</span>
              <input type="number" min="1" value={form.floor} onChange={e => setForm({ ...form, floor: e.target.value })} required />
            </label>
            <label className="form-field">
              <span>Room Type</span>
              <select value={form.roomTypeId} onChange={e => setForm({ ...form, roomTypeId: e.target.value })}>
                {roomTypes.map(rt => <option key={rt.id} value={rt.id}>{rt.name}</option>)}
              </select>
            </label>
            <label className="form-field">
              <span>Status</span>
              <select value={form.status} onChange={e => setForm({ ...form, status: e.target.value })}>
                <option value="AVAILABLE">Available</option>
                <option value="MAINTENANCE">Maintenance</option>
                <option value="OUT_OF_SERVICE">Out of Service</option>
              </select>
            </label>
          </div>
          <div className="admin-form__actions">
            <button type="submit" className="btn btn--primary btn--sm">
              {editingId ? 'Update' : 'Create'} Room
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
        <div className="rooms-grid">
          {rooms.map(room => (
            <div key={room.id} className="room-mgmt-card">
              <div className="room-mgmt-card__header">
                <div className="room-mgmt-card__number">
                  <BedDouble size={18} />
                  <strong>{room.roomNumber}</strong>
                </div>
                <span className={`status-badge ${STATUS_COLORS[room.status] || ''}`}>{room.status}</span>
              </div>
              <div className="room-mgmt-card__body">
                <span>{room.roomTypeName}</span>
                <span>Floor {room.floor}</span>
              </div>
              <div className="room-mgmt-card__actions">
                <button onClick={() => startEdit(room)} className="btn btn--outline btn--xs" title="Edit">
                  <Edit3 size={14} />
                </button>
                <button onClick={() => toggleMaintenance(room)}
                  className="btn btn--outline btn--xs"
                  title={room.status === 'MAINTENANCE' ? 'Mark Available' : 'Mark Maintenance'}>
                  {room.status === 'MAINTENANCE' ? <Check size={14} /> : <Wrench size={14} />}
                </button>
                <button onClick={() => deleteRoom(room.id)} className="btn btn--danger btn--xs" title="Delete">
                  <Trash2 size={14} />
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
