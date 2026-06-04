import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api, money } from '../api';
import { Users, BedDouble, ChevronRight, Wifi, Tv, Wind, Wine, Waves, Check } from 'lucide-react';

const ROOM_IMAGES = {
  default: [
    'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&w=1200&q=85',
    'https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=1200&q=85',
    'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?auto=format&fit=crop&w=1200&q=85',
  ],
};

const AMENITY_ICONS = {
  WiFi: Wifi, TV: Tv, AC: Wind, 'Mini Bar': Wine, Jacuzzi: Waves,
};

export default function RoomsPage() {
  const [roomTypes, setRoomTypes] = useState([]);
  const [ratePlans, setRatePlans] = useState([]);

  useEffect(() => {
    Promise.all([api('/api/room-types'), api('/api/rate-plans')])
      .then(([rt, rp]) => { setRoomTypes(rt); setRatePlans(rp); })
      .catch(() => {});
  }, []);

  return (
    <>
      <section className="page-hero">
        <div className="page-hero__bg" style={{
          backgroundImage: `url(https://images.unsplash.com/photo-1618773928121-c32242e63f39?auto=format&fit=crop&w=1920&q=90)`
        }} />
        <div className="page-hero__overlay" />
        <div className="page-hero__content">
          <span className="label label--gold">Accommodations</span>
          <h1>Rooms & Suites</h1>
          <p>Discover our collection of meticulously designed rooms, each offering a unique perspective of luxury.</p>
        </div>
      </section>

      <section className="section">
        <div className="container">
          {roomTypes.map((rt, idx) => {
            const imgs = ROOM_IMAGES.default;
            const amenityList = rt.amenities ? rt.amenities.split(',').map(a => a.trim()) : [];
            return (
              <div key={rt.id} className={`room-detail ${idx % 2 === 1 ? 'room-detail--reverse' : ''}`}>
                <div className="room-detail__gallery">
                  <img src={imgs[idx % imgs.length]} alt={rt.name} className="room-detail__img" />
                </div>
                <div className="room-detail__info">
                  <span className="label">{rt.bedType || 'Premium'} Suite</span>
                  <h2>{rt.name}</h2>
                  <p className="room-detail__desc">{rt.description}</p>

                  <div className="room-detail__specs">
                    <div><Users size={18} /> <span>Up to {rt.maxOccupancy} Guests</span></div>
                    <div><BedDouble size={18} /> <span>{rt.bedType || 'King'} Bed</span></div>
                    {rt.sizeSqm && <div><span>{rt.sizeSqm} sqm</span></div>}
                  </div>

                  <div className="room-detail__amenities">
                    {amenityList.map((a, i) => {
                      const Icon = AMENITY_ICONS[a] || Check;
                      return (
                        <span key={i} className="amenity-tag">
                          <Icon size={14} /> {a}
                        </span>
                      );
                    })}
                  </div>

                  <div className="room-detail__pricing">
                    <div className="room-detail__price">
                      <span>From</span>
                      <strong>{money(rt.basePricePerNight)}</strong>
                      <span>per night</span>
                    </div>
                    <Link to="/booking" className="btn btn--primary">
                      Reserve <ChevronRight size={16} />
                    </Link>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      </section>

      {ratePlans.length > 0 && (
        <section className="section section--cream">
          <div className="container">
            <div className="section__header">
              <span className="label">Rate Plans</span>
              <h2>Choose Your Experience</h2>
            </div>
            <div className="rate-plans-grid">
              {ratePlans.map(plan => (
                <div key={plan.id} className="rate-plan-card">
                  <h3>{plan.name}</h3>
                  <p>{plan.description}</p>
                  <div className="rate-plan-card__price">+{money(plan.priceModifier)}</div>
                  {plan.cancellationPolicy && (
                    <span className="rate-plan-card__policy">{plan.cancellationPolicy}</span>
                  )}
                  <Link to="/booking" className="btn btn--secondary btn--sm">Select Plan</Link>
                </div>
              ))}
              <div className="rate-plan-card rate-plan-card--basic">
                <h3>Room Only</h3>
                <p>Best flexible rate with no extras included.</p>
                <div className="rate-plan-card__price">Base Rate</div>
                <span className="rate-plan-card__policy">Free cancellation 24h before arrival</span>
                <Link to="/booking" className="btn btn--secondary btn--sm">Select</Link>
              </div>
            </div>
          </div>
        </section>
      )}
    </>
  );
}
