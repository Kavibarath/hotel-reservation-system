import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api';
import {
  Crown, Star, Waves, UtensilsCrossed, Dumbbell, Car,
  Wifi, ShieldCheck, Sparkles, ChevronRight, CalendarDays,
  Users, BedDouble, MapPin, Phone
} from 'lucide-react';

const HERO_IMG = 'https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=1920&q=90';
const POOL_IMG = 'https://images.unsplash.com/photo-1571896349842-33c89424de2d?auto=format&fit=crop&w=800&q=85';
const DINING_IMG = 'https://images.unsplash.com/photo-1414235077428-338989a2e8c0?auto=format&fit=crop&w=800&q=85';
const SPA_IMG = 'https://images.unsplash.com/photo-1600334129128-685c5582fd35?auto=format&fit=crop&w=800&q=85';

const ROOM_IMAGES = [
  'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&w=800&q=85',
  'https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=800&q=85',
  'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?auto=format&fit=crop&w=800&q=85',
];

const amenities = [
  { icon: Waves, title: 'Infinity Pool', desc: 'Rooftop pool with panoramic ocean views' },
  { icon: UtensilsCrossed, title: 'Fine Dining', desc: 'Award-winning international cuisine' },
  { icon: Dumbbell, title: 'Fitness & Spa', desc: '24/7 gym and luxury wellness center' },
  { icon: Car, title: 'Airport Transfer', desc: 'Complimentary luxury vehicle pickup' },
  { icon: Wifi, title: 'High-Speed WiFi', desc: 'Complimentary throughout the property' },
  { icon: ShieldCheck, title: '24/7 Concierge', desc: 'Personalized service around the clock' },
];

export default function HomePage() {
  const [roomTypes, setRoomTypes] = useState([]);

  useEffect(() => {
    api('/api/room-types').then(setRoomTypes).catch(() => {});
  }, []);

  return (
    <>
      {/* Hero */}
      <section className="hero">
        <div className="hero__bg" style={{ backgroundImage: `url(${HERO_IMG})` }} />
        <div className="hero__overlay" />
        <div className="hero__content">
          <div className="hero__badge">
            {[...Array(5)].map((_, i) => <Star key={i} size={14} fill="currentColor" />)}
            <span>Five-Star Luxury</span>
          </div>
          <h1>Experience Timeless<br />Elegance</h1>
          <p>Where the Indian Ocean meets refined hospitality. Discover a sanctuary of luxury in the heart of Colombo.</p>
          <div className="hero__actions">
            <Link to="/booking" className="btn btn--primary btn--lg">
              Reserve Your Stay <ChevronRight size={18} />
            </Link>
            <Link to="/rooms" className="btn btn--outline btn--lg">
              Explore Rooms
            </Link>
          </div>
        </div>
      </section>

      {/* Welcome */}
      <section className="section welcome">
        <div className="container">
          <div className="welcome__grid">
            <div className="welcome__text">
              <span className="label">Welcome to Aurelia Grand</span>
              <h2>A Legacy of Luxury Since 1965</h2>
              <p>
                Nestled along the shores of Colombo, Aurelia Grand has been the epitome of
                five-star hospitality for over five decades. Our 200+ rooms and suites
                offer breathtaking views of the Indian Ocean, while our award-winning
                restaurants and world-class spa ensure every moment is extraordinary.
              </p>
              <div className="welcome__stats">
                <div><strong>200+</strong><span>Luxury Rooms</span></div>
                <div><strong>5</strong><span>Restaurants</span></div>
                <div><strong>50+</strong><span>Years Legacy</span></div>
                <div><strong>4.9</strong><span>Guest Rating</span></div>
              </div>
            </div>
            <div className="welcome__images">
              <img src={POOL_IMG} alt="Infinity pool" className="welcome__img welcome__img--main" />
              <img src={DINING_IMG} alt="Fine dining" className="welcome__img welcome__img--accent" />
            </div>
          </div>
        </div>
      </section>

      {/* Rooms Preview */}
      <section className="section section--cream rooms-preview">
        <div className="container">
          <div className="section__header">
            <span className="label">Accommodations</span>
            <h2>Rooms & Suites</h2>
            <p>Each room is a masterpiece of design, offering the perfect blend of comfort and sophistication.</p>
          </div>
          <div className="rooms-preview__grid">
            {roomTypes.map((rt, i) => (
              <article key={rt.id} className="room-card">
                <div className="room-card__img">
                  <img src={ROOM_IMAGES[i % ROOM_IMAGES.length]} alt={rt.name} />
                  <div className="room-card__price">
                    From <strong>${rt.basePricePerNight}</strong>/night
                  </div>
                </div>
                <div className="room-card__body">
                  <h3>{rt.name}</h3>
                  <p>{rt.description}</p>
                  <div className="room-card__meta">
                    <span><Users size={15} /> Up to {rt.maxOccupancy} guests</span>
                    <span><BedDouble size={15} /> {rt.bedType || 'King'} Bed</span>
                    {rt.sizeSqm && <span>{rt.sizeSqm} sqm</span>}
                  </div>
                  <Link to="/booking" className="btn btn--secondary">
                    Book Now <ChevronRight size={16} />
                  </Link>
                </div>
              </article>
            ))}
          </div>
          <div className="section__cta">
            <Link to="/rooms" className="btn btn--outline">View All Rooms & Suites</Link>
          </div>
        </div>
      </section>

      {/* Amenities */}
      <section className="section amenities">
        <div className="container">
          <div className="section__header">
            <span className="label">Hotel Amenities</span>
            <h2>World-Class Facilities</h2>
          </div>
          <div className="amenities__grid">
            {amenities.map((a, i) => (
              <div key={i} className="amenity-card">
                <div className="amenity-card__icon">
                  <a.icon size={28} strokeWidth={1.5} />
                </div>
                <h4>{a.title}</h4>
                <p>{a.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Experiences */}
      <section className="section section--dark experiences">
        <div className="container">
          <div className="section__header section__header--light">
            <span className="label label--gold">Curated Experiences</span>
            <h2>Beyond Accommodation</h2>
          </div>
          <div className="experiences__grid">
            <div className="experience-card">
              <img src={SPA_IMG} alt="Spa" />
              <div className="experience-card__content">
                <h3>The Aurelia Spa</h3>
                <p>Surrender to tranquility with traditional Ayurvedic treatments and modern wellness therapies.</p>
              </div>
            </div>
            <div className="experience-card">
              <img src={DINING_IMG} alt="Dining" />
              <div className="experience-card__content">
                <h3>Culinary Journey</h3>
                <p>From authentic Sri Lankan cuisine to international fine dining, savor flavors that tell a story.</p>
              </div>
            </div>
            <div className="experience-card">
              <img src={POOL_IMG} alt="Pool" />
              <div className="experience-card__content">
                <h3>Ocean Lounge</h3>
                <p>Unwind at our signature rooftop bar with craft cocktails and stunning sunset views.</p>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* CTA */}
      <section className="cta-banner">
        <div className="container">
          <div className="cta-banner__inner">
            <div>
              <span className="label label--gold">Special Offer</span>
              <h2>Book Direct & Save 15%</h2>
              <p>Enjoy exclusive rates, complimentary breakfast, and late checkout when you book directly.</p>
            </div>
            <Link to="/booking" className="btn btn--primary btn--lg">
              Reserve Now <ChevronRight size={18} />
            </Link>
          </div>
        </div>
      </section>
    </>
  );
}
