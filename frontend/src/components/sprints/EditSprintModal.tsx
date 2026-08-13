import React, { useState, useEffect } from 'react';
import { Modal } from '../common/Modal';
import { sprintApi } from '../../api/sprintApi';
import { parseApiError } from '../../api/apiClient';
import { SprintResponse, SprintStatus } from '../../types/domain.types';

interface EditSprintModalProps {
  isOpen: boolean;
  sprint: SprintResponse | null;
  onClose: () => void;
  onSuccess: (updatedSprint: SprintResponse) => void;
}

export const EditSprintModal: React.FC<EditSprintModalProps> = ({ isOpen, sprint, onClose, onSuccess }) => {
  const [name, setName] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [status, setStatus] = useState<SprintStatus>('PLANNED');

  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (sprint) {
      setName(sprint.name);
      setStartDate(sprint.startDate);
      setEndDate(sprint.endDate);
      setStatus(sprint.status);
    }
  }, [sprint]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!sprint) return;
    setError(null);

    if (!name.trim() || !startDate || !endDate) {
      setError('All fields are required.');
      return;
    }

    try {
      setIsSubmitting(true);
      const updated = await sprintApi.updateSprint(sprint.id, {
        name: name.trim(),
        startDate,
        endDate,
        status,
      });
      onSuccess(updated);
      onClose();
    } catch (err) {
      setError(parseApiError(err));
    } finally {
      setIsSubmitting(false);
    }
  };

  if (!sprint) return null;

  return (
    <Modal isOpen={isOpen} onClose={onClose} title={`Edit ${sprint.name}`}>
      {error && <div className="alert alert-danger">{error}</div>}

      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label className="form-label" htmlFor="editSprintName">
            Sprint Name
          </label>
          <input
            id="editSprintName"
            type="text"
            className="form-input"
            value={name}
            onChange={(e) => setName(e.target.value)}
            required
            disabled={isSubmitting}
          />
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
          <div className="form-group">
            <label className="form-label" htmlFor="editStartDate">
              Start Date
            </label>
            <input
              id="editStartDate"
              type="date"
              className="form-input"
              value={startDate}
              onChange={(e) => setStartDate(e.target.value)}
              required
              disabled={isSubmitting}
            />
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="editEndDate">
              End Date
            </label>
            <input
              id="editEndDate"
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
          <label className="form-label" htmlFor="editSprintStatus">
            Status
          </label>
          <select
            id="editSprintStatus"
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
            {isSubmitting ? 'Saving Changes...' : 'Save Changes'}
          </button>
        </div>
      </form>
    </Modal>
  );
};
