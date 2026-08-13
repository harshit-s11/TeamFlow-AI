import React from 'react';
import { Link } from 'react-router-dom';
import { TaskResponse } from '../../types/domain.types';
import { formatDate } from '../../utils/formatters';

export const TaskCard: React.FC<{ task: TaskResponse }> = ({ task }) => {
  const getPriorityBadgeClass = (priority: string) => {
    switch (priority) {
      case 'URGENT':
      case 'HIGH':
        return 'badge badge-admin';
      case 'MEDIUM':
        return 'badge badge-user';
      default:
        return 'badge badge-success';
    }
  };

  const getStatusBadgeClass = (status: string) => {
    switch (status) {
      case 'DONE':
        return 'badge badge-success';
      case 'IN_PROGRESS':
      case 'IN_REVIEW':
        return 'badge badge-admin';
      default:
        return 'badge badge-user';
    }
  };

  return (
    <div className="card" style={{ display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
      <div>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '0.75rem', gap: '0.5rem' }}>
          <h3 style={{ fontSize: '1.05rem', fontWeight: 600, color: 'var(--text-primary)', margin: 0 }}>{task.title}</h3>
          <span className={getPriorityBadgeClass(task.priority)}>{task.priority}</span>
        </div>
        <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem', marginBottom: '1rem', display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden' }}>
          {task.description || 'No description provided.'}
        </p>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.75rem' }}>
          <span className={getStatusBadgeClass(task.status)}>{task.status}</span>
        </div>
      </div>

      <div style={{ borderTop: '1px solid var(--border-subtle)', paddingTop: '0.75rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{formatDate(task.createdAt)}</span>
        <Link to={`/tasks/${task.id}`} className="btn btn-secondary" style={{ padding: '0.35rem 0.75rem', fontSize: '0.8rem' }}>
          View Task →
        </Link>
      </div>
    </div>
  );
};
