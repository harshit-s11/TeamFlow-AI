import React, { useEffect, useState } from 'react';
import { teamApi } from '../api/teamApi';
import { parseApiError } from '../api/apiClient';
import { TeamResponse } from '../types/domain.types';
import { TeamCard } from '../components/teams/TeamCard';
import { CreateTeamModal } from '../components/teams/CreateTeamModal';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import { EmptyState } from '../components/common/EmptyState';

export const TeamsPage: React.FC = () => {
  const [teams, setTeams] = useState<TeamResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);

  const fetchTeams = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await teamApi.getAllTeams();
      setTeams(data);
    } catch (err) {
      setError(parseApiError(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTeams();
  }, []);

  const handleTeamCreated = (newTeam: TeamResponse) => {
    setTeams((prev) => [newTeam, ...prev]);
  };

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <div>
          <h1 style={{ fontSize: '1.875rem', fontWeight: 700, marginBottom: '0.25rem' }}>Team Management</h1>
          <p style={{ color: 'var(--text-secondary)' }}>View your enrolled teams and manage team workspaces.</p>
        </div>
        <button className="btn btn-primary" onClick={() => setIsModalOpen(true)}>
          + Create Team
        </button>
      </div>

      {error && <div className="alert alert-danger">{error}</div>}

      {loading ? (
        <LoadingSpinner message="Fetching your teams..." />
      ) : teams.length === 0 ? (
        <EmptyState
          title="No Teams Found"
          description="You are currently not enrolled in any teams. Create a team to get started!"
          actionText="+ Create Your First Team"
          onAction={() => setIsModalOpen(true)}
        />
      ) : (
        <div className="grid-3">
          {teams.map((team) => (
            <TeamCard key={team.id} team={team} />
          ))}
        </div>
      )}

      <CreateTeamModal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} onSuccess={handleTeamCreated} />
    </div>
  );
};
