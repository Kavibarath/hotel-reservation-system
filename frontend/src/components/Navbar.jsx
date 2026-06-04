import { useState, useEffect } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Menu, X, Crown, User, LogOut, LayoutDashboard } from 'lucide-react';

export default function Navbar() {
  const { user, logout, isAdmin, isStaff } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const [mobileOpen, setMobileOpen] = useState(false);
  const [scrolled, setScrolled] = useState(false);

  const isHome = location.pathname === '/';

  useEffect(() => {
    function onScroll() {
      setScrolled(window.scrollY > 80);
    }
    onScroll();
    window.addEventListener('scroll', onScroll, { passive: true });
    return () => window.removeEventListener('scroll', onScroll);
  }, []);

  // On non-home pages always solid; on home page solid only after scroll
  const isSolid = !isHome || scrolled;

  function handleLogout() {
    logout();
    navigate('/');
    setMobileOpen(false);
  }

  return (
    <nav className={`navbar ${isSolid ? 'navbar--solid' : 'navbar--transparent'}`}>
      <div className="navbar__inner">
        <Link to="/" className="navbar__brand" onClick={() => setMobileOpen(false)}>
          <Crown size={26} strokeWidth={1.5} />
          <span>Aurelia Grand</span>
        </Link>

        <button
          className="navbar__toggle"
          onClick={() => setMobileOpen(!mobileOpen)}
          aria-label="Toggle menu"
        >
          {mobileOpen ? <X size={24} /> : <Menu size={24} />}
        </button>

        <div className={`navbar__links ${mobileOpen ? 'navbar__links--open' : ''}`}>
          <Link to="/" onClick={() => setMobileOpen(false)}
                className={location.pathname === '/' ? 'active' : ''}>Home</Link>
          <Link to="/rooms" onClick={() => setMobileOpen(false)}
                className={location.pathname === '/rooms' ? 'active' : ''}>Rooms & Suites</Link>
          <Link to="/booking" onClick={() => setMobileOpen(false)}
                className={location.pathname === '/booking' ? 'active' : ''}>Book Now</Link>

          {user ? (
            <>
              <Link to="/my-reservations" onClick={() => setMobileOpen(false)}
                    className={location.pathname === '/my-reservations' ? 'active' : ''}>
                My Bookings
              </Link>
              {isStaff && (
                <Link to="/admin" onClick={() => setMobileOpen(false)}
                      className={location.pathname.startsWith('/admin') ? 'active' : ''}>
                  <LayoutDashboard size={16} /> Dashboard
                </Link>
              )}
              <div className="navbar__user">
                <User size={16} />
                <span>{user.fullName}</span>
                <button onClick={handleLogout} className="navbar__logout" title="Sign out">
                  <LogOut size={16} />
                </button>
              </div>
            </>
          ) : (
            <Link to="/login" onClick={() => setMobileOpen(false)}
                  className={`navbar__signin ${location.pathname === '/login' ? 'active' : ''}`}>
              Sign In
            </Link>
          )}
        </div>
      </div>
    </nav>
  );
}
