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
- **Containerization**: Docker / Docker Compose (*S3-1 PLANNED*)

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
- **Structured Error Responses**: `401 Unauthorized` for missing/invalid tokens; `403 Forbidden` for unauthorized resource access (`ApiErrorResponse`).

---

## Database Schema & Migrations

- `V1__create_core_schema.sql`: Core schema for `users`, `teams`, `team_members`, `projects`, `project_members`, `sprints`, `tasks`.
- `V2__add_authentication_fields.sql`: Additive migration adding `password_hash VARCHAR(255)` and `role VARCHAR(50) DEFAULT 'USER'`.

---

## Testing & Verification

- **Backend Test Suite**: 140 automated tests executed (100% pass rate).
- **Frontend Test Suite**: 9 Vitest unit/integration tests executed (100% pass rate).
- **Production Build**: Clean TypeScript compilation (`tsc && vite build`) with zero errors.

---

## Current Status

**Current Phase:** Phase S3 — DevOps & Deployment Foundation

Completed Milestones:

- ✅ **S0-1 through S0-11** — Project Setup Phase
- ✅ **S1-1 through S1-6** — Backend Foundation & Security Phase
- ✅ **S2-1** — React Frontend Foundation & Authentication Flow (`3bbd261`)
- ✅ **S2-2** — Team & Project Management UI (`9112df0`)
- ✅ **S2-3** — Sprint Planning & Task Kanban UI (`6c00705`)

Planned Next Milestone:

- 📋 **S3-1 — DevOps & Multi-Container Dockerization** (*PLANNED / NEXT*)

---

## Potential Future Work (Proposed / Uncommitted)

- Advanced task status state machine transitions & audit logging.
- AI assistant integration.

---

## License

This project is licensed under the MIT License. See the `LICENSE` file for details.