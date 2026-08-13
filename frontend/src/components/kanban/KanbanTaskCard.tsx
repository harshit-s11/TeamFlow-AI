import React from 'react';
import { TaskResponse, TaskStatus } from '../../types/domain.types';

interface KanbanTaskCardProps {
  task: TaskResponse;
  onStatusChange: (taskId: string, newStatus: TaskStatus) => void;
  onEdit: (task: TaskResponse) => void;
}

export const KanbanTaskCard: React.FC<KanbanTaskCardProps> = ({ task, onStatusChange, onEdit }) => {
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

  const statuses: TaskStatus[] = ['TODO', 'IN_PROGRESS', 'IN_REVIEW', 'DONE'];

  return (
    <div
      style={{
        backgroundColor: 'var(--bg-card)',
        border: '1px solid var(--border-subtle)',
        borderRadius: 'var(--radius-sm)',
        padding: '0.85rem',
        marginBottom: '0.75rem',
        boxShadow: 'var(--shadow-sm)',
      }}
    >
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '0.5rem', gap: '0.5rem' }}>
        <span
          onClick={() => onEdit(task)}
          style={{ fontWeight: 600, fontSize: '0.9rem', color: 'var(--text-primary)', cursor: 'pointer', textDecoration: 'underline' }}
        >
          {task.title}
        </span>
        <span className={getPriorityBadgeClass(task.priority)}>{task.priority}</span>
      </div>

      {task.description && (
        <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginBottom: '0.75rem', display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden' }}>
          {task.description}
        </p>
      )}

      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '0.5rem', paddingTop: '0.5rem', borderTop: '1px dashed var(--border-subtle)' }}>
        <label htmlFor={`status-select-${task.id}`} style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Move:</label>
        <select
          id={`status-select-${task.id}`}
          aria-label="Move task status"
          value={task.status}
          onChange={(e) => onStatusChange(task.id, e.target.value as TaskStatus)}
          style={{
            fontSize: '0.75rem',
            padding: '0.2rem 0.4rem',
            backgroundColor: 'var(--bg-secondary)',
            color: 'var(--text-primary)',
            border: '1px solid var(--border-subtle)',
            borderRadius: '4px',
            cursor: 'pointer',
          }}
        >
          {statuses.map((s) => (
            <option key={s} value={s}>
              {s}
            </option>
          ))}
        </select>
      </div>
    </div>
  );
};
