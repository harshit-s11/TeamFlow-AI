# Development Log

## Purpose

This document records the major milestones completed during the development of TeamFlow AI.

It serves as a chronological engineering log summarizing the objective, outcome, and progression of the project.

---

## S0-1 — GitHub Repository

**Objective**

Create the remote repository and establish version control for the project.

**Completed**

- Created the GitHub repository.
- Configured the initial repository.
- Added the project license and initial README.

**Challenges**

- None.

---

## S0-2 — Local Development Workspace

**Objective**

Set up the local development workspace.

**Completed**

- Created the local project directory.
- Organized the initial folder structure.
- Verified the development workspace.

**Challenges**

- None.

---

## S0-3 — Install & Configure Git

**Objective**

Prepare Git for local development.

**Completed**

- Installed Git.
- Configured Git user information.
- Connected the local repository to GitHub.
- Verified Git operations.

**Challenges**

- None.

---

## S0-4 — Install Java 21

**Objective**

Prepare the Java development environment.

**Completed**

- Installed Java 21.
- Configured JAVA_HOME.
- Verified the Java installation.

**Challenges**

- None.

---

## S0-5 — Install & Configure IntelliJ IDEA

**Objective**

Set up the primary Java IDE.

**Completed**

- Installed IntelliJ IDEA Community Edition.
- Configured the IDE.
- Verified Gradle and Java integration.

**Challenges**

- None.

---

## S0-6 — Initialize Spring Boot Backend

**Objective**

Create the backend application.

**Completed**

- Generated the Spring Boot project.
- Configured Gradle Wrapper.
- Verified successful project startup.

**Challenges**

- None.

---

## S0-7 — Install & Configure Docker Desktop

**Objective**

Prepare the containerization environment.

**Completed**

- Installed Docker Desktop.
- Verified Docker Engine.
- Confirmed container support.

**Challenges**

- None.

---

## S0-8 — Install & Configure PostgreSQL

**Objective**

Prepare the project database.

**Completed**

- Installed PostgreSQL.
- Created the development database.
- Verified database connectivity.

**Challenges**

- None.

---

## S0-9 — Install & Configure Postman

**Objective**

Prepare the API testing environment.

**Completed**

- Installed Postman.
- Created the initial workspace.
- Verified API request execution.

**Challenges**

- None.

---

## S0-10 — Install & Configure Draw.io

**Objective**

Prepare the diagramming environment.

**Completed**

- Installed Draw.io Desktop (diagrams.net).
- Configured recommended diagram libraries.
- Verified diagram creation and saving.

**Challenges**

- None.

---

## S0-11 — Final Setup Audit & Documentation

**Objective**

Verify the complete development environment and establish production-quality project documentation.

**Completed**

- Audited the repository structure.
- Reviewed the documentation layout.
- Updated the project README.
- Standardized engineering documentation.

**Challenges**

- None.

---

## S1-1 — Application Layer Foundation & DTO Baseline

**Commit**: `53a109c feat(backend): implement API and application layer foundation`

**Objective**

Establish base DTO infrastructure and global exception handling for standard REST error formatting.

**Completed**

- Created `ApiErrorResponse` standard error payload representation.
- Implemented `GlobalExceptionHandler` for centralized exception translation (`400`, `404`, `500`).

---

## S1-2 & S1-3 — Core Domain Model & Database Schema

**Commit**: `11c601a feat(backend): implement core domain model and Flyway database migrations`

**Objective**

Define immutable domain record entities and Flyway database schema migration baseline.

**Completed**

- Created domain record models: `User`, `Team`, `Project`, `Sprint`, `Task`.
- Added Flyway migration `V1__create_core_schema.sql` creating `users`, `teams`, `team_members`, `projects`, `project_members`, `sprints`, and `tasks`.
- Implemented Spring `JdbcTemplate` data access repositories.

---

## S1-4 — Core CRUD APIs

