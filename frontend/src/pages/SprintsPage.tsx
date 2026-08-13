import React, { useEffect, useState } from 'react';
import { sprintApi } from '../api/sprintApi';
import { parseApiError } from '../api/apiClient';
import { SprintResponse } from '../types/domain.types';
import { SprintCard } from '../components/sprints/SprintCard';
import { CreateSprintModal } from '../components/sprints/CreateSprintModal';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import { EmptyState } from '../components/common/EmptyState';

export const SprintsPage: React.FC = () => {
  const [sprints, setSprints] = useState<SprintResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isCreateOpen, setIsCreateOpen] = useState(false);

  const fetchSprints = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await sprintApi.getAllSprints();
      setSprints(data);
    } catch (err) {
      setError(parseApiError(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSprints();
  }, []);

  const handleCreated = (newSprint: SprintResponse) => {
    setSprints((prev) => [newSprint, ...prev]);
  };

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <div>
          <h1 style={{ fontSize: '1.875rem', fontWeight: 700, marginBottom: '0.25rem' }}>Sprint Planning</h1>
          <p style={{ color: 'var(--text-secondary)' }}>View and manage development iteration sprints.</p>
        </div>
        <button className="btn btn-primary" onClick={() => setIsCreateOpen(true)}>
          + Create Sprint
        </button>
      </div>

      {error && <div className="alert alert-danger">{error}</div>}

      {loading ? (
        <LoadingSpinner message="Fetching sprints..." />
      ) : sprints.length === 0 ? (
        <EmptyState
          title="No Sprints Found"
          description="There are currently no active or planned sprints. Create a sprint to start an iteration!"
          actionText="+ Create Your First Sprint"
          onAction={() => setIsCreateOpen(true)}
        />
      ) : (
        <div className="grid-3">
          {sprints.map((sprint) => (
            <SprintCard key={sprint.id} sprint={sprint} />
          ))}
        </div>
      )}

      <CreateSprintModal isOpen={isCreateOpen} onClose={() => setIsCreateOpen(false)} onSuccess={handleCreated} />
    </div>
  );
};
