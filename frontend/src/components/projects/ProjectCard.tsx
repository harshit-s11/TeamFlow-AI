import React from 'react';
import { Link } from 'react-router-dom';
import { ProjectResponse } from '../../types/domain.types';
import { formatDate } from '../../utils/formatters';

export const ProjectCard: React.FC<{ project: ProjectResponse }> = ({ project }) => {
  return (
    <div className="card" style={{ display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
      <div>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '0.75rem' }}>
          <h3 style={{ fontSize: '1.15rem', fontWeight: 600, color: 'var(--text-primary)' }}>{project.name}</h3>
          <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{formatDate(project.createdAt)}</span>
        </div>
        <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', marginBottom: '1.25rem', display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden' }}>
          {project.description || 'No description provided.'}
        </p>
      </div>

      <div style={{ borderTop: '1px solid var(--border-subtle)', paddingTop: '1rem', display: 'flex', justifyContent: 'flex-end' }}>
        <Link to={`/projects/${project.id}`} className="btn btn-secondary" style={{ padding: '0.4rem 0.85rem', fontSize: '0.85rem' }}>
          View Details & Roster →
        </Link>
      </div>
    </div>
  );
};