**Commits**:
- `623732e feat(backend): implement Users CRUD API and exception handling`
- `e33a66a feat(backend): implement Teams CRUD and Team Membership APIs`
- `65a9b68 feat(backend): implement Projects CRUD and Project Membership APIs`
- `30d64cf feat(backend): implement Sprints CRUD API and project-scoped sprint endpoints`
- `7484988 feat(backend): implement Tasks CRUD API and scoped task endpoints`

**Objective**

Implement full CRUD REST controllers, business services, and scoped child queries across core entities.

**Completed**

- Created Controllers and Services for Users, Teams, Projects, Sprints, and Tasks.
- Implemented junction table membership management for `team_members` and `project_members`.
- Implemented scoped query endpoints (`/projects/{id}/sprints`, `/projects/{id}/tasks`, `/sprints/{id}/tasks`).
- Reached 95 passing automated unit and controller slice tests.

---

## S1-5 — Authentication & JWT Security

**Commits**:
- `1ebac5c feat(backend): add authentication data model and password security foundation`
- `16fdb65 feat(backend): implement JWT authentication and secure REST APIs`

**Objective**

Introduce password security, Flyway `V2` user account schema, and stateless JWT token authentication.

**Completed**

- Added Flyway migration `V2__add_authentication_fields.sql` (`password_hash VARCHAR(255)`, `role VARCHAR(50) DEFAULT 'USER'`).
- Integrated Spring Security 7.0 and JJWT 0.12.6 dependencies.
- Configured `BCryptPasswordEncoder` with strength 12.
- Created `JwtService`, `JwtAuthenticationFilter`, `AuthService`, and `AuthController` (`/api/v1/auth/register`, `/api/v1/auth/login`).
- Configured stateless Spring Security policy, protecting `/api/v1/**` endpoints while permitting auth and health check routes.
- Expanded test suite to 117 passing automated tests.

---

## S1-6 — Role-Based Access Control & Resource Authorization Model

**Commit**: `d6d39b6 feat(backend): implement RBAC and resource authorization model`

**Objective**

Enforce service-layer resource authorization, membership access checks, role-based restrictions (`USER` vs `ADMIN`), and IDOR protection.

**Completed**

- Created `SecurityUtils` utility helper for extracting authenticated user identity (`UUID`) and verifying `ADMIN` authorities.
- Updated `UserService`: Restricted full user listing and creation to `ADMIN` only; enforced self-or-admin access on single-user operations.
- Updated `TeamService` & `ProjectService`: Enforced `team_members` / `project_members` or `ADMIN` checks. Auto-enrolled creator as a member upon resource creation within a `@Transactional` boundary.
- Updated `SprintService` & `TaskService`: Enforced project-inherited membership checks (`project_members` of parent project) or `ADMIN` checks across all CRUD operations and scoped routes.
- Updated `GlobalExceptionHandler`: Added `@ExceptionHandler(AccessDeniedException.class)` returning structured `403 Forbidden` (`ApiErrorResponse`).
- Expanded automated integration test suite (`AuthorizationIntegrationTest`) verifying authentication, authorization, IDOR protection, role spoofing prevention, and scoped route enforcement.
- Final test verification result: **140/140 automated tests passed (100% pass rate, 0 failures)**.

---

## S2-1 — React Frontend Foundation & Authentication Flow

**Commit**: `3bbd261 feat(frontend): implement React frontend foundation and authentication flow`

**Objective**

Establish the modern single-page application (SPA) frontend foundation, setting up React 18, Vite 6, TypeScript 5, React Router DOM, Axios HTTP client, token storage abstractions, authentication context, and route protection guards.

**Completed**

