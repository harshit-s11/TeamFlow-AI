# TeamFlow AI — Docker Deployment Guide

## Overview

This guide provides instructions for building, deploying, and operating TeamFlow AI using Docker and Docker Compose.

The deployment topology consists of three containerized services connected via an isolated bridge network:
1. **`db`**: PostgreSQL 16+ database with named volume persistence (`postgres_data`).
2. **`backend`**: Spring Boot 4.1.0 (Java 21) REST API application container.
3. **`frontend`**: React 18 + Vite static single-page application served via NGINX container.

---

## Prerequisites

- **Docker Engine**: Version 20.10+ (Tested on Docker `29.6.2`)
- **Docker Compose**: Version 2.0+ (Tested on Compose `v5.3.1`)
- **Git**

---

## Environment Setup

1. Clone the repository and navigate to the project root:
   ```bash
   git clone https://github.com/harshit-s11/TeamFlow-AI.git
   cd TeamFlow-AI
   ```

2. Copy the example environment template `.env.example` to `.env`:
   ```bash
   cp .env.example .env
   ```

3. Configure runtime credentials in `.env`:
   ```env
   # Database Credentials
   TEAMFLOW_DB_USERNAME=teamflow_user
   TEAMFLOW_DB_PASSWORD=teamflow_password

   # JWT Security Settings
   TEAMFLOW_JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
   TEAMFLOW_JWT_EXPIRATION=900000
   ```
   *(Note: Never commit `.env` to Git repository).*

---

## Build & Launch Commands

### 1. Build Containers
Build multi-stage Docker images for backend and frontend services:
```bash
docker compose build
```

### 2. Start Services
Launch all services in detached mode:
```bash
docker compose up -d
```

### 3. Verify Container Health
Check container running status and health check state:
```bash
docker compose ps
```
Expected output:
```text
NAME                IMAGE                COMMAND                  SERVICE             CREATED             STATUS                    PORTS
teamflow-backend    teamflow-ai-backend  "java -jar /app/app.…"   backend             10 seconds ago      Up 10 seconds (healthy)   0.0.0.0:8080->8080/tcp
teamflow-db         postgres:16-alpine   "docker-entrypoint.s…"   db                  10 seconds ago      Up 10 seconds (healthy)   0.0.0.0:5432->5432/tcp
teamflow-frontend   teamflow-ai-frontend "/docker-entrypoint.…"   frontend            10 seconds ago      Up 10 seconds             0.0.0.0:80->80/tcp
```

### 4. Inspect Container Logs
View logs for individual services:
```bash
docker compose logs -f backend
docker compose logs -f db
docker compose logs -f frontend
```

---

## Application Access & Verification

Once all containers show `healthy` status:

1. **Frontend Web UI**: Open [http://localhost](http://localhost) in your web browser.
2. **Backend REST API**: Access health check at [http://localhost:8080/api/v1/health](http://localhost:8080/api/v1/health).
3. **End-to-End Workflow Verification**:
   - Register a new user at `http://localhost/register`.
   - Sign in at `http://localhost/login`.
   - Create a Team at `http://localhost/teams`.
   - Create a Project at `http://localhost/projects`.
   - Create a Sprint at `http://localhost/sprints`.
   - Create and manage tasks on the Kanban Board at `http://localhost/projects/<project-id>/kanban`.
   - Refresh browser directly on SPA routes (`/teams`, `/projects`, `/kanban`) to verify NGINX client-side routing fallback.

---

## Container Lifecycle & Maintenance

### Stopping Services
Stop running containers while preserving data volumes:
```bash
docker compose down
```

### Restarting Services & Data Persistence
Restart containers and verify database records survive container recreation:
```bash
docker compose up -d
```
The named Docker volume `teamflow_postgres_data` automatically retains all database tables and records.

### Destructive Reset (Warning)
To completely delete database volume storage and start fresh (destructive):
```bash
docker compose down -v
```

---

## Security Best Practices

1. **Secrets Protection**: `.env` is ignored by Git. No database passwords or JWT secret keys are baked into Dockerfiles or image layers.
2. **Non-Root Execution**: Backend execution uses an unprivileged `teamflow` container system user.
3. **CORS Restrictions**: Backend `SecurityConfig` allows origins `http://localhost` and `http://localhost:80`.
