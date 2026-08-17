<p align="center">
  <img src="TeamFlow%20AI%20Banner.png" alt="TeamFlow AI Banner" width="100%" />
</p>

# TeamFlow AI

TeamFlow AI is a production-quality AI-powered Agile Project Management Platform being built to demonstrate modern backend engineering, system design, and software development practices.

The project serves as a portfolio application for Software Development Engineer (SDE) roles and emphasizes clean architecture, maintainability, scalability, and production-ready engineering workflows.

---

## Project Objectives

- Build a production-quality full-stack application.
- Strengthen Java 21 and Spring Boot expertise.
- Practice scalable backend architecture, REST API design, and security engineering.
- Apply modern software engineering best practices.
- Create a strong GitHub portfolio project for interviews.

---

## Technology Stack

### Backend
- **Language**: Java 21
- **Framework**: Spring Boot 4.1.0
- **Build System**: Gradle (Gradle Wrapper)
- **Database Access**: Spring JDBC (`JdbcTemplate`)
- **Database Migrations**: Flyway (`V1__create_core_schema.sql`, `V2__add_authentication_fields.sql`)
- **Security**: Spring Security 7.0, BCrypt (`PasswordEncoder` strength 12), JJWT 0.12.6

### Frontend
- **Framework**: React 18
- **Build Tool**: Vite 6
- **Language**: TypeScript 5
- **Routing**: React Router DOM 6
- **HTTP Client**: Axios 1.7
- **Testing**: Vitest 3.0 + Testing Library
- **Styling**: Vanilla CSS (HSL design tokens, modular component primitives)

### Database
- **Engine**: PostgreSQL 16+

### Containerization & Deployment
- **Containerization**: Multi-container Docker & Docker Compose orchestration (`postgres:16-alpine`, Java 21 Spring Boot backend, NGINX React SPA frontend) with container health checks and persistent PostgreSQL database volume storage (`8606c79`).

---

## Quick Start

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/harshit-s11/TeamFlow-AI.git
   cd TeamFlow-AI
   ```

2. **Configure Environment File**:
   - On Windows (PowerShell / CMD):
     ```cmd
     copy .env.example .env
     ```
   - On Linux / macOS:
     ```bash
     cp .env.example .env
     ```
   > **Note**: Setting `GEMINI_API_KEY` in `.env` is optional for running the core application. Base Agile features operate fully without an API key; live Gemini AI features require a valid key.

3. **Launch Docker Compose Stack**:
   ```bash
   docker compose up -d
   ```

4. **Verify Container Health**:
   ```bash
   docker compose ps
   ```

5. **Access Application**:
   - **Frontend UI**: [http://localhost/](http://localhost/)
   - **Backend Health Check**: [http://localhost:8080/api/v1/health](http://localhost:8080/api/v1/health)

---

## Architecture Pattern

TeamFlow AI strictly follows a clean full-stack architecture:

```text
React 18 + Vite SPA (Frontend)
    ↓ Axios HTTP (Bearer JWT)
Controller (REST API Layer)
    ↓
Service (Domain Business Logic & Security Authorization)
    ↓
Repository (Data Access Layer)
    ↓
JdbcTemplate (Parameterized SQL Execution)
    ↓
