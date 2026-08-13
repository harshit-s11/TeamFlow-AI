import React, { useEffect, useState, useCallback } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { teamApi } from '../api/teamApi';
import { parseApiError } from '../api/apiClient';
import { TeamResponse } from '../types/domain.types';
import { UserResponse } from '../types/user.types';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import { AddTeamMemberModal } from '../components/teams/AddTeamMemberModal';
import { ConfirmDialog } from '../components/common/ConfirmDialog';
import { formatDate } from '../utils/formatters';

export const TeamDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const [team, setTeam] = useState<TeamResponse | null>(null);
  const [members, setMembers] = useState<UserResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [isAddMemberOpen, setIsAddMemberOpen] = useState(false);
  const [userToRemove, setUserToRemove] = useState<UserResponse | null>(null);
  const [isDeleteTeamOpen, setIsDeleteTeamOpen] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);

  const loadTeamData = useCallback(async () => {
    if (!id) return;
    try {
      setLoading(true);
      setError(null);
      const [teamData, membersData] = await Promise.all([
        teamApi.getTeamById(id),
        teamApi.getTeamMembers(id),
      ]);
      setTeam(teamData);
      setMembers(membersData);
    } catch (err) {
      setError(parseApiError(err));
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    loadTeamData();
  }, [loadTeamData]);

  const handleRemoveMember = async () => {
    if (!id || !userToRemove) return;
    try {
      await teamApi.removeTeamMember(id, userToRemove.id);
      setMembers((prev) => prev.filter((m) => m.id !== userToRemove.id));
      setUserToRemove(null);
    } catch (err) {
      alert(parseApiError(err));
    }
  };

  const handleDeleteTeam = async () => {
    if (!id) return;
    try {
      setIsDeleting(true);
      await teamApi.deleteTeam(id);
      navigate('/teams');
    } catch (err) {
      alert(parseApiError(err));
    } finally {
      setIsDeleting(false);
    }
  };

  if (loading) {
    return <LoadingSpinner message="Loading team details..." />;
  }

  if (error) {
    return (
      <div style={{ maxWidth: '600px', margin: '2rem auto' }}>
        <div className="alert alert-danger">{error}</div>
        <Link to="/teams" className="btn btn-secondary">
          ← Back to Teams
        </Link>
      </div>
    );
  }

  return (
    <div>
      <div style={{ marginBottom: '1.5rem' }}>
        <Link to="/teams" style={{ color: 'var(--text-secondary)', textDecoration: 'none', fontSize: '0.9rem' }}>
          ← Back to Teams List
        </Link>
      </div>

      <div className="card" style={{ marginBottom: '2rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '1rem' }}>
          <div>
            <h1 style={{ fontSize: '1.875rem', fontWeight: 700, marginBottom: '0.25rem' }}>{team?.name}</h1>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
              Created on {team?.createdAt ? formatDate(team.createdAt) : 'N/A'} • Team ID: {team?.id}
            </p>
          </div>
          <button className="btn btn-danger" onClick={() => setIsDeleteTeamOpen(true)}>
            Delete Team
          </button>
        </div>
      </div>

      <div className="card">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
          <h2 style={{ fontSize: '1.25rem', fontWeight: 600 }}>Team Roster ({members.length})</h2>
          <button className="btn btn-primary" onClick={() => setIsAddMemberOpen(true)}>
            + Add Member
          </button>
        </div>

        {members.length === 0 ? (
          <p style={{ color: 'var(--text-secondary)' }}>No members found in this team roster.</p>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
            {members.map((member) => (
              <div
                key={member.id}
                style={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  padding: '0.85rem 1rem',
                  backgroundColor: 'var(--bg-secondary)',
                  borderRadius: 'var(--radius-sm)',
                  border: '1px solid var(--border-subtle)',
                }}
              >
                <div>
                  <div style={{ fontWeight: 600, fontSize: '0.95rem' }}>{member.name}</div>
                  <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
                    {member.email} • Role:{' '}
                    <span className={member.role === 'ADMIN' ? 'badge badge-admin' : 'badge badge-user'}>
                      {member.role}
                    </span>
                  </div>
                </div>
                <button
                  className="btn btn-secondary"
                  style={{ color: 'var(--accent-danger)', padding: '0.35rem 0.75rem', fontSize: '0.8rem' }}
                  onClick={() => setUserToRemove(member)}
                >
                  Remove
                </button>
              </div>
            ))}
          </div>
        )}
      </div>

      {id && (
        <AddTeamMemberModal
          isOpen={isAddMemberOpen}
          teamId={id}
          existingMemberIds={members.map((m) => m.id)}
          onClose={() => setIsAddMemberOpen(false)}
          onSuccess={(newMember) => setMembers((prev) => [...prev, newMember])}
        />
      )}

      <ConfirmDialog
        isOpen={Boolean(userToRemove)}
        onClose={() => setUserToRemove(null)}
        onConfirm={handleRemoveMember}
        title="Remove Team Member"
        message={`Are you sure you want to remove ${userToRemove?.name} (${userToRemove?.email}) from this team?`}
        confirmText="Remove Member"
      />

      <ConfirmDialog
        isOpen={isDeleteTeamOpen}
        onClose={() => setIsDeleteTeamOpen(false)}
        onConfirm={handleDeleteTeam}
        title="Delete Team Workspace"
        message={`Are you sure you want to delete the team "${team?.name}"? This action cannot be undone.`}
        confirmText="Delete Team"
        isLoading={isDeleting}
      />
    </div>
  );
};
