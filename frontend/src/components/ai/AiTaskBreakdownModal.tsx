import React, { useState } from 'react';
import { aiApi } from '../../api/aiApi';
import { taskApi } from '../../api/taskApi';
import { parseApiError } from '../../api/apiClient';
import { SuggestedSubtask } from '../../types/ai.types';
import { TaskPriority } from '../../types/domain.types';

interface AiTaskBreakdownModalProps {
  isOpen: boolean;
  taskId: string;
  projectId: string;
  onClose: () => void;
  onSuccess: () => void;
}

export const AiTaskBreakdownModal: React.FC<AiTaskBreakdownModalProps> = ({
  isOpen,
  taskId,
  projectId,
  onClose,
  onSuccess,
}) => {
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [subtasks, setSubtasks] = useState<SuggestedSubtask[]>([]);
  const [selectedIndices, setSelectedIndices] = useState<Set<number>>(new Set());

  if (!isOpen) return null;

  const handleGenerate = async () => {
    try {
      setLoading(true);
      setError(null);
      const res = await aiApi.generateTaskBreakdown(taskId);
      setSubtasks(res.suggestedSubtasks);
      setSelectedIndices(new Set(res.suggestedSubtasks.map((_, i) => i)));
    } catch (err) {
      setError(parseApiError(err));
    } finally {
      setLoading(false);
    }
  };

  const handleToggleSelect = (index: number) => {
    const next = new Set(selectedIndices);
    if (next.has(index)) next.delete(index);
    else next.add(index);
    setSelectedIndices(next);
  };

  const handleSubtaskChange = (index: number, field: keyof SuggestedSubtask, value: any) => {
    const updated = [...subtasks];
    updated[index] = { ...updated[index], [field]: value };
    setSubtasks(updated);
  };

  const handleApprove = async () => {
    const toCreate = subtasks.filter((_, i) => selectedIndices.has(i));
    if (toCreate.length === 0) {
      setError('Please select at least one subtask to create.');
      return;
    }

    try {
      setSaving(true);
      setError(null);

      for (const item of toCreate) {
        await taskApi.createTask({
          projectId,
          title: item.title,
          description: item.description,
          status: 'TODO',
          priority: item.priority,
        });
      }

      onSuccess();
      onClose();
    } catch (err) {
      setError(parseApiError(err));
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal-content" style={{ maxWidth: '700px' }} onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>AI Task Breakdown</h2>
          <button className="btn-icon" onClick={onClose}>&times;</button>
        </div>

        <div className="modal-body">
          {error && <div className="alert alert-danger mb-3">{error}</div>}

          {subtasks.length === 0 && !loading && (
            <div style={{ textAlign: 'center', padding: '2rem 1rem' }}>
              <p style={{ color: 'var(--text-muted)', marginBottom: '1.5rem' }}>
                Use Google Gemini AI to analyze this task and generate a structured breakdown of subtasks.
              </p>
              <button className="btn btn-primary" onClick={handleGenerate}>
                ✨ Generate Subtasks with AI
              </button>
            </div>
          )}

          {loading && (
            <div style={{ textAlign: 'center', padding: '2rem' }}>
              <div className="spinner mb-2"></div>
              <p>Analyzing task requirements with Gemini AI...</p>
            </div>
          )}

          {subtasks.length > 0 && !loading && (
            <div>
              <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)', marginBottom: '1rem' }}>
                Review, edit, and select subtasks before adding them to your project:
              </p>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', maxHeight: '400px', overflowY: 'auto' }}>
                {subtasks.map((st, i) => (
                  <div
                    key={i}
                    style={{
                      border: '1px solid var(--border-color)',
                      borderRadius: 'var(--radius-md)',
                      padding: '0.875rem',
                      backgroundColor: 'var(--bg-secondary)',
                      display: 'flex',
                      gap: '0.75rem',
                      alignItems: 'flex-start',
                    }}
                  >
                    <input
                      type="checkbox"
                      checked={selectedIndices.has(i)}
                      onChange={() => handleToggleSelect(i)}
                      style={{ marginTop: '0.25rem' }}
                    />

                    <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                      <input
                        type="text"
                        className="form-control"
                        value={st.title}
                        onChange={(e) => handleSubtaskChange(i, 'title', e.target.value)}
                        placeholder="Subtask Title"
                        style={{ fontWeight: 600 }}
                      />
                      <textarea
                        className="form-control"
                        value={st.description}
                        onChange={(e) => handleSubtaskChange(i, 'description', e.target.value)}
                        placeholder="Description"
                        rows={2}
                      />
                      <div style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
                        <label style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Priority:</label>
                        <select
                          className="form-control"
                          value={st.priority}
                          onChange={(e) => handleSubtaskChange(i, 'priority', e.target.value as TaskPriority)}
                          style={{ width: 'auto', padding: '0.25rem 0.5rem' }}
                        >
                          <option value="LOW">LOW</option>
                          <option value="MEDIUM">MEDIUM</option>
                          <option value="HIGH">HIGH</option>
                          <option value="URGENT">URGENT</option>
                        </select>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>

        <div className="modal-footer">
          <button className="btn btn-secondary" onClick={onClose} disabled={saving}>
            Cancel
          </button>
          {subtasks.length > 0 && (
            <button className="btn btn-primary" onClick={handleApprove} disabled={saving || loading}>
              {saving ? 'Creating Subtasks...' : `Approve & Create (${selectedIndices.size})`}
            </button>
          )}
        </div>
      </div>
    </div>
  );
};
