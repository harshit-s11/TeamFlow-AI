import React, { useState, useEffect, useCallback } from 'react';
import { aiApi } from '../../api/aiApi';
import { parseApiError } from '../../api/apiClient';
import { StandupSummaryResponse } from '../../types/ai.types';

interface StandupSummaryModalProps {
  isOpen: boolean;
  projectId: string;
  onClose: () => void;
}

export const StandupSummaryModal: React.FC<StandupSummaryModalProps> = ({
  isOpen,
  projectId,
  onClose,
}) => {
  const [summaryData, setSummaryData] = useState<StandupSummaryResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<'summary' | 'completed' | 'inprogress' | 'blockers'>('summary');
  const [copied, setCopied] = useState(false);

  const fetchStandupSummary = useCallback(async () => {
    if (!projectId) return;
    try {
      setLoading(true);
      setError(null);
      const data = await aiApi.generateStandupSummary(projectId, 24);
      setSummaryData(data);
    } catch (err) {
      setError(parseApiError(err));
    } finally {
      setLoading(false);
    }
  }, [projectId]);

  useEffect(() => {
    if (isOpen) {
      fetchStandupSummary();
    }
  }, [isOpen, fetchStandupSummary]);

  if (!isOpen) return null;

  const handleCopy = () => {
    if (summaryData?.generatedSummary) {
      navigator.clipboard.writeText(summaryData.generatedSummary);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal-content" style={{ maxWidth: '750px' }} onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>🤖 AI Daily Standup Summary (24h)</h2>
          <button className="btn-icon" onClick={onClose}>&times;</button>
        </div>

        <div className="modal-body">
          {error && <div className="alert alert-danger mb-3">{error}</div>}

          {loading && (
            <div style={{ textAlign: 'center', padding: '3rem' }}>
              <div className="spinner mb-2"></div>
              <p>Aggregating project task logs and generating daily standup report...</p>
            </div>
          )}

          {summaryData && !loading && (
            <div>
              <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '1rem', borderBottom: '1px solid var(--border-color)', paddingBottom: '0.5rem' }}>
                <button
                  className={`btn btn-sm ${activeTab === 'summary' ? 'btn-primary' : 'btn-secondary'}`}
                  onClick={() => setActiveTab('summary')}
                >
                  📝 Markdown Report
                </button>
                <button
                  className={`btn btn-sm ${activeTab === 'completed' ? 'btn-primary' : 'btn-secondary'}`}
                  onClick={() => setActiveTab('completed')}
                >
                  ✅ Completed ({summaryData.completedWork.length})
                </button>
                <button
                  className={`btn btn-sm ${activeTab === 'inprogress' ? 'btn-primary' : 'btn-secondary'}`}
                  onClick={() => setActiveTab('inprogress')}
                >
                  🔄 In Progress ({summaryData.inProgressWork.length})
                </button>
                <button
                  className={`btn btn-sm ${activeTab === 'blockers' ? 'btn-primary' : 'btn-secondary'}`}
                  onClick={() => setActiveTab('blockers')}
                >
                  ⚠️ Blockers ({summaryData.blockersAndRisks.length})
                </button>
              </div>

              {activeTab === 'summary' && (
                <div>
                  <pre
                    style={{
                      backgroundColor: 'var(--bg-secondary)',
                      padding: '1rem',
                      borderRadius: 'var(--radius-md)',
                      whiteSpace: 'pre-wrap',
                      fontFamily: 'monospace',
                      fontSize: '0.875rem',
                      maxHeight: '350px',
                      overflowY: 'auto',
                    }}
                  >
                    {summaryData.generatedSummary}
                  </pre>
                  <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '0.5rem' }}>
                    <button className="btn btn-secondary btn-sm" onClick={handleCopy}>
                      {copied ? 'Copied to Clipboard! ✓' : '📋 Copy Markdown'}
                    </button>
                  </div>
                </div>
              )}

              {activeTab === 'completed' && (
                <ul style={{ paddingLeft: '1.25rem' }}>
                  {summaryData.completedWork.map((item, idx) => (
                    <li key={idx} style={{ marginBottom: '0.5rem' }}>{item}</li>
                  ))}
                </ul>
              )}

              {activeTab === 'inprogress' && (
                <ul style={{ paddingLeft: '1.25rem' }}>
                  {summaryData.inProgressWork.map((item, idx) => (
                    <li key={idx} style={{ marginBottom: '0.5rem' }}>{item}</li>
                  ))}
                </ul>
              )}

              {activeTab === 'blockers' && (
                <ul style={{ paddingLeft: '1.25rem' }}>
                  {summaryData.blockersAndRisks.map((item, idx) => (
                    <li key={idx} style={{ marginBottom: '0.5rem', color: 'var(--text-danger)' }}>{item}</li>
                  ))}
                </ul>
              )}
            </div>
          )}
        </div>

        <div className="modal-footer">
          <button className="btn btn-secondary" onClick={onClose}>
            Close
          </button>
          <button className="btn btn-primary" onClick={fetchStandupSummary} disabled={loading}>
            Refresh Report
          </button>
        </div>
      </div>
    </div>
  );
};
