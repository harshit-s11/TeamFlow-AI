import React from 'react';
import { TaskResponse, TaskStatus } from '../../types/domain.types';
import { KanbanTaskCard } from './KanbanTaskCard';

interface KanbanColumnProps {
  title: string;
  status: TaskStatus;
  tasks: TaskResponse[];
  onStatusChange: (taskId: string, newStatus: TaskStatus) => void;
  onEdit: (task: TaskResponse) => void;
}

export const KanbanColumn: React.FC<KanbanColumnProps> = ({ title, status, tasks, onStatusChange, onEdit }) => {
  return (
    <div
      style={{
        backgroundColor: 'var(--bg-secondary)',
        borderRadius: 'var(--radius-sm)',
        border: '1px solid var(--border-subtle)',
        padding: '1rem',
        display: 'flex',
        flexDirection: 'column',
        minHeight: '450px',
      }}
    >
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem', paddingBottom: '0.5rem', borderBottom: '2px solid var(--border-subtle)' }}>
        <h3 style={{ fontSize: '0.95rem', fontWeight: 700, margin: 0, textTransform: 'uppercase', color: 'var(--text-primary)' }}>
          {title}
        </h3>
        <span className="badge badge-user" style={{ fontSize: '0.75rem' }}>
          {tasks.length}
        </span>
      </div>

      <div style={{ flex: 1, overflowY: 'auto' }}>
        {tasks.length === 0 ? (
          <div
            style={{
              padding: '2rem 0.5rem',
              textAlign: 'center',
              color: 'var(--text-muted)',
              fontSize: '0.85rem',
              border: '1px dashed var(--border-subtle)',
              borderRadius: 'var(--radius-sm)',
            }}
          >
            No tasks in {status}
          </div>
        ) : (
          tasks.map((task) => (
            <KanbanTaskCard key={task.id} task={task} onStatusChange={onStatusChange} onEdit={onEdit} />
          ))
        )}
      </div>
    </div>
  );
};
