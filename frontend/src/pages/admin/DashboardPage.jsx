import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api, money } from '../../api';
import {
  BarChart3, BedDouble, CalendarDays, DollarSign,
  Users, Loader2, TrendingUp, ClipboardList
} from 'lucide-react';

export default function DashboardPage() {
  const [summary, setSummary] = useState(null);
  const [revenue, setRevenue] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      api('/api/admin/dashboard/summary'),
      api('/api/admin/reports/daily-revenue'),
    ]).then(([s, r]) => {
      setSummary(s);
      setRevenue(r);
    }).catch(() => {}).finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <div className="admin-page">
        <div className="loading-state"><Loader2 size={32} className="spin" /><p>Loading dashboard...</p></div>
      </div>
    );
  }

  const maxRevenue = Math.max(...revenue.map(r => r.revenue || 0), 1);

  return (
    <div className="admin-page">
      <div className="admin-page__header">
        <div>
          <h1>Dashboard</h1>
          <p>Overview of hotel operations and performance</p>
        </div>
      </div>

      {/* KPI Cards */}
      <div className="kpi-grid">
        <div className="kpi-card">
          <div className="kpi-card__icon kpi-card__icon--blue"><CalendarDays size={24} /></div>
          <div>
            <span>Total Reservations</span>
            <strong>{summary?.totalReservations ?? 0}</strong>
          </div>
        </div>
        <div className="kpi-card">
          <div className="kpi-card__icon kpi-card__icon--green"><Users size={24} /></div>
          <div>
            <span>Active Bookings</span>
            <strong>{summary?.activeReservations ?? 0}</strong>
          </div>
        </div>
        <div className="kpi-card">
          <div className="kpi-card__icon kpi-card__icon--amber"><BedDouble size={24} /></div>
          <div>
            <span>Occupancy Rate</span>
            <strong>{summary?.occupancyPercent != null ? `${Math.round(summary.occupancyPercent)}%` : 'N/A'}</strong>
          </div>
        </div>
        <div className="kpi-card">
          <div className="kpi-card__icon kpi-card__icon--purple"><DollarSign size={24} /></div>
          <div>
            <span>Total Revenue</span>
            <strong>{money(summary?.totalRevenue)}</strong>
          </div>
        </div>
      </div>

      {/* Quick Actions */}
      <div className="admin-section">
        <h2><ClipboardList size={22} /> Quick Actions</h2>
        <div className="quick-actions">
          <Link to="/admin/reservations" className="quick-action-card">
            <CalendarDays size={28} />
            <span>Manage Reservations</span>
            <p>Check-in, check-out, and manage guest bookings</p>
          </Link>
          <Link to="/admin/rooms" className="quick-action-card">
            <BedDouble size={28} />
            <span>Room Management</span>
            <p>Manage room inventory and status</p>
          </Link>
          <Link to="/admin/rate-plans" className="quick-action-card">
            <DollarSign size={28} />
            <span>Rate Plans</span>
            <p>Configure pricing and packages</p>
          </Link>
        </div>
      </div>

      {/* Revenue Chart */}
      {revenue.length > 0 && (
        <div className="admin-section">
          <h2><TrendingUp size={22} /> Daily Revenue</h2>
          <div className="revenue-chart">
            {revenue.slice(-14).map((r, i) => (
              <div key={i} className="revenue-bar">
                <div className="revenue-bar__fill"
                  style={{ height: `${Math.max((r.revenue / maxRevenue) * 100, 4)}%` }}>
                  <span className="revenue-bar__tooltip">{money(r.revenue)}</span>
                </div>
                <span className="revenue-bar__label">{r.date?.slice(5) || ''}</span>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
