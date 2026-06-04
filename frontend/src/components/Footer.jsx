import { Crown, MapPin, Phone, Mail } from 'lucide-react';
import { Link } from 'react-router-dom';

export default function Footer() {
  return (
    <footer className="footer">
      <div className="footer__inner">
        <div className="footer__brand">
          <div className="footer__logo">
            <Crown size={28} strokeWidth={1.5} />
            <span>Aurelia Grand</span>
          </div>
          <p>A five-star coastal retreat offering refined luxury, world-class dining, and impeccable service in the heart of Colombo.</p>
        </div>

        <div className="footer__col">
          <h4>Quick Links</h4>
          <Link to="/">Home</Link>
          <Link to="/rooms">Rooms & Suites</Link>
          <Link to="/booking">Reservations</Link>
          <Link to="/login">Guest Login</Link>
        </div>

        <div className="footer__col">
          <h4>Hotel Services</h4>
          <span>Infinity Pool & Spa</span>
          <span>Fine Dining Restaurant</span>
          <span>Business Center</span>
          <span>Airport Transfer</span>
        </div>

        <div className="footer__col">
          <h4>Contact</h4>
          <span><MapPin size={14} /> 48 Janadhipathi Mawatha, Colombo 01</span>
          <span><Phone size={14} /> +94 11 242 1221</span>
          <span><Mail size={14} /> reservations@aureliagrand.com</span>
        </div>
      </div>

      <div className="footer__bottom">
        <p>&copy; {new Date().getFullYear()} Aurelia Grand Hotel. All rights reserved.</p>
      </div>
    </footer>
  );
}
