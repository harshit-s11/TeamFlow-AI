import React from 'react';
import { Routes, Route } from 'react-router-dom';
import { PublicRoute } from '../components/guard/PublicRoute';
import { ProtectedRoute } from '../components/guard/ProtectedRoute';
import { MainLayout } from '../components/layout/MainLayout';
import { LoginPage } from '../pages/LoginPage';
import { RegisterPage } from '../pages/RegisterPage';
import { DashboardPage } from '../pages/DashboardPage';
import { HealthCheck } from '../pages/HealthCheck';
import { NotFoundPage } from '../pages/NotFoundPage';
import { TeamsPage } from '../pages/TeamsPage';
import { TeamDetailPage } from '../pages/TeamDetailPage';
import { ProjectsPage } from '../pages/ProjectsPage';
import { ProjectDetailPage } from '../pages/ProjectDetailPage';
import { SprintsPage } from '../pages/SprintsPage';
import { SprintDetailPage } from '../pages/SprintDetailPage';
import { TasksPage } from '../pages/TasksPage';
import { TaskDetailPage } from '../pages/TaskDetailPage';
import { ProjectKanbanPage } from '../pages/ProjectKanbanPage';

export const AppRoutes: React.FC = () => {
  return (
    <MainLayout>
      <Routes>
        {/* Public Routes */}
        <Route
          path="/login"
          element={
            <PublicRoute>
              <LoginPage />
            </PublicRoute>
          }
        />
        <Route
          path="/register"
          element={
            <PublicRoute>
              <RegisterPage />
            </PublicRoute>
          }
        />
        <Route path="/health" element={<HealthCheck />} />

        {/* Protected Routes */}
        <Route
          path="/"
          element={
            <ProtectedRoute>
              <DashboardPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/teams"
          element={
            <ProtectedRoute>
              <TeamsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/teams/:id"
          element={
            <ProtectedRoute>
              <TeamDetailPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/projects"
          element={
            <ProtectedRoute>
              <ProjectsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/projects/:id"
          element={
            <ProtectedRoute>
              <ProjectDetailPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/projects/:id/kanban"
          element={
            <ProtectedRoute>
              <ProjectKanbanPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/sprints"
          element={
            <ProtectedRoute>
              <SprintsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/sprints/:id"
          element={
            <ProtectedRoute>
              <SprintDetailPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/tasks"
          element={
            <ProtectedRoute>
              <TasksPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/tasks/:id"
          element={
            <ProtectedRoute>
              <TaskDetailPage />
            </ProtectedRoute>
          }
        />

        {/* 404 Fallback */}
        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </MainLayout>
  );
};
