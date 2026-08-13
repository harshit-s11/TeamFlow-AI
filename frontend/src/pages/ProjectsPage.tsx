import React, { useEffect, useState } from 'react';
import { projectApi } from '../api/projectApi';
import { parseApiError } from '../api/apiClient';
import { ProjectResponse } from '../types/domain.types';
import { ProjectCard } from '../components/projects/ProjectCard';
import { CreateProjectModal } from '../components/projects/CreateProjectModal';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import { EmptyState } from '../components/common/EmptyState';

export const ProjectsPage: React.FC = () => {
  const [projects, setProjects] = useState<ProjectResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);

  const fetchProjects = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await projectApi.getAllProjects();
      setProjects(data);
    } catch (err) {
      setError(parseApiError(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProjects();
  }, []);

  const handleProjectCreated = (newProject: ProjectResponse) => {
    setProjects((prev) => [newProject, ...prev]);
  };

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <div>
          <h1 style={{ fontSize: '1.875rem', fontWeight: 700, marginBottom: '0.25rem' }}>Project Management</h1>
          <p style={{ color: 'var(--text-secondary)' }}>View your enrolled projects and manage project workspaces.</p>
        </div>
        <button className="btn btn-primary" onClick={() => setIsModalOpen(true)}>
          + Create Project
        </button>
      </div>

      {error && <div className="alert alert-danger">{error}</div>}

      {loading ? (
        <LoadingSpinner message="Fetching your projects..." />
      ) : projects.length === 0 ? (
        <EmptyState
          title="No Projects Found"
          description="You are currently not enrolled in any projects. Create a project to get started!"
          actionText="+ Create Your First Project"
          onAction={() => setIsModalOpen(true)}
        />
      ) : (
        <div className="grid-3">
          {projects.map((project) => (
            <ProjectCard key={project.id} project={project} />
          ))}
        </div>
      )}

      <CreateProjectModal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} onSuccess={handleProjectCreated} />
    </div>
  );
};
