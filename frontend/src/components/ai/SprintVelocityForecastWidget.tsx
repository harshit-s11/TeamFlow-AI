import React, { useEffect, useState, useCallback } from 'react';
import { aiApi } from '../../api/aiApi';
import { parseApiError } from '../../api/apiClient';
import { SprintVelocityForecastResponse } from '../../types/ai.types';

interface SprintVelocityForecastWidgetProps {
  sprintId: string;
}

export const SprintVelocityForecastWidget: React.FC<SprintVelocityForecastWidgetProps> = ({ sprintId }) => {
  const [forecast, setForecast] = useState<SprintVelocityForecastResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchForecast = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await aiApi.forecastSprintVelocity(sprintId);
      setForecast(data);
    } catch (err) {
      setError(parseApiError(err));
    } finally {
      setLoading(false);
    }
  }, [sprintId]);

  useEffect(() => {
    fetchForecast();
  }, [fetchForecast]);

  const getRiskBadgeColor = (risk: string) => {
    switch (risk) {
      case 'LOW':
        return 'badge-success';
      case 'MEDIUM':
        return 'badge-warning';
      case 'HIGH':
        return 'badge-danger';
      default:
        return 'badge-secondary';
    }
  };

  return (
    <div className="card" style={{ marginBottom: '1.5rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
        <h3 style={{ fontSize: '1.1rem', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          📊 AI Velocity & Risk Forecast
        </h3>
        <button className="btn btn-secondary btn-sm" onClick={fetchForecast} disabled={loading}>
          {loading ? 'Analyzing...' : 'Refresh'}
        </button>
      </div>

      {error && <div className="alert alert-danger mb-2">{error}</div>}

      {loading && (
        <div style={{ textAlign: 'center', padding: '1rem' }}>
          <div className="spinner mb-2"></div>
          <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)' }}>Calculating velocity metrics and AI risk analysis...</p>
        </div>
      )}

      {forecast && !loading && (
        <div>
          <div className="grid-3 mb-3" style={{ textAlign: 'center' }}>
            <div style={{ backgroundColor: 'var(--bg-secondary)', padding: '0.75rem', borderRadius: 'var(--radius-sm)' }}>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Avg Velocity</div>
              <div style={{ fontSize: '1.25rem', fontWeight: 700 }}>{forecast.historicalAverageVelocity} tasks</div>
            </div>
            <div style={{ backgroundColor: 'var(--bg-secondary)', padding: '0.75rem', borderRadius: 'var(--radius-sm)' }}>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Planned Capacity</div>
              <div style={{ fontSize: '1.25rem', fontWeight: 700 }}>{forecast.plannedCapacity} tasks</div>
            </div>
            <div style={{ backgroundColor: 'var(--bg-secondary)', padding: '0.75rem', borderRadius: 'var(--radius-sm)' }}>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Completion Risk</div>
              <div style={{ marginTop: '0.25rem' }}>
                <span className={`badge ${getRiskBadgeColor(forecast.riskLevel)}`}>
                  {forecast.riskLevel} RISK ({forecast.forecastedCompletionRate}%)
                </span>
              </div>
            </div>
          </div>

          <div>
            <h4 style={{ fontSize: '0.875rem', fontWeight: 600, color: 'var(--text-secondary)', marginBottom: '0.5rem' }}>
              AI Insights & Risk Assessment:
            </h4>
            <ul style={{ paddingLeft: '1.25rem', margin: 0, fontSize: '0.875rem', color: 'var(--text-main)' }}>
              {forecast.aiInsights.map((insight, idx) => (
                <li key={idx} style={{ marginBottom: '0.25rem' }}>{insight}</li>
              ))}
            </ul>
          </div>
        </div>
      )}
    </div>
  );
};
