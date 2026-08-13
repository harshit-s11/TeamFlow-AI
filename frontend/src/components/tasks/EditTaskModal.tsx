import React, { useState, useEffect } from 'react';
import { Modal } from '../common/Modal';
import { taskApi } from '../../api/taskApi';
import { projectApi } from '../../api/projectApi';
import { sprintApi } from '../../api/sprintApi';
import { parseApiError } from '../../api/apiClient';
import { TaskResponse, SprintResponse, TaskStatus, TaskPriority } from '../../types/domain.types';
import { UserResponse } from '../../types/user.types';

interface EditTaskModalProps {
  isOpen: boolean;
  task: TaskResponse | null;
  onClose: () => void;
  onSuccess: (updatedTask: TaskResponse) => void;
}

export const EditTaskModal: React.FC<EditTaskModalProps> = ({ isOpen, task, onClose, onSuccess }) => {
  const [sprintId, setSprintId] = useState('');
  const [assignedUserId, setAssignedUserId] = useState('');
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [status, setStatus] = useState<TaskStatus>('TODO');
  const [priority, setPriority] = useState<TaskPriority>('MEDIUM');

  const [sprints, setSprints] = useState<SprintResponse[]>([]);
  const [members, setMembers] = useState<UserResponse[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (task) {
      setTitle(task.title);
      setDescription(task.description || '');
      setStatus(task.status);
      setPriority(task.priority);
      setSprintId(task.sprintId || '');
      setAssignedUserId(task.assignedUserId || '');

      Promise.all([
        sprintApi.getSprintsByProjectId(task.projectId),
        projectApi.getProjectMembers(task.projectId),
      ])
        .then(([sprintsData, membersData]) => {
          setSprints(sprintsData);
          setMembers(membersData);
        })
        .catch(() => {});
    }
  }, [task]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!task) return;
    setError(null);

    if (!title.trim()) {
      setError('Task title is required.');
      return;
    }

    try {
      setIsSubmitting(true);
      const updated = await taskApi.updateTask(task.id, {
        sprintId: sprintId || null,
        assignedUserId: assignedUserId || null,
        title: title.trim(),
        description: description.trim(),
        status,
        priority,
      });
      onSuccess(updated);
      onClose();
    } catch (err) {
      setError(parseApiError(err));
    } finally {
      setIsSubmitting(false);
    }
  };

  if (!task) return null;

  return (
    <Modal isOpen={isOpen} onClose={onClose} title={`Edit ${task.title}`}>
      {error && <div className="alert alert-danger">{error}</div>}

      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label className="form-label" htmlFor="editTaskTitle">
            Task Title
          </label>
          <input
            id="editTaskTitle"
            type="text"
            className="form-input"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            required
            disabled={isSubmitting}
          />
        </div>

        <div className="form-group">
          <label className="form-label" htmlFor="editTaskDescription">
            Description
          </label>
          <textarea
            id="editTaskDescription"
            className="form-input"
            style={{ minHeight: '70px', resize: 'vertical' }}
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            disabled={isSubmitting}
          />
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
          <div className="form-group">
            <label className="form-label" htmlFor="editTaskStatus">
              Status
            </label>
            <select
              id="editTaskStatus"
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
            <label className="form-label" htmlFor="editTaskPriority">
              Priority
            </label>
            <select
              id="editTaskPriority"
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
            <label className="form-label" htmlFor="editTaskSprint">
              Assign to Sprint
            </label>
            <select
              id="editTaskSprint"
              className="form-input"
              value={sprintId}
              onChange={(e) => setSprintId(e.target.value)}
              disabled={isSubmitting}
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
            <label className="form-label" htmlFor="editTaskAssignee">
              Assigned Member
            </label>
            <select
              id="editTaskAssignee"
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
            {isSubmitting ? 'Saving Changes...' : 'Save Changes'}
          </button>
        </div>
      </form>
    </Modal>
  );
};
