import React, { useState, useEffect } from 'react';
import { Modal } from '../common/Modal';
import { teamApi } from '../../api/teamApi';
import { userApi } from '../../api/userApi';
import { parseApiError } from '../../api/apiClient';
import { UserResponse } from '../../types/user.types';
import { useAuth } from '../../context/AuthContext';

interface AddTeamMemberModalProps {
  isOpen: boolean;
  teamId: string;
  existingMemberIds: string[];
  onClose: () => void;
  onSuccess: (newMember: UserResponse) => void;
}

export const AddTeamMemberModal: React.FC<AddTeamMemberModalProps> = ({
  isOpen,
  teamId,
  existingMemberIds,
  onClose,
  onSuccess,
}) => {
  const [userIdInput, setUserIdInput] = useState('');
  const [usersList, setUsersList] = useState<UserResponse[]>([]);
  const [selectedUserId, setSelectedUserId] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isLoadingUsers, setIsLoadingUsers] = useState(false);

  const { isAdmin } = useAuth();

  useEffect(() => {
    if (isOpen && isAdmin) {
      setIsLoadingUsers(true);
      userApi
        .getAllUsers()
        .then((users) => {
          setUsersList(users.filter((u) => !existingMemberIds.includes(u.id)));
        })
        .catch(() => {
          // If not permitted or error, fallback to manual UUID input
        })
        .finally(() => setIsLoadingUsers(false));
    }
  }, [isOpen, isAdmin, existingMemberIds]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    const targetId = isAdmin && selectedUserId ? selectedUserId : userIdInput.trim();

    if (!targetId) {
      setError('Please select or enter a valid User ID (UUID).');
      return;
    }

    try {
      setIsSubmitting(true);
      const newMember = await teamApi.addTeamMember(teamId, targetId);
      setUserIdInput('');
      setSelectedUserId('');
      onSuccess(newMember);
      onClose();
    } catch (err) {
      setError(parseApiError(err));
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Add Member to Team">
      {error && <div className="alert alert-danger">{error}</div>}

      <form onSubmit={handleSubmit}>
        {isAdmin && usersList.length > 0 ? (
          <div className="form-group">
            <label className="form-label" htmlFor="userSelect">
              Select User to Invite
            </label>
            <select
              id="userSelect"
              className="form-input"
              value={selectedUserId}
              onChange={(e) => setSelectedUserId(e.target.value)}
              disabled={isSubmitting || isLoadingUsers}
              required
            >
              <option value="">-- Choose User --</option>
              {usersList.map((u) => (
                <option key={u.id} value={u.id}>
                  {u.name} ({u.email}) — {u.role}
                </option>
              ))}
            </select>
          </div>
        ) : (
          <div className="form-group">
            <label className="form-label" htmlFor="userId">
              User ID (UUID)
            </label>
            <input
              id="userId"
              type="text"
              className="form-input"
              placeholder="e.g. 123e4567-e89b-12d3-a456-426614174000"
              value={userIdInput}
              onChange={(e) => setUserIdInput(e.target.value)}
              required
              disabled={isSubmitting}
            />
          </div>
        )}

        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1.5rem' }}>
          <button type="button" className="btn btn-secondary" onClick={onClose} disabled={isSubmitting}>
            Cancel
          </button>
          <button type="submit" className="btn btn-primary" disabled={isSubmitting}>
            {isSubmitting ? 'Adding Member...' : 'Add Member'}
          </button>
        </div>
      </form>
    </Modal>
  );
};
