import React from 'react';
import { TaskActivityLogResponse } from '../../types/task.types';

interface TaskActivityTimelineProps {
  activities: TaskActivityLogResponse[];
  loading?: boolean;
}

export const TaskActivityTimeline: React.FC<TaskActivityTimelineProps> = ({ activities, loading }) => {
  if (loading) {
    return <div style={{ padding: '1rem', color: 'var(--text-secondary)' }}>Loading activity history...</div>;
  }

  if (activities.length === 0) {
    return <div style={{ padding: '1rem', color: 'var(--text-muted)' }}>No activity recorded for this task.</div>;
  }

  const formatEventDescription = (log: TaskActivityLogResponse) => {
    switch (log.eventType) {
      case 'TASK_CREATED':
        return `Task "${log.newValue || ''}" was created`;
      case 'STATUS_CHANGED':
        return `Status changed from ${log.oldValue} to ${log.newValue}`;
      case 'PRIORITY_CHANGED':
        return `Priority changed from ${log.oldValue} to ${log.newValue}`;
      case 'ASSIGNEE_CHANGED':
        return `Assignee changed from ${log.oldValue || 'Unassigned'} to ${log.newValue || 'Unassigned'}`;
      case 'SPRINT_CHANGED':
        return `Sprint allocation changed from ${log.oldValue || 'None'} to ${log.newValue || 'None'}`;
      case 'TASK_DELETED':
        return `Task "${log.oldValue || ''}" was deleted`;
      default:
        return `${log.eventType}: ${log.oldValue || ''} -> ${log.newValue || ''}`;
    }
  };

  return (
    <div style={{ marginTop: '1rem' }}>
      <h4 style={{ marginBottom: '0.75rem', fontSize: '1rem', color: 'var(--text-primary)' }}>Activity Timeline</h4>
      <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
        {activities.map((act) => (
          <div
            key={act.id}
            style={{
              padding: '0.75rem',
              backgroundColor: 'var(--bg-secondary)',
              border: '1px solid var(--border-subtle)',
              borderRadius: 'var(--radius-sm)',
              fontSize: '0.85rem',
            }}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.25rem' }}>
              <span style={{ fontWeight: 600, color: 'var(--text-primary)' }}>{act.actorName}</span>
              <span style={{ color: 'var(--text-muted)', fontSize: '0.75rem' }}>
                {new Date(act.createdAt).toLocaleString()}
              </span>
            </div>
            <div style={{ color: 'var(--text-secondary)' }}>{formatEventDescription(act)}</div>
          </div>
        ))}
      </div>
    </div>
  );
};
