import React, { useEffect, useState, useCallback } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { taskApi } from '../api/taskApi';
import { parseApiError } from '../api/apiClient';
import { TaskResponse } from '../types/domain.types';
import { TaskActivityLogResponse } from '../types/task.types';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import { EditTaskModal } from '../components/tasks/EditTaskModal';
import { ConfirmDialog } from '../components/common/ConfirmDialog';
import { TaskActivityTimeline } from '../components/tasks/TaskActivityTimeline';
import { AiTaskBreakdownModal } from '../components/ai/AiTaskBreakdownModal';
import { formatDate } from '../utils/formatters';

export const TaskDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const [task, setTask] = useState<TaskResponse | null>(null);
  const [activities, setActivities] = useState<TaskActivityLogResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [activityLoading, setActivityLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [isEditOpen, setIsEditOpen] = useState(false);
  const [isDeleteOpen, setIsDeleteOpen] = useState(false);
  const [isAiBreakdownOpen, setIsAiBreakdownOpen] = useState(false);
  const [deleting, setDeleting] = useState(false);

  const loadTaskData = useCallback(async () => {
    if (!id) return;
    try {
      setLoading(true);
      setError(null);
      const data = await taskApi.getTaskById(id);
      setTask(data);
    } catch (err) {
      setError(parseApiError(err));
    } finally {
      setLoading(false);
    }
  }, [id]);

  const loadActivityData = useCallback(async () => {
    if (!id) return;
    try {
      setActivityLoading(true);
      const activityData = await taskApi.getTaskActivity(id);
      setActivities(activityData);
    } catch (err) {
      console.error('Failed to load task activity', err);
    } finally {
      setActivityLoading(false);
    }
  }, [id]);

  useEffect(() => {
    loadTaskData();
    loadActivityData();
  }, [loadTaskData, loadActivityData]);

  const handleDelete = async () => {
    if (!id) return;
    try {
      setDeleting(true);
      await taskApi.deleteTask(id);
      navigate('/tasks');
    } catch (err) {
      setError(parseApiError(err));
      setDeleting(false);
    }
  };

  const handleTaskUpdated = (updated: TaskResponse) => {
    setTask(updated);
    loadActivityData();
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
            <button className="btn btn-primary" onClick={() => setIsAiBreakdownOpen(true)}>
              ✨ AI Breakdown
            </button>
            <button className="btn btn-secondary" onClick={() => setIsEditOpen(true)}>
              Edit Task
            </button>
            <button className="btn btn-danger" onClick={() => setIsDeleteOpen(true)}>
              Delete Task
            </button>
          </div>
        </div>
      </div>

      <div className="card" style={{ marginBottom: '2rem' }}>
        <h2 style={{ fontSize: '1.25rem', fontWeight: 600, marginBottom: '1rem' }}>Task Association Details</h2>
        <div className="grid-2">
          <div style={{ backgroundColor: 'var(--bg-secondary)', padding: '1rem', borderRadius: 'var(--radius-sm)' }}>
            <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Project ID</span>
            <p style={{ margin: 0, fontWeight: 500 }}>{task?.projectId || 'Unassigned'}</p>
          </div>
          <div style={{ backgroundColor: 'var(--bg-secondary)', padding: '1rem', borderRadius: 'var(--radius-sm)' }}>
            <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Sprint ID</span>
            <p style={{ margin: 0, fontWeight: 500 }}>{task?.sprintId || 'Backlog (No Sprint)'}</p>
          </div>
        </div>
      </div>

      <div className="card">
        <TaskActivityTimeline activities={activities} loading={activityLoading} />
      </div>

      {task && (
        <AiTaskBreakdownModal
          isOpen={isAiBreakdownOpen}
          taskId={task.id}
          projectId={task.projectId}
          onClose={() => setIsAiBreakdownOpen(false)}
          onSuccess={loadTaskData}
        />
      )}

      <EditTaskModal
        isOpen={isEditOpen}
        task={task}
        onClose={() => setIsEditOpen(false)}
        onSuccess={handleTaskUpdated}
      />

      <ConfirmDialog
        isOpen={isDeleteOpen}
        onClose={() => setIsDeleteOpen(false)}
        onConfirm={handleDelete}
        title="Delete Task"
        message={`Are you sure you want to delete task "${task?.title}"?`}
        confirmText={deleting ? 'Deleting...' : 'Delete Task'}
        isLoading={deleting}
      />

    </div>
  );
};
