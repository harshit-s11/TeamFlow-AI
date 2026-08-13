import React, { useEffect, useState, useCallback } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { taskApi } from '../api/taskApi';
import { parseApiError } from '../api/apiClient';
import { TaskResponse } from '../types/domain.types';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import { EditTaskModal } from '../components/tasks/EditTaskModal';
import { ConfirmDialog } from '../components/common/ConfirmDialog';
import { formatDate } from '../utils/formatters';

export const TaskDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const [task, setTask] = useState<TaskResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [isEditOpen, setIsEditOpen] = useState(false);
  const [isDeleteOpen, setIsDeleteOpen] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);

  const loadTaskData = useCallback(async () => {
    if (!id) return;
    try {
      setLoading(true);
      setError(null);
      const taskData = await taskApi.getTaskById(id);
      setTask(taskData);
    } catch (err) {
      setError(parseApiError(err));
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    loadTaskData();
  }, [loadTaskData]);

  const handleDelete = async () => {
    if (!id) return;
    try {
      setIsDeleting(true);
      await taskApi.deleteTask(id);
      navigate('/tasks');
    } catch (err) {
      alert(parseApiError(err));
    } finally {
      setIsDeleting(false);
    }
  };

  if (loading) {
    return <LoadingSpinner message="Loading task details..." />;
  }

  if (error) {
    return (
      <div style={{ maxWidth: '600px', margin: '2rem auto' }}>
        <div className="alert alert-danger">{error}</div>
        <Link to="/tasks" className="btn btn-secondary">
          ← Back to Tasks
        </Link>
      </div>
    );
  }

  return (
    <div>
      <div style={{ marginBottom: '1.5rem' }}>
        <Link to="/tasks" style={{ color: 'var(--text-secondary)', textDecoration: 'none', fontSize: '0.9rem' }}>
          ← Back to Tasks List
        </Link>
      </div>

      <div className="card" style={{ marginBottom: '2rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '1rem' }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '0.5rem' }}>
              <h1 style={{ fontSize: '1.875rem', fontWeight: 700, margin: 0 }}>{task?.title}</h1>
              <span className="badge badge-success">{task?.status}</span>
              <span className="badge badge-admin">{task?.priority}</span>
            </div>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', marginBottom: '1rem' }}>
              Created on {task?.createdAt ? formatDate(task.createdAt) : ''} • Task ID: {task?.id}
            </p>
            <p style={{ color: 'var(--text-primary)', fontSize: '1rem', whiteSpace: 'pre-wrap' }}>
              {task?.description || 'No description provided.'}
            </p>
          </div>

          <div style={{ display: 'flex', gap: '0.75rem' }}>
            <button className="btn btn-secondary" onClick={() => setIsEditOpen(true)}>
              Edit Task
            </button>
            <button className="btn btn-danger" onClick={() => setIsDeleteOpen(true)}>
              Delete Task
            </button>
          </div>
        </div>
      </div>

      <div className="card">
        <h2 style={{ fontSize: '1.25rem', fontWeight: 600, marginBottom: '1rem' }}>Task Association Details</h2>
        <div className="grid-2">
          <div style={{ backgroundColor: 'var(--bg-secondary)', padding: '1rem', borderRadius: 'var(--radius-sm)' }}>
            <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', textTransform: 'uppercase' }}>Project ID</span>
            <div style={{ marginTop: '0.25rem', fontWeight: 600 }}>
              <Link to={`/projects/${task?.projectId}`} style={{ color: 'var(--accent-primary)', textDecoration: 'none' }}>
                {task?.projectId}
              </Link>
            </div>
          </div>

          <div style={{ backgroundColor: 'var(--bg-secondary)', padding: '1rem', borderRadius: 'var(--radius-sm)' }}>
            <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', textTransform: 'uppercase' }}>Sprint ID</span>
            <div style={{ marginTop: '0.25rem', fontWeight: 600 }}>
              {task?.sprintId ? (
                <Link to={`/sprints/${task.sprintId}`} style={{ color: 'var(--accent-primary)', textDecoration: 'none' }}>
                  {task.sprintId}
                </Link>
              ) : (
                'Backlog (Unassigned)'
              )}
            </div>
          </div>
        </div>
      </div>

      <EditTaskModal
        isOpen={isEditOpen}
        task={task}
        onClose={() => setIsEditOpen(false)}
        onSuccess={(updated) => setTask(updated)}
      />

      <ConfirmDialog
        isOpen={isDeleteOpen}
        onClose={() => setIsDeleteOpen(false)}
        onConfirm={handleDelete}
        title="Delete Task"
        message={`Are you sure you want to delete the task "${task?.title}"?`}
        confirmText="Delete Task"
        isLoading={isDeleting}
      />
    </div>
  );
};
