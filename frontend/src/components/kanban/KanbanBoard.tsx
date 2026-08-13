import React from 'react';
import { TaskResponse, TaskStatus } from '../../types/domain.types';
import { KanbanColumn } from './KanbanColumn';

interface KanbanBoardProps {
  tasks: TaskResponse[];
  onStatusChange: (taskId: string, newStatus: TaskStatus) => void;
  onEdit: (task: TaskResponse) => void;
}

export const KanbanBoard: React.FC<KanbanBoardProps> = ({ tasks, onStatusChange, onEdit }) => {
  const columns: { title: string; status: TaskStatus }[] = [
    { title: 'To Do', status: 'TODO' },
    { title: 'In Progress', status: 'IN_PROGRESS' },
    { title: 'In Review', status: 'IN_REVIEW' },
    { title: 'Done', status: 'DONE' },
  ];

  return (
    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))', gap: '1.25rem' }}>
      {columns.map((col) => (
        <KanbanColumn
          key={col.status}
          title={col.title}
          status={col.status}
          tasks={tasks.filter((t) => t.status === col.status)}
          onStatusChange={onStatusChange}
          onEdit={onEdit}
        />
      ))}
    </div>
  );
};
