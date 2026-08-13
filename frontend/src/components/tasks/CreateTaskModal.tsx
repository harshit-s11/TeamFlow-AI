import React, { useState, useEffect } from 'react';
import { Modal } from '../common/Modal';
import { taskApi } from '../../api/taskApi';
import { projectApi } from '../../api/projectApi';
import { sprintApi } from '../../api/sprintApi';
import { parseApiError } from '../../api/apiClient';
import { TaskResponse, ProjectResponse, SprintResponse, TaskStatus, TaskPriority } from '../../types/domain.types';
import { UserResponse } from '../../types/user.types';

interface CreateTaskModalProps {
  isOpen: boolean;
  defaultProjectId?: string;
  defaultSprintId?: string;
  defaultStatus?: TaskStatus;
  onClose: () => void;
  onSuccess: (newTask: TaskResponse) => void;
}

export const CreateTaskModal: React.FC<CreateTaskModalProps> = ({
  isOpen,
  defaultProjectId,
  defaultSprintId,
  defaultStatus = 'TODO',
  onClose,
  onSuccess,
}) => {
  const [projectId, setProjectId] = useState(defaultProjectId || '');
  const [sprintId, setSprintId] = useState(defaultSprintId || '');
  const [assignedUserId, setAssignedUserId] = useState('');
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [status, setStatus] = useState<TaskStatus>(defaultStatus);
  const [priority, setPriority] = useState<TaskPriority>('MEDIUM');

  const [projects, setProjects] = useState<ProjectResponse[]>([]);
  const [sprints, setSprints] = useState<SprintResponse[]>([]);
  const [members, setMembers] = useState<UserResponse[]>([]);

  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (isOpen && !defaultProjectId) {
      projectApi.getAllProjects().then(setProjects).catch(() => {});
    }
  }, [isOpen, defaultProjectId]);

  useEffect(() => {
    const activeProj = defaultProjectId || projectId;
    if (isOpen && activeProj) {
      Promise.all([
        sprintApi.getSprintsByProjectId(activeProj),
        projectApi.getProjectMembers(activeProj),
      ])
        .then(([sprintsData, membersData]) => {
          setSprints(sprintsData);
          setMembers(membersData);
        })
        .catch(() => {});
    }
  }, [isOpen, defaultProjectId, projectId]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    const targetProjectId = defaultProjectId || projectId;

    if (!targetProjectId || !title.trim()) {
      setError('Project ID and Task title are required.');
      return;
    }

    try {
      setIsSubmitting(true);
      const created = await taskApi.createTask({
        projectId: targetProjectId,
        sprintId: sprintId || null,
        assignedUserId: assignedUserId || null,
        title: title.trim(),
        description: description.trim(),
        status,
        priority,
      });
      setTitle('');
      setDescription('');
      onSuccess(created);
      onClose();
    } catch (err) {
      setError(parseApiError(err));
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Create New Task">
      {error && <div className="alert alert-danger">{error}</div>}

      <form onSubmit={handleSubmit}>
        {!defaultProjectId && (
          <div className="form-group">
            <label className="form-label" htmlFor="taskProject">
              Target Project
            </label>
            <select
              id="taskProject"
              className="form-input"
              value={projectId}
              onChange={(e) => setProjectId(e.target.value)}
              required
              disabled={isSubmitting}
            >
              <option value="">-- Choose Project --</option>
              {projects.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.name}
                </option>
              ))}
            </select>
          </div>
        )}

        <div className="form-group">
          <label className="form-label" htmlFor="taskTitle">
            Task Title
          </label>
          <input
            id="taskTitle"
            type="text"
            className="form-input"
            placeholder="e.g. Implement user authentication interceptor"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            required
            disabled={isSubmitting}
          />
        </div>

        <div className="form-group">
          <label className="form-label" htmlFor="taskDescription">
            Description
          </label>
          <textarea
            id="taskDescription"
            className="form-input"
            style={{ minHeight: '70px', resize: 'vertical' }}
            placeholder="Technical details and acceptance criteria..."
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            disabled={isSubmitting}
          />
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
          <div className="form-group">
            <label className="form-label" htmlFor="taskStatus">
              Status
            </label>
            <select
              id="taskStatus"
              className="form-input"
              value={status}
              onChange={(e) => setStatus(e.target.value as TaskStatus)}
              required
              disabled={isSubmitting}
            >
              <option value="TODO">TODO</option>
              <option value="IN_PROGRESS">IN_PROGRESS</option>
              <option value="IN_REVIEW">IN_REVIEW</option>
              <option value="DONE">DONE</option>
            </select>
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="taskPriority">
              Priority
            </label>
            <select
              id="taskPriority"
              className="form-input"
              value={priority}
              onChange={(e) => setPriority(e.target.value as TaskPriority)}
              required
              disabled={isSubmitting}
            >
              <option value="LOW">LOW</option>
              <option value="MEDIUM">MEDIUM</option>
              <option value="HIGH">HIGH</option>
              <option value="URGENT">URGENT</option>
            </select>
          </div>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
          <div className="form-group">
            <label className="form-label" htmlFor="taskSprint">
              Assign to Sprint (Optional)
            </label>
            <select
              id="taskSprint"
              className="form-input"
              value={sprintId}
              onChange={(e) => setSprintId(e.target.value)}
              disabled={isSubmitting || Boolean(defaultSprintId)}
            >
              <option value="">-- Backlog (No Sprint) --</option>
              {sprints.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.name} ({s.status})
                </option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="taskAssignee">
              Assigned Member (Optional)
            </label>
            <select
              id="taskAssignee"
              className="form-input"
              value={assignedUserId}
              onChange={(e) => setAssignedUserId(e.target.value)}
              disabled={isSubmitting}
            >
              <option value="">-- Unassigned --</option>
              {members.map((m) => (
                <option key={m.id} value={m.id}>
                  {m.name} ({m.email})
                </option>
              ))}
            </select>
          </div>
        </div>

        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1.5rem' }}>
          <button type="button" className="btn btn-secondary" onClick={onClose} disabled={isSubmitting}>
            Cancel
          </button>
          <button type="submit" className="btn btn-primary" disabled={isSubmitting}>
            {isSubmitting ? 'Creating Task...' : 'Create Task'}
          </button>
        </div>
      </form>
    </Modal>
  );
};
