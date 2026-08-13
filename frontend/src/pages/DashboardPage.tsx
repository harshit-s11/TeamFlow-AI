import React from 'react';
import { useAuth } from '../context/AuthContext';
import { formatDate } from '../utils/formatters';

export const DashboardPage: React.FC = () => {
  const { user } = useAuth();

  return (
    <div>
      <div style={{ marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '1.875rem', fontWeight: 700, marginBottom: '0.25rem' }}>
          Welcome back, {user?.name}!
        </h1>
        <p style={{ color: 'var(--text-secondary)' }}>
          Agile Workspace Overview & Active System Session
        </p>
      </div>

      <div className="grid-3" style={{ marginBottom: '2rem' }}>
        <div className="card">
          <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', fontWeight: 600, textTransform: 'uppercase' }}>
            Account Role
          </div>
          <div style={{ marginTop: '0.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <span className={user?.role === 'ADMIN' ? 'badge badge-admin' : 'badge badge-user'}>
              {user?.role}
            </span>
          </div>
        </div>

        <div className="card">
          <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', fontWeight: 600, textTransform: 'uppercase' }}>
            Account Email
          </div>
          <div style={{ marginTop: '0.5rem', fontSize: '1.1rem', fontWeight: 600 }}>
            {user?.email}
          </div>
        </div>

        <div className="card">
          <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', fontWeight: 600, textTransform: 'uppercase' }}>
            Member Since
          </div>
          <div style={{ marginTop: '0.5rem', fontSize: '1.1rem', fontWeight: 600 }}>
            {user?.createdAt ? formatDate(user.createdAt) : 'Recently Registered'}
          </div>
        </div>
      </div>

      <div className="card" style={{ marginBottom: '2rem' }}>
        <h2 style={{ fontSize: '1.25rem', fontWeight: 600, marginBottom: '1rem' }}>
          Platform Feature Status (S2-1 Frontend Foundation)
        </h2>
        <div className="grid-2">
          <div style={{ backgroundColor: 'var(--bg-secondary)', padding: '1.25rem', borderRadius: 'var(--radius-sm)' }}>
            <h3 style={{ fontSize: '1rem', fontWeight: 600, color: 'var(--accent-primary)', marginBottom: '0.5rem' }}>
              🔒 Authentication & Security
            </h3>
            <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)' }}>
              Stateless JWT authentication integrated. Token storage abstraction configured using localStorage. Protected route guards active.
            </p>
          </div>

          <div style={{ backgroundColor: 'var(--bg-secondary)', padding: '1.25rem', borderRadius: 'var(--radius-sm)' }}>
            <h3 style={{ fontSize: '1rem', fontWeight: 600, color: 'var(--accent-success)', marginBottom: '0.5rem' }}>
              🌐 CORS & API Integration
            </h3>
            <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)' }}>
              Spring Boot backend connected via Axios client with request Bearer injection & error response interceptor.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};
