import React, { useState, useEffect } from 'react';
import { Modal } from '../common/Modal';
import { sprintApi } from '../../api/sprintApi';
import { projectApi } from '../../api/projectApi';
import { parseApiError } from '../../api/apiClient';
import { SprintResponse, ProjectResponse, SprintStatus } from '../../types/domain.types';

interface CreateSprintModalProps {
  isOpen: boolean;
  defaultProjectId?: string;
  onClose: () => void;
  onSuccess: (newSprint: SprintResponse) => void;
}

export const CreateSprintModal: React.FC<CreateSprintModalProps> = ({
  isOpen,
  defaultProjectId,
  onClose,
  onSuccess,
}) => {
  const [projectId, setProjectId] = useState(defaultProjectId || '');
  const [name, setName] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [status, setStatus] = useState<SprintStatus>('PLANNED');

  const [projects, setProjects] = useState<ProjectResponse[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (isOpen && !defaultProjectId) {
      projectApi.getAllProjects().then(setProjects).catch(() => {});
    }
  }, [isOpen, defaultProjectId]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    const targetProjectId = defaultProjectId || projectId;

    if (!targetProjectId || !name.trim() || !startDate || !endDate) {
      setError('All fields are required.');
      return;
    }

    try {
      setIsSubmitting(true);
      const created = await sprintApi.createSprint({
        projectId: targetProjectId,
        name: name.trim(),
        startDate,
        endDate,
        status,
      });
      setName('');
      setStartDate('');
      setEndDate('');
      onSuccess(created);
      onClose();
    } catch (err) {
      setError(parseApiError(err));
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Create New Sprint">
      {error && <div className="alert alert-danger">{error}</div>}

      <form onSubmit={handleSubmit}>
        {!defaultProjectId && (
          <div className="form-group">
            <label className="form-label" htmlFor="sprintProject">
              Select Target Project
            </label>
            <select
              id="sprintProject"
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
          <label className="form-label" htmlFor="sprintName">
            Sprint Name
          </label>
          <input
            id="sprintName"
            type="text"
            className="form-input"
            placeholder="e.g. Sprint 1 - Auth & Core Setup"
            value={name}
            onChange={(e) => setName(e.target.value)}
            required
            disabled={isSubmitting}
          />
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
          <div className="form-group">
            <label className="form-label" htmlFor="startDate">
              Start Date
            </label>
            <input
              id="startDate"
              type="date"
              className="form-input"
              value={startDate}
              onChange={(e) => setStartDate(e.target.value)}
              required
              disabled={isSubmitting}
            />
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="endDate">
              End Date
            </label>
            <input
              id="endDate"
              type="date"
              className="form-input"
              value={endDate}
              onChange={(e) => setEndDate(e.target.value)}
              required
              disabled={isSubmitting}
            />
          </div>
        </div>

        <div className="form-group">
          <label className="form-label" htmlFor="sprintStatus">
            Status
          </label>
          <select
            id="sprintStatus"
            className="form-input"
            value={status}
            onChange={(e) => setStatus(e.target.value as SprintStatus)}
            required
            disabled={isSubmitting}
          >
            <option value="PLANNED">PLANNED</option>
            <option value="ACTIVE">ACTIVE</option>
            <option value="COMPLETED">COMPLETED</option>
          </select>
        </div>

        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1.5rem' }}>
          <button type="button" className="btn btn-secondary" onClick={onClose} disabled={isSubmitting}>
            Cancel
          </button>
          <button type="submit" className="btn btn-primary" disabled={isSubmitting}>
            {isSubmitting ? 'Creating Sprint...' : 'Create Sprint'}
          </button>
        </div>
      </form>
    </Modal>
  );
};
