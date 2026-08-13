import React, { useEffect, useState } from 'react';
import { taskApi } from '../api/taskApi';
import { parseApiError } from '../api/apiClient';
import { TaskResponse } from '../types/domain.types';
import { TaskCard } from '../components/tasks/TaskCard';
import { CreateTaskModal } from '../components/tasks/CreateTaskModal';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import { EmptyState } from '../components/common/EmptyState';

export const TasksPage: React.FC = () => {
  const [tasks, setTasks] = useState<TaskResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isCreateOpen, setIsCreateOpen] = useState(false);

  const fetchTasks = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await taskApi.getAllTasks();
      setTasks(data);
    } catch (err) {
      setError(parseApiError(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTasks();
  }, []);

  const handleCreated = (newTask: TaskResponse) => {
    setTasks((prev) => [newTask, ...prev]);
  };

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <div>
          <h1 style={{ fontSize: '1.875rem', fontWeight: 700, marginBottom: '0.25rem' }}>Task Backlog & Roster</h1>
          <p style={{ color: 'var(--text-secondary)' }}>View and manage project development tasks.</p>
        </div>
        <button className="btn btn-primary" onClick={() => setIsCreateOpen(true)}>
          + Create Task
        </button>
      </div>

      {error && <div className="alert alert-danger">{error}</div>}

      {loading ? (
        <LoadingSpinner message="Fetching tasks..." />
      ) : tasks.length === 0 ? (
        <EmptyState
          title="No Tasks Found"
          description="There are currently no tasks. Create a task to start tracking work!"
          actionText="+ Create Your First Task"
          onAction={() => setIsCreateOpen(true)}
        />
      ) : (
        <div className="grid-3">
          {tasks.map((task) => (
            <TaskCard key={task.id} task={task} />
          ))}
        </div>
      )}

      <CreateTaskModal isOpen={isCreateOpen} onClose={() => setIsCreateOpen(false)} onSuccess={handleCreated} />
    </div>
  );
};