PostgreSQL (Database)
```

---

## API Surface

| Endpoint | Method | Access / Role Required | Description |
| :--- | :---: | :--- | :--- |
| `/api/v1/auth/register` | `POST` | Public | Register new user account |
| `/api/v1/auth/login` | `POST` | Public | Authenticate user & return JWT token |
| `/api/v1/health` | `GET` | Public | Application health check |
| `/api/v1/users` | `GET`, `POST` | `ADMIN` | List all users or create user |
| `/api/v1/users/{id}` | `GET`, `PUT`, `DELETE` | Self or `ADMIN` | Get, update, or delete user |
| `/api/v1/teams` | `GET`, `POST` | Authenticated | List user's teams or create team |
| `/api/v1/teams/{id}` | `GET`, `PUT`, `DELETE` | Team Member or `ADMIN` | Team CRUD operations |
| `/api/v1/teams/{id}/members` | `GET`, `POST`, `DELETE` | Team Member or `ADMIN` | Team membership management |
| `/api/v1/projects` | `GET`, `POST` | Authenticated | List user's projects or create project |
| `/api/v1/projects/{id}` | `GET`, `PUT`, `DELETE` | Project Member or `ADMIN` | Project CRUD operations |
| `/api/v1/projects/{id}/members` | `GET`, `POST`, `DELETE` | Project Member or `ADMIN` | Project membership management |
| `/api/v1/sprints` | `GET`, `POST` | `ADMIN` (GET) / Project Member (POST) | List all sprints or create sprint |
| `/api/v1/sprints/{id}` | `GET`, `PUT`, `DELETE` | Project Member or `ADMIN` | Sprint CRUD operations |
| `/api/v1/projects/{id}/sprints` | `GET` | Project Member or `ADMIN` | List project-scoped sprints |
| `/api/v1/tasks` | `GET`, `POST` | `ADMIN` (GET) / Project Member (POST) | List all tasks or create task |
| `/api/v1/tasks/{id}` | `GET`, `PUT`, `DELETE` | Project Member or `ADMIN` | Task CRUD operations |
| `/api/v1/tasks/{id}/activity` | `GET` | Project Member or `ADMIN` | List task activity audit logs |
| `/api/v1/ai/tasks/{id}/breakdown` | `POST` | Project Member or `ADMIN` | Generate AI task subtask breakdown draft preview |
| `/api/v1/ai/sprints/{id}/forecast` | `POST` | Project Member or `ADMIN` | Calculate sprint velocity & AI risk forecast |
| `/api/v1/ai/projects/{id}/standup-summary` | `POST` | Project Member or `ADMIN` | Generate automated daily standup summary |
| `/api/v1/projects/{id}/tasks` | `GET` | Project Member or `ADMIN` | List project-scoped tasks |
| `/api/v1/sprints/{id}/tasks` | `GET` | Project Member or `ADMIN` | List sprint-scoped tasks |

---

## Security Model

- **Password Hashing**: BCrypt (`BCryptPasswordEncoder` with strength 12).
- **Stateless Authentication**: JWT bearer token authentication (15-minute token lifespan).
- **Role-Based Access Control (RBAC)**: Supports `USER` and `ADMIN` roles.
- **Resource Membership Authorization**: Access to teams, projects, sprints, and tasks is restricted to enrolled members of `team_members` or `project_members`.
- **Creator Auto-Membership**: Creating a team or project automatically enrolls the creator into the membership table inside a single transaction.
- **IDOR Protection**: Requests attempting to access resources belonging to another team/project without membership return `HTTP 403 Forbidden`.
- **Server-Side Actor Extraction**: Audit events extract actor user identity exclusively from the authenticated JWT Security Context (`SecurityUtils.getCurrentUserId()`).
- **Server-Side AI Integration**: Gemini API calls (`gemini-3.6-flash`) are managed strictly server-side behind Spring `GeminiApiClient` with API key isolation (`GEMINI_API_KEY`).
- **Structured Error Responses**: `401 Unauthorized` for missing/invalid tokens; `403 Forbidden` for unauthorized resource access; `400 Bad Request` for invalid state machine transitions; `503 Service Unavailable` for missing AI key configuration (`ApiErrorResponse`).

---

## Database Schema & Migrations

- `V1__create_core_schema.sql`: Core schema for `users`, `teams`, `team_members`, `projects`, `project_members`, `sprints`, `tasks`.
- `V2__add_authentication_fields.sql`: Additive migration adding `password_hash VARCHAR(255)` and `role VARCHAR(50) DEFAULT 'USER'`.
- `V3__add_task_workflow_and_audit_schema.sql`: Migration updating `CHECK` constraints for `IN_REVIEW` status and `URGENT` priority, creating `task_activity_logs` audit table with task deletion retention (`task_id ON DELETE SET NULL`).

---

## Testing & Verification

- **Backend Test Suite**: 144 automated tests executed (100% pass rate).
- **Frontend Test Suite**: 15 Vitest unit/integration tests passing across 7 test files (100% pass rate).
- **Production Build**: Clean TypeScript compilation (`tsc && vite build`) with zero errors.
- **Container E2E Verification**: End-to-end REST API verification executed successfully against live containerized environment (`db`, `backend`, `frontend`).

---

## Current Status

**Current Phase:** Phase S6 — CI/CD & Automated Release Pipeline

Completed Milestones:

- ✅ **S0-1 through S0-11** — Project Setup Phase
- ✅ **S1-1 through S1-6** — Backend Foundation & Security Phase
- ✅ **S2-1** — React Frontend Foundation & Authentication Flow (`3bbd261`)
- ✅ **S2-2** — Team & Project Management UI (`9112df0`)
- ✅ **S2-3** — Sprint Planning & Task Kanban UI (`6c00705`)
- ✅ **S3-1** — DevOps & Multi-Container Dockerization (`8606c79`)
- ✅ **S4-1** — Advanced Task Workflow & Audit Logging (`442f90d`)
- ✅ **S5-1** — AI Integration & Agile Intelligence (`803696b`)
- ✅ **S6-1** — CI/CD & Automated Release Pipeline

---

## Continuous Integration & Automated Release (CI/CD)

- **CI Workflow (`.github/workflows/ci.yml`)**: Executes on Pull Requests targeting `main` and pushes to `main`. Automatically runs Java 21 backend tests (`./gradlew test`), Node 20 frontend Vitest tests (`npm test`), Vite production builds (`npm run build`), and multi-container Docker Compose health checks (`docker compose up -d`).
- **Release Workflow (`.github/workflows/release.yml`)**: Triggered automatically via `workflow_run` after CI passes on `main`. Builds and publishes tagged Docker images (`backend` and `frontend`) to GitHub Container Registry (`ghcr.io`) using full commit SHA and `latest` tags.
- **Security & Secret Isolation**: Operates under least-privilege permissions (`contents: read`, `packages: write`). Secrets and API keys are isolated; Gemini AI integration defaults to graceful `HTTP 503` handling in unconfigured CI smoke environments.

---

## Potential Future Work (Proposed / Uncommitted)

- Cloud Kubernetes (K8s) cluster deployment and production ingress routing.

---



## License

This project is licensed under the MIT License. See the `LICENSE` file for details.
