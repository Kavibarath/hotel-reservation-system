import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api, money, nightsBetween, todayStr, addDays, formatDate } from '../api';
import { useAuth } from '../context/AuthContext';
import {
  CalendarDays, Users, BedDouble, CreditCard, Search,
  Loader2, CheckCircle2, ChevronRight, AlertCircle, Shield
} from 'lucide-react';

const ROOM_IMAGES = [
  'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&w=800&q=85',
  'https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=800&q=85',
  'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?auto=format&fit=crop&w=800&q=85',
];

export default function BookingPage() {
  const { user } = useAuth();
  const navigate = useNavigate();

  const [step, setStep] = useState(1);
  const [roomTypes, setRoomTypes] = useState([]);
  const [ratePlans, setRatePlans] = useState([]);
  const [availability, setAvailability] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const [search, setSearch] = useState({
    checkInDate: todayStr(),
    checkOutDate: addDays(todayStr(), 2),
    guests: 2,
    roomTypeId: '',
  });
  const [selectedResult, setSelectedResult] = useState(null);
  const [selectedRatePlan, setSelectedRatePlan] = useState('');
  const [confirmation, setConfirmation] = useState(null);

  useEffect(() => {
    Promise.all([api('/api/room-types'), api('/api/rate-plans')])
      .then(([rt, rp]) => { setRoomTypes(rt); setRatePlans(rp); })
      .catch(() => {});
  }, []);

  async function searchAvailability(e) {
    e.preventDefault();
    if (!user) {
      navigate('/login', { state: { from: '/booking' } });
      return;
    }
    setLoading(true);
    setError('');
    setAvailability([]);
    setSelectedResult(null);
    try {
      const payload = {
        ...search,
        guests: Number(search.guests),
        roomTypeId: search.roomTypeId ? Number(search.roomTypeId) : null,
      };
      const results = await api('/api/search-availability', {
        method: 'POST',
        body: JSON.stringify(payload),
      });
      setAvailability(results);
      if (results.length > 0) setStep(2);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  function selectRoom(result) {
    setSelectedResult(result);
    setStep(3);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  async function confirmBooking() {
    if (!user) {
      navigate('/login', { state: { from: '/booking' } });
      return;
    }
    setLoading(true);
    setError('');
    try {
      const reservation = await api('/api/reservations', {
        method: 'POST',
        body: JSON.stringify({
          roomTypeId: selectedResult.roomTypeId,
          checkInDate: search.checkInDate,
          checkOutDate: search.checkOutDate,
          guests: Number(search.guests),
          userId: user.userId,
          ratePlanId: selectedRatePlan ? Number(selectedRatePlan) : null,
        }),
      });

      const payment = await api(
        `/api/payments/initiate?reservationId=${reservation.reservationId}&amount=${reservation.totalPrice}&paymentMethod=CARD`,
        { method: 'POST' },
      );

      await api(
        `/api/payments/confirm?transactionRef=${encodeURIComponent(payment.transactionRef)}`,
        { method: 'POST' },
      );

      setConfirmation(reservation);
      setStep(4);
      window.scrollTo({ top: 0, behavior: 'smooth' });
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  const nights = nightsBetween(search.checkInDate, search.checkOutDate);
  const selectedPlan = ratePlans.find(p => String(p.id) === selectedRatePlan);

  return (
    <>
      <section className="page-hero page-hero--short">
        <div className="page-hero__bg" style={{
          backgroundImage: `url(https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=1920&q=90)`
        }} />
        <div className="page-hero__overlay" />
        <div className="page-hero__content">
          <span className="label label--gold">Reservations</span>
          <h1>Book Your Stay</h1>
        </div>
      </section>

      {/* Progress Steps */}
      <div className="booking-progress">
        <div className="container">
          <div className="booking-progress__steps">
            {['Search', 'Select Room', 'Review & Pay', 'Confirmation'].map((s, i) => (
              <div key={i} className={`booking-step ${step > i + 1 ? 'booking-step--done' : ''} ${step === i + 1 ? 'booking-step--active' : ''}`}>
                <div className="booking-step__num">{step > i + 1 ? <CheckCircle2 size={18} /> : i + 1}</div>
                <span>{s}</span>
              </div>
            ))}
          </div>
        </div>
      </div>

      <section className="section booking-section">
        <div className="container">
          {error && (
            <div className="alert alert--error">
              <AlertCircle size={18} /> {error}
            </div>
          )}

          {/* Step 1: Search */}
          {step === 1 && (
            <div className="booking-search">
              <div className="booking-search__card">
                <h2>Find Your Perfect Room</h2>
                <p>Select your dates and preferences to check availability.</p>
                <form onSubmit={searchAvailability} className="booking-form">
                  <div className="booking-form__grid">
                    <label className="form-field">
                      <span><CalendarDays size={16} /> Check-in</span>
                      <input type="date" min={todayStr()} value={search.checkInDate}
                        onChange={e => setSearch({ ...search, checkInDate: e.target.value })} required />
                    </label>
                    <label className="form-field">
                      <span><CalendarDays size={16} /> Check-out</span>
                      <input type="date" min={search.checkInDate || todayStr()} value={search.checkOutDate}
                        onChange={e => setSearch({ ...search, checkOutDate: e.target.value })} required />
                    </label>
                    <label className="form-field">
                      <span><Users size={16} /> Guests</span>
                      <input type="number" min="1" max="10" value={search.guests}
                        onChange={e => setSearch({ ...search, guests: e.target.value })} required />
                    </label>
                    <label className="form-field">
                      <span><BedDouble size={16} /> Room Type</span>
                      <select value={search.roomTypeId}
                        onChange={e => setSearch({ ...search, roomTypeId: e.target.value })}>
                        <option value="">All Room Types</option>
                        {roomTypes.map(rt => (
                          <option key={rt.id} value={rt.id}>{rt.name}</option>
                        ))}
                      </select>
                    </label>
                  </div>
                  <button type="submit" className="btn btn--primary btn--lg btn--full" disabled={loading}>
                    {loading ? <Loader2 size={18} className="spin" /> : <Search size={18} />}
                    Check Availability
                  </button>
                </form>
              </div>
            </div>
          )}

          {/* Step 2: Select Room */}
          {step === 2 && (
            <div className="booking-results">
              <div className="booking-results__header">
                <div>
                  <h2>Available Rooms</h2>
                  <p>{formatDate(search.checkInDate)} — {formatDate(search.checkOutDate)} · {nights} night{nights !== 1 ? 's' : ''} · {search.guests} guest{search.guests > 1 ? 's' : ''}</p>
                </div>
                <button onClick={() => setStep(1)} className="btn btn--outline btn--sm">Modify Search</button>
              </div>

              {/* Rate Plan Selection */}
              <div className="rate-plan-select">
                <label className="form-field">
                  <span><CreditCard size={16} /> Rate Plan</span>
                  <select value={selectedRatePlan} onChange={e => setSelectedRatePlan(e.target.value)}>
                    <option value="">Room Only (Base Rate)</option>
                    {ratePlans.map(p => (
                      <option key={p.id} value={p.id}>{p.name} (+{money(p.priceModifier)})</option>
                    ))}
                  </select>
                </label>
              </div>

              <div className="booking-results__grid">
                {availability.map((result, i) => {
                  const rt = roomTypes.find(r => r.id === result.roomTypeId);
                  return (
                    <article key={result.roomTypeId} className="result-card">
                      <img src={ROOM_IMAGES[i % ROOM_IMAGES.length]} alt={result.roomTypeName} />
                      <div className="result-card__body">
                        <div>
                          <h3>{result.roomTypeName}</h3>
                          <p>{rt?.description || 'Luxury accommodation with premium amenities.'}</p>
                          <div className="result-card__meta">
                            <span><Users size={14} /> Up to {rt?.maxOccupancy || 2}</span>
                            <span><BedDouble size={14} /> {rt?.bedType || 'King'}</span>
                            {rt?.sizeSqm && <span>{rt.sizeSqm} sqm</span>}
                          </div>
                        </div>
                        <div className="result-card__footer">
                          <div className="result-card__price">
                            <span>{nights} night{nights !== 1 ? 's' : ''}</span>
                            <strong>{money(result.totalPrice)}</strong>
                          </div>
                          <button
                            className="btn btn--primary"
                            disabled={!result.available}
                            onClick={() => selectRoom(result)}
                          >
                            {result.available ? <>Select <ChevronRight size={16} /></> : 'Sold Out'}
                          </button>
                        </div>
                      </div>
                    </article>
                  );
                })}
                {availability.length === 0 && (
                  <div className="empty-state">
                    <p>No rooms available for your selected dates. Try different dates or room type.</p>
                  </div>
                )}
              </div>
            </div>
          )}

          {/* Step 3: Review & Pay */}
          {step === 3 && selectedResult && (
            <div className="booking-review">
              <div className="booking-review__grid">
                <div className="booking-review__summary">
                  <h2>Booking Summary</h2>
                  <div className="summary-card">
                    <img src={ROOM_IMAGES[0]} alt={selectedResult.roomTypeName} />
                    <div className="summary-card__details">
                      <h3>{selectedResult.roomTypeName}</h3>
                      <div className="summary-card__dates">
                        <div>
                          <span>Check-in</span>
                          <strong>{formatDate(search.checkInDate)}</strong>
                        </div>
                        <div>
                          <span>Check-out</span>
                          <strong>{formatDate(search.checkOutDate)}</strong>
                        </div>
                      </div>
                      <div className="summary-card__line">
                        <span>Duration</span>
                        <span>{nights} night{nights !== 1 ? 's' : ''}</span>
                      </div>
                      <div className="summary-card__line">
                        <span>Guests</span>
                        <span>{search.guests}</span>
                      </div>
                      {selectedPlan && (
                        <div className="summary-card__line">
                          <span>Rate Plan</span>
                          <span>{selectedPlan.name} (+{money(selectedPlan.priceModifier)})</span>
                        </div>
                      )}
                      <div className="summary-card__total">
                        <span>Total</span>
                        <strong>{money(selectedResult.totalPrice)}</strong>
                      </div>
                    </div>
                  </div>
                </div>

                <div className="booking-review__payment">
                  <h2>Payment</h2>
                  <div className="payment-card">
                    <div className="payment-card__secure">
                      <Shield size={18} /> Secure Payment
                    </div>
                    <p>Your booking will be confirmed immediately upon payment. We accept all major credit and debit cards.</p>
                    <div className="payment-card__method">
                      <CreditCard size={20} />
                      <span>Credit / Debit Card</span>
                    </div>
                    <div className="payment-card__actions">
                      <button onClick={() => setStep(2)} className="btn btn--outline">Back</button>
                      <button onClick={confirmBooking} className="btn btn--primary btn--lg" disabled={loading}>
                        {loading ? <Loader2 size={18} className="spin" /> : null}
                        Confirm & Pay {money(selectedResult.totalPrice)}
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* Step 4: Confirmation */}
          {step === 4 && confirmation && (
            <div className="booking-confirmation">
              <div className="confirmation-card">
                <div className="confirmation-card__icon">
                  <CheckCircle2 size={56} />
                </div>
                <h2>Booking Confirmed!</h2>
                <p>Your reservation has been successfully confirmed. A confirmation email will be sent shortly.</p>
                <div className="confirmation-card__details">
                  <div className="confirmation-card__code">
                    <span>Reservation Code</span>
                    <strong>{confirmation.reservationCode}</strong>
                  </div>
                  <div className="confirmation-card__grid">
                    <div><span>Room</span><strong>{selectedResult?.roomTypeName}</strong></div>
                    <div><span>Check-in</span><strong>{formatDate(search.checkInDate)}</strong></div>
                    <div><span>Check-out</span><strong>{formatDate(search.checkOutDate)}</strong></div>
                    <div><span>Total Paid</span><strong>{money(confirmation.totalPrice)}</strong></div>
                  </div>
                </div>
                <div className="confirmation-card__actions">
                  <button onClick={() => navigate('/my-reservations')} className="btn btn--primary">
                    View My Bookings
                  </button>
                  <button onClick={() => { setStep(1); setConfirmation(null); setSelectedResult(null); }} className="btn btn--outline">
                    Book Another Room
                  </button>
                </div>
              </div>
            </div>
          )}
        </div>
      </section>
    </>
  );
}
