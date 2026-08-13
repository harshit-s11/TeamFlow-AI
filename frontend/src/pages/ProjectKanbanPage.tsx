import React, { useEffect, useState, useCallback } from 'react';
import { useParams, Link } from 'react-router-dom';
import { projectApi } from '../api/projectApi';
import { taskApi } from '../api/taskApi';
import { parseApiError } from '../api/apiClient';
import { ProjectResponse, TaskResponse, TaskStatus } from '../types/domain.types';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import { KanbanBoard } from '../components/kanban/KanbanBoard';
import { CreateTaskModal } from '../components/tasks/CreateTaskModal';
import { EditTaskModal } from '../components/tasks/EditTaskModal';

export const ProjectKanbanPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();

  const [project, setProject] = useState<ProjectResponse | null>(null);
  const [tasks, setTasks] = useState<TaskResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [taskToEdit, setTaskToEdit] = useState<TaskResponse | null>(null);

  const loadData = useCallback(async () => {
    if (!id) return;
    try {
      setLoading(true);
      setError(null);
      const [projectData, tasksData] = await Promise.all([
        projectApi.getProjectById(id),
        taskApi.getTasksByProjectId(id),
      ]);
      setProject(projectData);
      setTasks(tasksData);
    } catch (err) {
      setError(parseApiError(err));
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const handleStatusChange = async (taskId: string, newStatus: TaskStatus) => {
    const targetTask = tasks.find((t) => t.id === taskId);
    if (!targetTask) return;

    // Optimistic UI update
    setTasks((prev) => prev.map((t) => (t.id === taskId ? { ...t, status: newStatus } : t)));

    try {
      await taskApi.updateTask(taskId, {
        sprintId: targetTask.sprintId,
        assignedUserId: targetTask.assignedUserId,
        title: targetTask.title,
        description: targetTask.description || '',
        status: newStatus,
        priority: targetTask.priority,
      });
    } catch (err) {
      // Revert optimistic update on error
      setTasks((prev) => prev.map((t) => (t.id === taskId ? { ...t, status: targetTask.status } : t)));
      alert(parseApiError(err));
    }
  };

  const handleTaskCreated = (newTask: TaskResponse) => {
    setTasks((prev) => [newTask, ...prev]);
  };

  const handleTaskUpdated = (updatedTask: TaskResponse) => {
    setTasks((prev) => prev.map((t) => (t.id === updatedTask.id ? updatedTask : t)));
  };

  if (loading) {
    return <LoadingSpinner message="Loading Kanban board..." />;
  }

  if (error) {
    return (
      <div style={{ maxWidth: '600px', margin: '2rem auto' }}>
        <div className="alert alert-danger">{error}</div>
        <Link to="/projects" className="btn btn-secondary">
          ← Back to Projects
        </Link>
      </div>
    );
  }

  return (
    <div>
      <div style={{ marginBottom: '1.5rem' }}>
        <Link to={`/projects/${id}`} style={{ color: 'var(--text-secondary)', textDecoration: 'none', fontSize: '0.9rem' }}>
          ← Back to Project Details ({project?.name})
        </Link>
      </div>

      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1 style={{ fontSize: '1.875rem', fontWeight: 700, marginBottom: '0.25rem' }}>
            {project?.name} — Kanban Board
          </h1>
          <p style={{ color: 'var(--text-secondary)' }}>
            Interactive task workflow board ({tasks.length} total tasks)
          </p>
        </div>
        <button className="btn btn-primary" onClick={() => setIsCreateOpen(true)}>
          + Add Task
        </button>
      </div>

      <KanbanBoard tasks={tasks} onStatusChange={handleStatusChange} onEdit={(task) => setTaskToEdit(task)} />

      {id && (
        <CreateTaskModal
          isOpen={isCreateOpen}
          defaultProjectId={id}
          onClose={() => setIsCreateOpen(false)}
          onSuccess={handleTaskCreated}
        />
      )}

      <EditTaskModal
        isOpen={Boolean(taskToEdit)}
        task={taskToEdit}
        onClose={() => setTaskToEdit(null)}
        onSuccess={handleTaskUpdated}
      />
    </div>
  );
};
