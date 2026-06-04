import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { api, money, formatDate } from '../api';
import { useAuth } from '../context/AuthContext';
import { CalendarDays, Loader2, AlertCircle, XCircle, ChevronRight } from 'lucide-react';

const STATUS_COLORS = {
  PENDING: 'status--pending',
  CONFIRMED: 'status--confirmed',
  CHECKED_IN: 'status--active',
  CHECKED_OUT: 'status--done',
  CANCELLED: 'status--cancelled',
  NO_SHOW: 'status--cancelled',
};

export default function MyReservationsPage() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [reservations, setReservations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [cancelling, setCancelling] = useState(null);

  useEffect(() => {
    if (!user) { navigate('/login', { state: { from: '/my-reservations' } }); return; }
    loadReservations();
  }, [user]);

  async function loadReservations() {
    setLoading(true);
    try {
      const data = await api(`/api/reservations/my?userId=${user.userId}`);
      setReservations(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  async function cancelReservation(id) {
    setCancelling(id);
    setError('');
    try {
      await api(`/api/reservations/${id}/cancel`, { method: 'POST' });
      await loadReservations();
    } catch (err) {
      setError(err.message);
    } finally {
      setCancelling(null);
    }
  }

  if (!user) return null;

  return (
    <>
      <section className="page-hero page-hero--short">
        <div className="page-hero__bg" style={{
          backgroundImage: 'url(https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?auto=format&fit=crop&w=1920&q=90)'
        }} />
        <div className="page-hero__overlay" />
        <div className="page-hero__content">
          <span className="label label--gold">Guest Portal</span>
          <h1>My Reservations</h1>
        </div>
      </section>

      <section className="section">
        <div className="container container--narrow">
          {error && <div className="alert alert--error"><AlertCircle size={16} /> {error}</div>}

          {loading ? (
            <div className="loading-state"><Loader2 size={32} className="spin" /><p>Loading reservations...</p></div>
          ) : reservations.length === 0 ? (
            <div className="empty-state">
              <CalendarDays size={48} strokeWidth={1} />
              <h3>No Reservations Yet</h3>
              <p>You haven't made any bookings. Start planning your stay!</p>
              <Link to="/booking" className="btn btn--primary">Book Now <ChevronRight size={16} /></Link>
            </div>
          ) : (
            <div className="reservations-list">
              {reservations.map(r => (
                <article key={r.reservationId} className="reservation-card">
                  <div className="reservation-card__header">
                    <div>
                      <span className="reservation-card__code">{r.reservationCode}</span>
                      <span className={`status-badge ${STATUS_COLORS[r.status] || ''}`}>{r.status}</span>
                    </div>
                    <span className="reservation-card__price">{money(r.totalPrice)}</span>
                  </div>
                  <div className="reservation-card__body">
                    <div className="reservation-card__room">{r.roomTypeName}</div>
                    <div className="reservation-card__dates">
                      <div>
                        <span>Check-in</span>
                        <strong>{formatDate(r.checkInDate)}</strong>
                      </div>
                      <ChevronRight size={16} className="reservation-card__arrow" />
                      <div>
                        <span>Check-out</span>
                        <strong>{formatDate(r.checkOutDate)}</strong>
                      </div>
                    </div>
                    {r.roomNumber && (
                      <div className="reservation-card__assigned">Room {r.roomNumber}</div>
                    )}
                  </div>
                  {(r.status === 'PENDING' || r.status === 'CONFIRMED') && (
                    <div className="reservation-card__actions">
                      <button
                        onClick={() => cancelReservation(r.reservationId)}
                        className="btn btn--danger btn--sm"
                        disabled={cancelling === r.reservationId}
                      >
                        {cancelling === r.id ? <Loader2 size={14} className="spin" /> : <XCircle size={14} />}
                        Cancel Booking
                      </button>
                    </div>
                  )}
                </article>
              ))}
            </div>
          )}
        </div>
      </section>
    </>
  );
}
