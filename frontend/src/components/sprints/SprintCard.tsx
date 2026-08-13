import React from 'react';
import { Link } from 'react-router-dom';
import { SprintResponse } from '../../types/domain.types';
import { formatDate } from '../../utils/formatters';

export const SprintCard: React.FC<{ sprint: SprintResponse }> = ({ sprint }) => {
  const getBadgeClass = (status: string) => {
    switch (status) {
      case 'ACTIVE':
        return 'badge badge-success';
      case 'COMPLETED':
        return 'badge badge-user';
      default:
        return 'badge badge-admin';
    }
  };

  return (
    <div className="card" style={{ display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
      <div>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '0.75rem' }}>
          <h3 style={{ fontSize: '1.15rem', fontWeight: 600, color: 'var(--text-primary)' }}>{sprint.name}</h3>
          <span className={getBadgeClass(sprint.status)}>{sprint.status}</span>
        </div>
        <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', marginBottom: '1.25rem' }}>
          📅 {formatDate(sprint.startDate)} – {formatDate(sprint.endDate)}
        </p>
      </div>

      <div style={{ borderTop: '1px solid var(--border-subtle)', paddingTop: '1rem', display: 'flex', justifyContent: 'flex-end' }}>
        <Link to={`/sprints/${sprint.id}`} className="btn btn-secondary" style={{ padding: '0.4rem 0.85rem', fontSize: '0.85rem' }}>
          Sprint Details & Tasks →
        </Link>
      </div>
    </div>
  );
};
