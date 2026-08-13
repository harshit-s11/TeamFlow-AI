import React, { useEffect, useState } from 'react';
import { healthApi, HealthResponse } from '../api/healthApi';
import { parseApiError } from '../api/apiClient';

export const HealthCheck: React.FC = () => {
  const [health, setHealth] = useState<HealthResponse | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    healthApi
      .getHealth()
      .then((data) => {
        setHealth(data);
        setError(null);
      })
      .catch((err) => {
        setError(parseApiError(err));
        setHealth(null);
      })
      .finally(() => setLoading(false));
  }, []);

  return (
    <div style={{ maxWidth: '500px', margin: '2rem auto' }}>
      <div className="card">
        <h1 style={{ fontSize: '1.5rem', fontWeight: 700, marginBottom: '1rem' }}>
          Backend Health Status
        </h1>

        {loading ? (
          <div style={{ color: 'var(--text-secondary)' }}>Checking Spring Boot backend API...</div>
        ) : error ? (
          <div className="alert alert-danger">
            <strong>Offline / Error:</strong> {error}
          </div>
        ) : (
          <div className="alert alert-success">
            <strong>Online:</strong> Spring Boot API is responding with status:{' '}
            <span className="badge badge-success" style={{ marginLeft: '0.5rem' }}>
              {health?.status}
            </span>
          </div>
        )}
      </div>
    </div>
  );
};
