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

## Future Phase Planning

Post-S1-6 milestones are currently not formally specified in the project roadmap.

### Potential Future Directions (Proposed / Uncommitted)

The following areas represent candidate directions for future project phases:

- **Frontend Development Phase**: Build a modern React + Vite SPA connecting to backend REST APIs.
- **Advanced Task Management**: Task status state machine transitions, priority escalation, activity audit logging.
- **AI Integration Phase**: LLM-powered sprint velocity forecasting, task breakdown generation, and automated standup summaries.
- **DevOps & Containerization**: Dockerization of backend, frontend, and PostgreSQL with Docker Compose.