import React, { useEffect, useState, useCallback } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { sprintApi } from '../api/sprintApi';
import { parseApiError } from '../api/apiClient';
import { SprintResponse, TaskResponse } from '../types/domain.types';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import { EditSprintModal } from '../components/sprints/EditSprintModal';
import { ConfirmDialog } from '../components/common/ConfirmDialog';
import { TaskCard } from '../components/tasks/TaskCard';
import { formatDate } from '../utils/formatters';

export const SprintDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const [sprint, setSprint] = useState<SprintResponse | null>(null);
  const [tasks, setTasks] = useState<TaskResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [isEditOpen, setIsEditOpen] = useState(false);
  const [isDeleteOpen, setIsDeleteOpen] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);

  const loadSprintData = useCallback(async () => {
    if (!id) return;
    try {
      setLoading(true);
      setError(null);
      const [sprintData, tasksData] = await Promise.all([
        sprintApi.getSprintById(id),
        sprintApi.getTasksBySprintId(id),
      ]);
      setSprint(sprintData);
      setTasks(tasksData);
    } catch (err) {
      setError(parseApiError(err));
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    loadSprintData();
  }, [loadSprintData]);

  const handleDelete = async () => {
    if (!id) return;
    try {
      setIsDeleting(true);
      await sprintApi.deleteSprint(id);
      navigate('/sprints');
    } catch (err) {
      alert(parseApiError(err));
    } finally {
      setIsDeleting(false);
    }
  };

  if (loading) {
    return <LoadingSpinner message="Loading sprint details..." />;
  }

  if (error) {
    return (
      <div style={{ maxWidth: '600px', margin: '2rem auto' }}>
        <div className="alert alert-danger">{error}</div>
        <Link to="/sprints" className="btn btn-secondary">
          ← Back to Sprints
        </Link>
      </div>
    );
  }

  return (
    <div>
      <div style={{ marginBottom: '1.5rem' }}>
        <Link to="/sprints" style={{ color: 'var(--text-secondary)', textDecoration: 'none', fontSize: '0.9rem' }}>
          ← Back to Sprints List
        </Link>
      </div>

      <div className="card" style={{ marginBottom: '2rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '1rem' }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '0.5rem' }}>
              <h1 style={{ fontSize: '1.875rem', fontWeight: 700, margin: 0 }}>{sprint?.name}</h1>
              <span className={sprint?.status === 'ACTIVE' ? 'badge badge-success' : 'badge badge-user'}>
                {sprint?.status}
              </span>
            </div>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
              📅 {sprint?.startDate ? formatDate(sprint.startDate) : ''} – {sprint?.endDate ? formatDate(sprint.endDate) : ''} • Sprint ID: {sprint?.id}
            </p>
          </div>

          <div style={{ display: 'flex', gap: '0.75rem' }}>
            <button className="btn btn-secondary" onClick={() => setIsEditOpen(true)}>
              Edit Sprint
            </button>
            <button className="btn btn-danger" onClick={() => setIsDeleteOpen(true)}>
              Delete Sprint
            </button>
          </div>
        </div>
      </div>

      <div className="card">
        <h2 style={{ fontSize: '1.25rem', fontWeight: 600, marginBottom: '1.5rem' }}>
          Sprint Tasks ({tasks.length})
        </h2>

        {tasks.length === 0 ? (
          <p style={{ color: 'var(--text-secondary)' }}>No tasks assigned to this sprint yet.</p>
        ) : (
          <div className="grid-3">
            {tasks.map((task) => (
              <TaskCard key={task.id} task={task} />
            ))}
          </div>
        )}
      </div>

      <EditSprintModal
        isOpen={isEditOpen}
        sprint={sprint}
        onClose={() => setIsEditOpen(false)}
        onSuccess={(updated) => setSprint(updated)}
      />

      <ConfirmDialog
        isOpen={isDeleteOpen}
        onClose={() => setIsDeleteOpen(false)}
        onConfirm={handleDelete}
        title="Delete Sprint"
        message={`Are you sure you want to delete the sprint "${sprint?.name}"?`}
        confirmText="Delete Sprint"
        isLoading={isDeleting}
      />
    </div>
  );
};
