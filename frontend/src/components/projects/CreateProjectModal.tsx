import React, { useState } from 'react';
import { Modal } from '../common/Modal';
import { projectApi } from '../../api/projectApi';
import { parseApiError } from '../../api/apiClient';
import { ProjectResponse } from '../../types/domain.types';

interface CreateProjectModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: (newProject: ProjectResponse) => void;
}

export const CreateProjectModal: React.FC<CreateProjectModalProps> = ({ isOpen, onClose, onSuccess }) => {
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (!name.trim()) {
      setError('Project name is required.');
      return;
    }

    try {
      setIsSubmitting(true);
      const newProject = await projectApi.createProject({
        name: name.trim(),
        description: description.trim(),
      });
      setName('');
      setDescription('');
      onSuccess(newProject);
      onClose();
    } catch (err) {
      setError(parseApiError(err));
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Create New Project">
      {error && <div className="alert alert-danger">{error}</div>}

      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label className="form-label" htmlFor="projectName">
            Project Name
          </label>
          <input
            id="projectName"
            type="text"
            className="form-input"
            placeholder="e.g. AI Engine v2"
            value={name}
            onChange={(e) => setName(e.target.value)}
            required
            disabled={isSubmitting}
          />
        </div>

        <div className="form-group">
          <label className="form-label" htmlFor="projectDesc">
            Description
          </label>
          <textarea
            id="projectDesc"
            className="form-input"
            style={{ minHeight: '80px', resize: 'vertical' }}
            placeholder="e.g. Next generation core machine learning pipeline"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            disabled={isSubmitting}
          />
        </div>

        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1.5rem' }}>
          <button type="button" className="btn btn-secondary" onClick={onClose} disabled={isSubmitting}>
            Cancel
          </button>
          <button type="submit" className="btn btn-primary" disabled={isSubmitting}>
            {isSubmitting ? 'Creating Project...' : 'Create Project'}
          </button>
        </div>
      </form>
    </Modal>
  );
};