- Initialized React 18 + Vite 6 + TypeScript 5 single-page application in `frontend/`.
- Configured Vanilla CSS design system with HSL color tokens, typography scales, card containers, form controls, badges, and alerts in `index.css`.
- Implemented TypeScript DTO models matching Spring Boot schemas (`UserResponse`, `AuthResponse`, `TeamResponse`, `ProjectResponse`, `SprintResponse`, `TaskResponse`, `ApiErrorResponse`).
- Built `tokenStorage.ts` abstraction managing `localStorage` access for JWT bearer tokens and user session data.
- Built `apiClient.ts` using Axios with request `Authorization: Bearer <token>` injection and automated HTTP 401 token clearing interceptor.
- Created `AuthContext.tsx` handling stateless user session lifecycle (`login`, `register`, `logout`).
- Implemented client-side route guards (`PublicRoute` and `ProtectedRoute`).
- Built primary pages and components: `LoginPage`, `RegisterPage`, `DashboardPage`, `HealthCheck`, `NotFoundPage`, and `Navbar`.
- Configured Spring Boot backend CORS support in `SecurityConfig.java` allowing `http://localhost:5173`.
- Added Vitest test suite (`tokenStorage.test.ts`, `apiClient.test.ts`) passing 5/5 tests cleanly.
- Verified TypeScript compilation (`tsc && vite build`) with zero errors.

---

## S2-2 — Team & Project Management UI

**Commit**: `9112df0 feat(frontend): implement team and project management UI`

**Objective**

Build interactive frontend feature pages and components for Team Management and Project Management, integrating with backend REST APIs (`/api/v1/teams`, `/api/v1/projects`, `/api/v1/users`).

**Completed**

- Implemented API service modules (`teamApi.ts`, `projectApi.ts`, `userApi.ts`).
- Created TypeScript request DTOs (`TeamCreateRequest`, `TeamUpdateRequest`, `ProjectCreateRequest`, `ProjectUpdateRequest`, member request models).
- Implemented reusable UI primitives (`Modal`, `ConfirmDialog`, `LoadingSpinner`, `EmptyState`).
- Built Team Management views: `TeamsPage` (`/teams`), `TeamDetailPage` (`/teams/:id`), `TeamCard`, `CreateTeamModal`, and `AddTeamMemberModal`.
- Built Project Management views: `ProjectsPage` (`/projects`), `ProjectDetailPage` (`/projects/:id`), `ProjectCard`, `CreateProjectModal`, and `AddProjectMemberModal`.
- Added `ADMIN` user lookup dropdown for inviting existing system users to teams and projects.
- Integrated Team and Project routes into `AppRoutes.tsx` and `Navbar.tsx`.
- Enforced membership authorization handling in UI (displaying structured `403 Forbidden` error state for non-member detail requests).
- Expanded Vitest test suite (`teamApi.test.ts`, `projectApi.test.ts`) reaching 7/7 tests passing (100% pass rate).
- Verified production build output (`dist/`) and end-to-end browser functionality against Spring Boot API.

---

## S2-3 — Sprint Planning & Task Kanban UI

**Status**: 📋 *PLANNED / NEXT*

**Objective**

Expose the existing secured Sprint and Task backend APIs through an interactive React UI featuring Sprint planning views and a 4-column Task Kanban board (`TODO`, `IN_PROGRESS`, `IN_REVIEW`, `DONE`).

**Planned Scope**

- Sprint API service (`sprintApi.ts`) & Task API service (`taskApi.ts`).
- Sprint & Task DTO request/response TypeScript models (`sprint.types.ts`, `task.types.ts`).
- Sprint management views (`/sprints`, `/sprints/:id`, `/projects/:id/sprints`).
- Task management views (`/tasks`, `/tasks/:id`, `/projects/:id/tasks`).
- Interactive 4-column Task Kanban board (`/projects/:id/kanban`).
- Status transition controls calling existing `PUT /api/v1/tasks/{id}` endpoint.
- Route & Navbar integration for Sprints, Tasks, and Kanban views.
- Vitest tests for API services and Kanban components.

---

## Current Status

- Phase S1 — Backend Foundation: **COMPLETE**
- S2-1 — Frontend Foundation: **COMPLETE** (`3bbd261`)
- S2-2 — Team & Project Management UI: **COMPLETE** (`9112df0`)
- S2-3 — Sprint Planning & Task Kanban UI: **PLANNED / NEXT**