import React, { useState } from 'react';
import { Modal } from '../common/Modal';
import { teamApi } from '../../api/teamApi';
import { parseApiError } from '../../api/apiClient';
import { TeamResponse } from '../../types/domain.types';

interface CreateTeamModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: (newTeam: TeamResponse) => void;
}

export const CreateTeamModal: React.FC<CreateTeamModalProps> = ({ isOpen, onClose, onSuccess }) => {
  const [name, setName] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (!name.trim()) {
      setError('Team name is required.');
      return;
    }

    try {
      setIsSubmitting(true);
      const newTeam = await teamApi.createTeam({ name: name.trim() });
      setName('');
      onSuccess(newTeam);
      onClose();
    } catch (err) {
      setError(parseApiError(err));
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Create New Team">
      {error && <div className="alert alert-danger">{error}</div>}

      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label className="form-label" htmlFor="teamName">
            Team Name
          </label>
          <input
            id="teamName"
            type="text"
            className="form-input"
            placeholder="e.g. Core Infrastructure Engine"
            value={name}
            onChange={(e) => setName(e.target.value)}
            required
            disabled={isSubmitting}
          />
        </div>

        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1.5rem' }}>
          <button type="button" className="btn btn-secondary" onClick={onClose} disabled={isSubmitting}>
            Cancel
          </button>
          <button type="submit" className="btn btn-primary" disabled={isSubmitting}>
            {isSubmitting ? 'Creating Team...' : 'Create Team'}
          </button>
        </div>
      </form>
    </Modal>
  );
};
