import { useEffect, useState } from 'react';
import { api, money, formatDate } from '../../api';
import {
  Search, Loader2, DoorOpen, CheckCircle2, XCircle, AlertCircle,
  UserX, RefreshCw, Filter, ArrowRightLeft
} from 'lucide-react';

const STATUS_COLORS = {
  PENDING: 'status--pending',
  CONFIRMED: 'status--confirmed',
  CHECKED_IN: 'status--active',
  CHECKED_OUT: 'status--done',
  CANCELLED: 'status--cancelled',
  NO_SHOW: 'status--cancelled',
};

export default function AdminReservationsPage() {
  const [reservations, setReservations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(null);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [searchCode, setSearchCode] = useState('');
  const [filterStatus, setFilterStatus] = useState('');
  const [changeRoomId, setChangeRoomId] = useState({});

  useEffect(() => { loadReservations(); }, []);

  async function loadReservations() {
    setLoading(true);
    setError('');
    try {
      let url = '/api/admin/reservations';
      if (filterStatus) url += `/filter?status=${filterStatus}`;
      const data = await api(url);
      setReservations(Array.isArray(data) ? data : [data]);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  async function searchByCode() {
    if (!searchCode.trim()) { loadReservations(); return; }
    setLoading(true);
    setError('');
    try {
      const data = await api(`/api/admin/reservations/search?reservationCode=${encodeURIComponent(searchCode.trim())}`);
      setReservations(data ? [data] : []);
    } catch (err) {
      setError(err.message);
      setReservations([]);
    } finally {
      setLoading(false);
    }
  }

  async function performAction(id, action, extraParams = '') {
    setActionLoading(`${id}-${action}`);
    setError('');
    setSuccess('');
    try {
      await api(`/api/admin/reservations/${id}/${action}${extraParams}`, { method: 'POST' });
      setSuccess(`${action.replace('-', ' ')} completed successfully`);
      await loadReservations();
    } catch (err) {
      setError(err.message);
    } finally {
      setActionLoading(null);
    }
  }

  function isActionLoading(id, action) {
    return actionLoading === `${id}-${action}`;
  }

  return (
    <div className="admin-page">
      <div className="admin-page__header">
        <div>
          <h1>Reservations</h1>
          <p>Manage guest bookings, check-in/out, and handle changes</p>
        </div>
        <button onClick={loadReservations} className="btn btn--outline btn--sm">
          <RefreshCw size={16} /> Refresh
        </button>
      </div>

      {/* Search and Filter Bar */}
      <div className="admin-toolbar">
        <div className="admin-toolbar__search">
          <Search size={16} />
          <input type="text" placeholder="Search by reservation code..."
            value={searchCode} onChange={e => setSearchCode(e.target.value)}
            onKeyDown={e => e.key === 'Enter' && searchByCode()} />
          <button onClick={searchByCode} className="btn btn--primary btn--sm">Search</button>
        </div>
        <div className="admin-toolbar__filter">
          <Filter size={16} />
          <select value={filterStatus} onChange={e => { setFilterStatus(e.target.value); }}>
            <option value="">All Statuses</option>
            <option value="PENDING">Pending</option>
            <option value="CONFIRMED">Confirmed</option>
            <option value="CHECKED_IN">Checked In</option>
            <option value="CHECKED_OUT">Checked Out</option>
            <option value="CANCELLED">Cancelled</option>
            <option value="NO_SHOW">No Show</option>
          </select>
          <button onClick={loadReservations} className="btn btn--outline btn--sm">Apply</button>
        </div>
      </div>

      {error && <div className="alert alert--error"><AlertCircle size={16} /> {error}</div>}
      {success && <div className="alert alert--success"><CheckCircle2 size={16} /> {success}</div>}

      {loading ? (
        <div className="loading-state"><Loader2 size={32} className="spin" /></div>
      ) : (
        <div className="admin-table">
          <div className="admin-table__header">
            <span>Code</span>
            <span>Guest</span>
            <span>Room Type</span>
            <span>Dates</span>
            <span>Room</span>
            <span>Amount</span>
            <span>Status</span>
            <span>Actions</span>
          </div>
          {reservations.map(r => (
            <div key={r.reservationId} className="admin-table__row">
              <span className="admin-table__code">{r.reservationCode}</span>
              <span>{r.guestName || '—'}</span>
              <span>{r.roomTypeName}</span>
              <span className="admin-table__dates">
                {formatDate(r.checkInDate)}<br />{formatDate(r.checkOutDate)}
              </span>
              <span>{r.roomNumber || '—'}</span>
              <span className="admin-table__amount">{money(r.totalPrice)}</span>
              <span><span className={`status-badge ${STATUS_COLORS[r.status] || ''}`}>{r.status}</span></span>
              <div className="admin-table__actions">
                {r.status === 'CONFIRMED' && (
                  <button onClick={() => performAction(r.reservationId, 'check-in')}
                    className="btn btn--success btn--xs" disabled={isActionLoading(r.reservationId, 'check-in')}
                    title="Check In">
                    {isActionLoading(r.reservationId, 'check-in') ? <Loader2 size={14} className="spin" /> : <DoorOpen size={14} />}
                    Check In
                  </button>
                )}
                {r.status === 'CHECKED_IN' && (
                  <>
                    <button onClick={() => performAction(r.reservationId, 'check-out')}
                      className="btn btn--primary btn--xs" disabled={isActionLoading(r.reservationId, 'check-out')}
                      title="Check Out">
                      {isActionLoading(r.reservationId, 'check-out') ? <Loader2 size={14} className="spin" /> : <CheckCircle2 size={14} />}
                      Check Out
                    </button>
                    <div className="change-room-inline">
                      <input type="number" placeholder="Room ID"
                        value={changeRoomId[r.reservationId] || ''}
                        onChange={e => setChangeRoomId(prev => ({ ...prev, [r.reservationId]: e.target.value }))}
                        className="input--xs" />
                      <button onClick={() => performAction(r.reservationId, 'change-room', `?newRoomId=${changeRoomId[r.reservationId]}`)}
                        className="btn btn--outline btn--xs" title="Change Room"
                        disabled={!changeRoomId[r.reservationId]}>
                        <ArrowRightLeft size={14} />
                      </button>
                    </div>
                  </>
                )}
                {(r.status === 'PENDING' || r.status === 'CONFIRMED') && (
                  <button onClick={() => performAction(r.reservationId, 'cancel')}
                    className="btn btn--danger btn--xs" disabled={isActionLoading(r.reservationId, 'cancel')}
                    title="Cancel">
                    <XCircle size={14} /> Cancel
                  </button>
                )}
                {r.status === 'CONFIRMED' && (
                  <button onClick={() => performAction(r.reservationId, 'no-show')}
                    className="btn btn--warning btn--xs" disabled={isActionLoading(r.reservationId, 'no-show')}
                    title="No Show">
                    <UserX size={14} /> No Show
                  </button>
                )}
              </div>
            </div>
          ))}
          {reservations.length === 0 && (
            <div className="admin-table__empty">No reservations found</div>
          )}
        </div>
      )}
    </div>
  );
}
