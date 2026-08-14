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

**Commit**: `6c00705 feat(frontend): implement sprint planning and task kanban UI`

**Objective**

Expose the existing secured Sprint and Task backend APIs through an interactive React UI featuring Sprint planning views and a 4-column Task Kanban board (`TODO`, `IN_PROGRESS`, `IN_REVIEW`, `DONE`).

**Completed**

- Created Sprint API service (`sprintApi.ts`) & Task API service (`taskApi.ts`).
- Defined Sprint & Task request DTO TypeScript models (`sprint.types.ts`, `task.types.ts`).
- Built Sprint views: `SprintsPage` (`/sprints`), `SprintDetailPage` (`/sprints/:id`), `SprintCard`, `CreateSprintModal`, and `EditSprintModal`.
- Built Task views: `TasksPage` (`/tasks`), `TaskDetailPage` (`/tasks/:id`), `TaskCard`, `CreateTaskModal`, and `EditTaskModal`.
- Built interactive 4-column Task Kanban board (`/projects/:id/kanban`) for status columns (`TODO`, `IN_PROGRESS`, `IN_REVIEW`, `DONE`).
- Integrated status transition controls calling existing `PUT /api/v1/tasks/{id}` endpoint.
- Integrated Sprints, Tasks, and Kanban routes into `AppRoutes.tsx` and `Navbar.tsx`.
- Added unit tests (`sprintApi.test.ts`, `taskApi.test.ts`) reaching 9/9 passing Vitest tests (100% pass rate).
- Verified TypeScript compilation (`tsc && vite build`) and production bundle output with zero errors.

---

## S3-1 — DevOps & Multi-Container Dockerization

**Commit**: `8606c79 feat(devops): implement Docker Compose deployment environment`

**Objective**

Orchestrate PostgreSQL 16+, Spring Boot 4.1.0 / Java 21 backend, and React 18 / Vite NGINX frontend into a reproducible, multi-container Docker Compose environment.

**Completed**

- Created multi-stage Java 21 Spring Boot backend `Dockerfile` running under unprivileged non-root user `teamflow` (`backend/Dockerfile`, `backend/.dockerignore`).
- Created multi-stage React 18 / Vite NGINX frontend `Dockerfile` with SPA fallback routing (`frontend/Dockerfile`, `frontend/nginx.conf`, `frontend/.dockerignore`).
- Created `docker-compose.yml` orchestrating `db` (`postgres:16-alpine`), `backend`, and `frontend` on `teamflow-network` bridge network.
- Configured PostgreSQL persistent named volume `teamflow_postgres_data` mapped to `/var/lib/postgresql/data`.
- Configured explicit container health checks (`CMD-SHELL` `pg_isready` for DB, `curl` `/api/v1/health` for backend, `curl` port 80 check for frontend) with `service_healthy` startup ordering.
- Updated Spring Boot CORS allowed origins in `SecurityConfig.java` to permit `http://localhost` and `http://localhost:80`.
- Created `.env.example` security credential template and verified `.env` git exclusion.
- Authored comprehensive deployment guide (`docs/deployment/docker-deployment-guide.md`).
- Executed live end-to-end containerized verification covering User Registration, Login, Team Creation, Project Creation, Sprint Creation, Task Creation, and Kanban status transitions.
- Verified PostgreSQL volume data persistence across full `docker compose down` and `docker compose up -d` container lifecycle.
- Confirmed test regression: 140/140 Java tests passed, 9/9 Vitest frontend tests passed, TypeScript compilation & Vite build passed.

**Challenges & Solutions**

- *Docker Desktop WSL 2 Integration*: Required initializing Docker Engine daemon in WSL 2 environment (`wsl -d Ubuntu -u root service docker start`) to orchestrate container lifecycle.
- *Container Health Check Alignment*: Added `curl` and explicit `HEALTHCHECK` instruction to `frontend/Dockerfile` and `docker-compose.yml` to satisfy `Up ... (healthy)` criteria across all three services (`db`, `backend`, `frontend`).

---

## S4-1 — Advanced Task Workflow & Audit Logging

**Commit**: `442f90d feat(tasks): implement advanced task workflow state machine and audit logging`

**Objective**

Extend TeamFlow AI's Task management domain with enforced task status transition state machine rules, explicit task priority escalation (`LOW`, `MEDIUM`, `HIGH`, `URGENT`), immutable activity audit logging (`task_activity_logs`), REST endpoint `GET /api/v1/tasks/{id}/activity`, and frontend activity timeline visualization.

**Completed**

- Created Flyway V3 database migration (`V3__add_task_workflow_and_audit_schema.sql`) updating `CHECK` constraints on `tasks.status` and `tasks.priority` and creating `task_activity_logs` audit table.
- Updated `TaskStatus` enum to include `IN_REVIEW` and `TaskPriority` enum to include `URGENT`.
- Implemented state machine transition validator (`validateStatusTransition`) in `TaskService` enforcing `TODO` → `IN_PROGRESS` → `IN_REVIEW` → `DONE` lifecycle. Invalid status jumps (e.g. `TODO` → `DONE`) return `HTTP 400 Bad Request`.
- Supported `URGENT` task priority without automatic assignment side effects (`assignedUserId` remains unchanged).
- Implemented field-specific transactional audit logging (`TASK_CREATED`, `STATUS_CHANGED`, `PRIORITY_CHANGED`, `ASSIGNEE_CHANGED`, `SPRINT_CHANGED`, `TASK_DELETED`) bound to task mutations.
- Enforced mandatory server-side actor identity extraction from JWT SecurityContext (`SecurityUtils.getCurrentUserId()`).
- Configured audit log retention after task row deletion using `task_id ON DELETE SET NULL` and `project_id ON DELETE CASCADE`, preserving historical `TASK_DELETED` records.
- Implemented `TaskActivityLogRepository` using Spring `JdbcTemplate` for query execution.
- Added `GET /api/v1/tasks/{id}/activity` REST endpoint in `TaskController` protected by project membership authorization (`checkProjectMemberOrAdmin`).
- Created `TaskActivityTimeline` UI component and integrated activity history display into React SPA `TaskDetailPage`.
- Added automated backend unit & integration tests (`TaskServiceTest`) reaching 144/144 passing Java tests.
- Added frontend unit tests (`taskApi.test.ts`) reaching 10/10 passing Vitest tests.
- Verified production Vite build and TypeScript compilation with zero errors.
- Executed live containerized E2E verification script (`test_s4_1_e2e.py`) against multi-container Docker Compose environment (`db`, `backend`, `frontend` all `healthy`).

**Challenges & Solutions**

- *Audit Log Deletion Semantics*: Configured `task_id REFERENCES tasks(id) ON DELETE SET NULL` and `project_id REFERENCES projects(id) ON DELETE CASCADE` in Flyway `V3` to ensure audit records (including `TASK_DELETED`) survive individual task row deletions while remaining queryable at the project level.
- *Actor Identity Security*: Enforced server-side resolution of `actor_user_id` strictly from Spring Security context to prevent client payload actor spoofing.

---

## Current Status

- Phase S1 — Backend Foundation: **COMPLETE**
- S2-1 — Frontend Foundation: **COMPLETE** (`3bbd261`)
- S2-2 — Team & Project Management UI: **COMPLETE** (`9112df0`)
- S2-3 — Sprint Planning & Task Kanban UI: **COMPLETE** (`6c00705`)
- S3-1 — DevOps & Multi-Container Dockerization: **COMPLETE** (`8606c79`)
- S4-1 — Advanced Task Workflow & Audit Logging: **COMPLETE** (`442f90d`)