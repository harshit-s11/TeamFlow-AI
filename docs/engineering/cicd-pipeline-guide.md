# TeamFlow AI — CI/CD Pipeline Guide

## 1. Overview

TeamFlow AI implements an automated, secure, and reproducible Continuous Integration and Continuous Release pipeline using **GitHub Actions** and **GitHub Container Registry (GHCR)**.

The pipeline consists of two primary workflows:
1. **CI Pipeline (`.github/workflows/ci.yml`)**: Triggered on Pull Requests targeting `main` and pushes to `main`. Validates backend Java tests, frontend Vitest tests, Vite production builds, and multi-container Docker Compose health checks.
2. **Release Pipeline (`.github/workflows/release.yml`)**: Triggered automatically via `workflow_run` ONLY after the CI Pipeline succeeds on `main`. Builds and publishes multi-stage Docker images to GHCR.

---

## 2. CI Workflow (`ci.yml`)

### Triggers
- `pull_request` targeting `main`
- `push` to `main`

### Permissions
- `contents: read`

### Jobs
1. **`backend-test` (Backend Java Tests & Build)**:
   - Runner: `ubuntu-latest`
   - JDK: Temurin Java 21 with Gradle caching
   - Commands: `./gradlew test --no-daemon` and `./gradlew bootJar -x test --no-daemon`
   - Uploads test report artifact on failure.
2. **`frontend-test-build` (Frontend Vitest & Production Build)**:
   - Runner: `ubuntu-latest`
   - Node: Node 20 with npm package-lock caching
   - Commands: `npm ci`, `npm test -- --run`, `npm run build`
3. **`container-integration-test` (Multi-Container Integration)**:
   - Runner: `ubuntu-latest`
   - Prepares `.env` from `.env.example`
   - Runs `docker compose up -d --build`
   - Waits for `teamflow-db` (PostgreSQL) and `teamflow-backend` (Spring Boot) health checks to pass.
   - Executes HTTP health check against `http://localhost:8080/api/v1/health`.
   - Executes HTTP smoke check against `http://localhost:80`.
   - Cleans up with `docker compose down -v`.

---

## 3. Release Workflow (`release.yml`)

### Triggers & Safety Dependency
- Triggered by `workflow_run` on completion of `TeamFlow AI CI Pipeline`.
- Condition: `if: ${{ github.event.workflow_run.conclusion == 'success' }}` on branch `main`.
- **Safety Guarantee**: If CI fails or is cancelled, image building and publishing are **completely skipped**.

### Permissions
- `contents: read`
- `packages: write`

### Target Registry & Image Tagging
- **Registry**: `ghcr.io`
- **Images**:
  - `ghcr.io/<owner>/teamflow-ai-backend`
  - `ghcr.io/<owner>/teamflow-ai-frontend`
- **Tagging**:
  1. `:<git-sha>`: Immutable full commit SHA tag corresponding to the exact verified source commit.
  2. `:latest`: Floating release tag updated upon successful `main` branch builds.

---

## 4. Gemini AI Key Isolation in CI

- **No Live API Calls**: Standard CI runs MUST NOT call Google Gemini API servers or require a live `GEMINI_API_KEY`.
- **Mocked Unit Tests**: Backend `AiServiceTest` mocks `GeminiApiClient` and executes entirely in-memory.
- **Graceful Unconfigured Handling**: During Docker Compose smoke testing, `GEMINI_API_KEY` is left blank/empty in `.env`. AI REST endpoints gracefully return `HTTP 503 Service Unavailable`, which is the verified expected behavior when AI integration is unconfigured.

---

## 5. How to Pull & Run Published Container Images

After a successful release build on `main`, pull and run images from GHCR:

```bash
# Log in to GHCR (requires GitHub PAT with read:packages permission or GITHUB_TOKEN)
echo $GHCR_PAT | docker login ghcr.io -u <YOUR_GITHUB_USERNAME> --password-stdin

# Pull latest published backend image
docker pull ghcr.io/<owner>/teamflow-ai-backend:latest

# Pull latest published frontend image
docker pull ghcr.io/<owner>/teamflow-ai-frontend:latest

# Run backend container
docker run -d --name teamflow-backend -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/teamflow_ai \
  ghcr.io/<owner>/teamflow-ai-backend:latest
```

---

## 6. Troubleshooting CI/CD Failures

- **Backend Test Failures**: Inspect the `java-test-report` artifact uploaded under the failed Actions run.
- **Frontend Build Failures**: Verify Vite TypeScript compilation locally via `npm run build` in `frontend/`.
- **Container Timeout**: Check container startup times in GitHub Actions step `Verify Container Status & Health`. If PostgreSQL takes longer than 60s to initialize, inspect container logs printed by `Output Container Logs on Failure`.
- **GHCR Push Permission Errors**: Ensure the repository settings under **Settings -> Actions -> General -> Workflow permissions** allow `Read and write permissions` or that `packages: write` is explicitly declared in `release.yml`.
