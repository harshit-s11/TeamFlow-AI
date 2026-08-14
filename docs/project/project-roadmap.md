# Project Roadmap

## Overview

This roadmap outlines the planned development phases for TeamFlow AI.

The roadmap is updated as major milestones are completed.

---

## Phase S0 — Project Setup

**Status:** ✅ Completed

Completed modules:

- [x] S0-1 — GitHub Repository
- [x] S0-2 — Local Development Workspace
- [x] S0-3 — Install & Configure Git
- [x] S0-4 — Install Java 21
- [x] S0-5 — Install & Configure IntelliJ IDEA
- [x] S0-6 — Initialize Spring Boot Backend
- [x] S0-7 — Install & Configure Docker Desktop
- [x] S0-8 — Install & Configure PostgreSQL
- [x] S0-9 — Install & Configure Postman
- [x] S0-10 — Install & Configure Draw.io
- [x] S0-11 — Final Setup Audit & Documentation

---

## Phase S1 — Project Foundation (Backend)

**Status:** ✅ Completed

Completed modules:

- [x] S1-1 — Application Layer Foundation & DTO Infrastructure
- [x] S1-2 — Core Domain Model & Database Schema (`V1__create_core_schema.sql`)
- [x] S1-3 — Repository/Data Access Layer (`JdbcTemplate`)
- [x] S1-4 — Core CRUD APIs (Users, Teams, Projects, Sprints, Tasks)
- [x] S1-5 — Authentication & JWT Security (`V2__add_authentication_fields.sql`, BCrypt, JJWT)
- [x] S1-6 — RBAC & Resource Authorization Model (`USER`/`ADMIN` roles, Team/Project Membership, IDOR protection)

---

## Phase S2 — Frontend & Feature Development

**Status:** ✅ Completed

Completed modules:

- [x] **S2-1 — React Frontend Foundation & Authentication Flow** (Commit `3bbd261`)
  - React + Vite + TypeScript frontend initialization
  - React Router DOM SPA navigation & layout
  - Axios API client with Bearer token request interceptor & error response handling
  - JWT `tokenStorage` abstraction using `localStorage`
  - `AuthContext` user session management (`login`, `register`, `logout`)
  - `PublicRoute` and `ProtectedRoute` client-side route guards
  - Login (`/login`) and Register (`/register`) views
  - Protected Dashboard (`/`) & System Health (`/health`) components
  - Custom 404 Not Found fallback view
  - Spring Boot backend CORS configuration for `http://localhost:5173`
  - Vitest test suite setup & production bundle build verification

- [x] **S2-2 — Team & Project Management UI** (Commit `9112df0`)
  - Team, Project, and User API service modules (`teamApi.ts`, `projectApi.ts`, `userApi.ts`)
  - Team and Project TypeScript DTO request/response models
  - Reusable UI primitives (`Modal`, `ConfirmDialog`, `LoadingSpinner`, `EmptyState`)
  - Teams list view (`/teams`) & Team detail / roster view (`/teams/:id`)
  - Team creation modal, member invitation modal, member removal, and team deletion
  - Projects list view (`/projects`) & Project detail / roster view (`/projects/:id`)
  - Project creation modal, member invitation modal, member removal, and project deletion
  - `ADMIN` user lookup for member selection
  - Route & Navbar integration for Teams and Projects
  - Automated Vitest service unit tests and live browser verification

- [x] **S2-3 — Sprint Planning & Task Kanban UI** (Commit `6c00705`)
  - Sprint API service module (`sprintApi.ts`) & Task API service module (`taskApi.ts`)
  - Sprint and Task TypeScript DTO request/response models
  - Sprint list & creation views (`/sprints`, `/projects/:id/sprints`)
  - Sprint detail view (`/sprints/:id`) & sprint-scoped tasks roster
  - Task list & creation views (`/tasks`, `/projects/:id/tasks`)
  - Task detail view (`/tasks/:id`) with edit/delete modals
  - Interactive 4-column Task Kanban board (`/projects/:id/kanban`) for status columns (`TODO`, `IN_PROGRESS`, `IN_REVIEW`, `DONE`)
  - Task status transition controls consuming existing `PUT /api/v1/tasks/{id}` endpoint
  - Route & Navbar integration for Sprints, Tasks, and Project Kanban
  - Vitest unit tests (`sprintApi.test.ts`, `taskApi.test.ts`) passing 9/9 tests cleanly

---

## Phase S3 — DevOps & Deployment Foundation

**Status:** ✅ Completed

Completed modules:

- [x] **S3-1 — DevOps & Multi-Container Dockerization** (Commit `8606c79`)
  - PostgreSQL 16+ database container (`postgres:16-alpine`) with persistent volume storage (`teamflow_postgres_data`)
  - Multi-stage Spring Boot Java 21 backend `Dockerfile` running as non-root user `teamflow`
  - Production React/Vite NGINX frontend `Dockerfile` serving static SPA bundle
  - Multi-container `docker-compose.yml` orchestrating database, backend, and frontend
  - Explicit container health check definitions (`CMD-SHELL` `pg_isready` for DB, `curl` health check for backend, `curl` port 80 check for frontend) and startup dependencies (`service_healthy`)
  - Security environment variable injection (`TEAMFLOW_DB_USERNAME`, `TEAMFLOW_DB_PASSWORD`, `TEAMFLOW_JWT_SECRET`, `TEAMFLOW_JWT_EXPIRATION`) via `.env.example`
  - CORS configuration update permitting `http://localhost` and `http://localhost:80`
  - Deployment guide creation (`docs/deployment/docker-deployment-guide.md`)
  - Verification of end-to-end containerized registration, authentication, Teams, Projects, Sprints, Tasks, and Kanban status transitions

---

## Future Phase Planning

### Potential Future Directions (PROPOSED / UNCOMMITTED)

The following areas represent candidate directions for future project phases:

- **Advanced Task Management**: Task status state machine transitions, priority escalation, activity audit logging.
- **AI Integration Phase**: LLM-powered sprint velocity forecasting, task breakdown generation, and automated standup summaries.