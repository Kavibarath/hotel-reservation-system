import { NavLink, Outlet, Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  LayoutDashboard, CalendarDays, BedDouble, DollarSign, Crown, LogOut, Home
} from 'lucide-react';

export default function AdminLayout() {
  const { user, isStaff, logout } = useAuth();

  if (!isStaff) return <Navigate to="/login" replace />;

  return (
    <div className="admin-layout">
      <aside className="admin-sidebar">
        <div className="admin-sidebar__brand">
          <Crown size={22} strokeWidth={1.5} />
          <span>Aurelia Admin</span>
        </div>
        <nav className="admin-sidebar__nav">
          <NavLink to="/admin" end>
            <LayoutDashboard size={18} /> Dashboard
          </NavLink>
          <NavLink to="/admin/reservations">
            <CalendarDays size={18} /> Reservations
          </NavLink>
          <NavLink to="/admin/rooms">
            <BedDouble size={18} /> Rooms
          </NavLink>
          <NavLink to="/admin/rate-plans">
            <DollarSign size={18} /> Rate Plans
          </NavLink>
        </nav>
        <div className="admin-sidebar__footer">
          <NavLink to="/" className="admin-sidebar__link">
            <Home size={16} /> Back to Site
          </NavLink>
          <div className="admin-sidebar__user">
            <span>{user?.fullName}</span>
            <span className="admin-sidebar__role">{user?.role}</span>
          </div>
          <button onClick={logout} className="admin-sidebar__logout">
            <LogOut size={16} /> Sign Out
          </button>
        </div>
      </aside>
      <main className="admin-main">
        <Outlet />
      </main>
    </div>
  );
}
